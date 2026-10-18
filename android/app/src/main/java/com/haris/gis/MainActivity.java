package com.haris.gis;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private TextView tvCoordinates;
    private TextView tvInfoTitle;
    private TextView tvInfoDescription;
    private MaterialButton btnSaveFeature;
    private MaterialButton btnExportData;

    // Data Penanda Lapangan (In-Memory & Ekspor GeoJSON)
    private final List<FieldMarkerItem> savedFeatures = new ArrayList<>();

    // Mode Pengukuran Jarak
    private boolean isMeasuring = false;
    private final List<GeoPoint> measurePoints = new ArrayList<>();
    private Polyline measureLine;

    // Model Data Titik Aset
    public static class FieldMarkerItem {
        public String name;
        public String description;
        public double latitude;
        public double longitude;

        public FieldMarkerItem(String name, String description, double latitude, double longitude) {
            this.name = name;
            this.description = description;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inisialisasi Osmdroid Configuration (Penting untuk caching offline di Android)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_main);

        // 2. Inisialisasi Komponen Tampilan UI
        mapView = findViewById(R.id.mapView);
        tvCoordinates = findViewById(R.id.tvCoordinates);
        tvInfoTitle = findViewById(R.id.tvInfoTitle);
        tvInfoDescription = findViewById(R.id.tvInfoDescription);
        btnSaveFeature = findViewById(R.id.btnSaveFeature);
        btnExportData = findViewById(R.id.btnExportData);

        FloatingActionButton fabMyLocation = findViewById(R.id.fabMyLocation);
        FloatingActionButton fabLayers = findViewById(R.id.fabLayers);
        FloatingActionButton fabMeasure = findViewById(R.id.fabMeasure);
        FloatingActionButton fabAddMarker = findViewById(R.id.fabAddMarker);

        // 3. Konfigurasi MapView (Ringan, Cepat, Hemat Memori)
        mapView.setTileSource(TileSourceFactory.MAPNIK); // OpenStreetMap Standard
        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        // Posisi Awal Default (Jakarta / Pusat Indonesia)
        GeoPoint startPoint = new GeoPoint(-6.2088, 106.8456);
        mapView.getController().setZoom(14.0);
        mapView.getController().setCenter(startPoint);

        // 4. Update Koordinat saat Peta Digeser
        mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                updateCoordinatesDisplay();
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                updateCoordinatesDisplay();
                return false;
            }
        });

        // 5. Cek & Minta Izin GPS dan Penyimpanan
        checkAndRequestPermissions();

        // 6. Listener Tombol Cepat
        fabMyLocation.setOnClickListener(v -> centerOnMyLocation());
        fabLayers.setOnClickListener(v -> showLayerSelectionDialog());
        fabMeasure.setOnClickListener(v -> toggleMeasureMode());
        fabAddMarker.setOnClickListener(v -> promptAddMarkerAtCenter());

        btnSaveFeature.setOnClickListener(v -> promptAddMarkerAtCenter());
        btnExportData.setOnClickListener(v -> exportFeaturesToGeoJSON());
    }

    private void updateCoordinatesDisplay() {
        if (mapView == null) return;
        GeoPoint center = (GeoPoint) mapView.getMapCenter();
        if (center != null) {
            tvCoordinates.setText(String.format(Locale.getDefault(),
                    "Lat: %.5f | Lon: %.5f | Zoom: %.1f",
                    center.getLatitude(), center.getLongitude(), mapView.getZoomLevelDouble()));
        }
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        boolean allGranted = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            enableMyLocationOverlay();
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void enableMyLocationOverlay() {
        if (myLocationOverlay == null) {
            myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
            myLocationOverlay.enableMyLocation();
            myLocationOverlay.enableFollowLocation();
            mapView.getOverlays().add(myLocationOverlay);
        }
    }

    private void centerOnMyLocation() {
        if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
            GeoPoint myLoc = myLocationOverlay.getMyLocation();
            mapView.getController().animateTo(myLoc);
            mapView.getController().setZoom(17.0);
            Toast.makeText(this, "Peta diarahkan ke lokasi Anda", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Mencari sinyal GPS...", Toast.LENGTH_SHORT).show();
            if (myLocationOverlay != null) {
                myLocationOverlay.enableMyLocation();
            }
        }
    }

    private void showLayerSelectionDialog() {
        String[] layers = {"OpenStreetMap Standard", "Peta Topografi (OpenTopo)", "Satelit Ringan"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Lapisan Peta (Basemap)");
        builder.setItems(layers, (dialog, which) -> {
            switch (which) {
                case 0:
                    mapView.setTileSource(TileSourceFactory.MAPNIK);
                    Toast.makeText(this, "Basemap: OpenStreetMap", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    mapView.setTileSource(TileSourceFactory.OpenTopo);
                    Toast.makeText(this, "Basemap: Topografi", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    mapView.setTileSource(TileSourceFactory.USGS_SAT);
                    Toast.makeText(this, "Basemap: Satelit USGS", Toast.LENGTH_SHORT).show();
                    break;
            }
            mapView.invalidate();
        });
        builder.show();
    }

    private void toggleMeasureMode() {
        isMeasuring = !isMeasuring;
        if (isMeasuring) {
            measurePoints.clear();
            if (measureLine != null) {
                mapView.getOverlays().remove(measureLine);
            }
            tvInfoTitle.setText("Mode Pengukuran Jarak Aktif");
            tvInfoDescription.setText("Arahkan pusat peta ke titik awal, lalu klik 'Simpan Titik' untuk menambah titik ukur.");
            Toast.makeText(this, "Mode Ukur Jarak AKTIF", Toast.LENGTH_SHORT).show();
        } else {
            if (measureLine != null) {
                mapView.getOverlays().remove(measureLine);
                mapView.invalidate();
            }
            measurePoints.clear();
            tvInfoTitle.setText("HARIS GIS Siap Digunakan");
            tvInfoDescription.setText("Geser peta untuk menentukan koordinat survei.");
            Toast.makeText(this, "Mode Ukur Selesai", Toast.LENGTH_SHORT).show();
        }
    }

    private void promptAddMarkerAtCenter() {
        final GeoPoint center = (GeoPoint) mapView.getMapCenter();
        if (center == null) return;

        if (isMeasuring) {
            // Mode pengukuran jarak
            measurePoints.add(center);
            if (measureLine == null) {
                measureLine = new Polyline(mapView);
                measureLine.getOutlinePaint().setColor(0xFFEF4444); // Merah
                measureLine.getOutlinePaint().setStrokeWidth(6.0f);
                mapView.getOverlays().add(measureLine);
            }
            measureLine.setPoints(measurePoints);
            mapView.invalidate();

            if (measurePoints.size() >= 2) {
                double totalDistanceMeters = 0;
                for (int i = 0; i < measurePoints.size() - 1; i++) {
                    totalDistanceMeters += measurePoints.get(i).distanceToAsDouble(measurePoints.get(i + 1));
                }
                String distanceStr;
                if (totalDistanceMeters >= 1000) {
                    distanceStr = String.format(Locale.getDefault(), "%.2f km", totalDistanceMeters / 1000.0);
                } else {
                    distanceStr = String.format(Locale.getDefault(), "%.1f meter", totalDistanceMeters);
                }
                tvInfoDescription.setText(String.format("Total Titik: %d | Jarak Terukur: %s", measurePoints.size(), distanceStr));
            }
            return;
        }

        // Mode Tambah Titik Lapangan
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tambah Titik Aset Lapangan");

        View viewInflated = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, null);
        final EditText inputName = new EditText(this);
        inputName.setHint("Nama Aset (contoh: Pipa Air, Tiang Listrik)");
        builder.setView(inputName);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            if (name.isEmpty()) name = "Titik Lapangan #" + (savedFeatures.size() + 1);

            FieldMarkerItem item = new FieldMarkerItem(name, "Diinput via HARIS GIS Android", center.getLatitude(), center.getLongitude());
            savedFeatures.add(item);

            // Tambahkan Marker ke MapView
            Marker marker = new Marker(mapView);
            marker.setPosition(center);
            marker.setTitle(item.name);
            marker.setSnippet(String.format(Locale.getDefault(), "Lat: %.5f, Lon: %.5f", item.latitude, item.longitude));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
            mapView.invalidate();

            tvInfoTitle.setText("Aset Disimpan: " + item.name);
            tvInfoDescription.setText(String.format(Locale.getDefault(), "Total %d titik tersimpan di HP.", savedFeatures.size()));
            Toast.makeText(this, "Titik berhasil disimpan!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void exportFeaturesToGeoJSON() {
        if (savedFeatures.isEmpty()) {
            Toast.makeText(this, "Belum ada titik yang disimpan untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Buat String Format Standar GeoJSON RFC 7946
        JsonObject root = new JsonObject();
        root.addProperty("type", "FeatureCollection");

        JsonArray featuresArray = new JsonArray();
        for (FieldMarkerItem item : savedFeatures) {
            JsonObject feature = new JsonObject();
            feature.addProperty("type", "Feature");

            JsonObject geom = new JsonObject();
            geom.addProperty("type", "Point");
            JsonArray coords = new JsonArray();
            coords.add(item.longitude);
            coords.add(item.latitude);
            geom.add("coordinates", coords);
            feature.add("geometry", geom);

            JsonObject props = new JsonObject();
            props.addProperty("name", item.name);
            props.addProperty("description", item.description);
            feature.add("properties", props);

            featuresArray.add(feature);
        }
        root.add("features", featuresArray);

        String geoJsonString = new Gson().toJson(root);

        // Bagikan via WhatsApp / Email / File Manager
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Data Spasial HARIS GIS");
        shareIntent.putExtra(Intent.EXTRA_TEXT, geoJsonString);
        startActivity(Intent.createChooser(shareIntent, "Bagikan Data GeoJSON via:"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocationOverlay();
            } else {
                Toast.makeText(this, "Izin GPS dibutuhkan untuk tracking lokasi", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }
}
