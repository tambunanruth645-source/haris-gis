/**
 * HARIS GIS - Core TypeScript Type Definitions
 * Standar kontrak tipe data untuk Frontend & API Client
 */

export type RoleCode = 'ADMIN' | 'OPERATOR' | 'VIEWER';

export interface UserProfile {
  id: string;
  email: string;
  fullName: string;
  role: RoleCode;
  isActive: boolean;
  createdAt: string;
}

export type GeometryType = 'Point' | 'MultiPoint' | 'LineString' | 'MultiLineString' | 'Polygon' | 'MultiPolygon';

export interface LayerFieldDefinition {
  field: string;
  type: 'string' | 'number' | 'boolean' | 'date';
  label: string;
  required?: boolean;
}

export interface LayerGroup {
  id: string;
  name: string;
  iconSlug?: string;
  displayOrder: number;
  isExpanded: boolean;
}

export interface LayerStyleRule {
  fillColor?: string;
  fillOpacity?: number;
  strokeColor?: string;
  strokeWidth?: number;
  circleRadius?: number;
  heatmapWeightField?: string;
  heatmapRadius?: number;
  heatmapColorStops?: [number, string][];
}

export interface LayerMetadata {
  id: string;
  code: string;
  title: string;
  description?: string;
  groupId?: string;
  geometryType: GeometryType;
  srid: number;
  isPublic: boolean;
  isVisible: boolean;
  opacity: number;
  schemaDefinition: LayerFieldDefinition[];
  style: LayerStyleRule;
  createdAt: string;
  updatedAt: string;
}

// GeoJSON RFC 7946 Specification Types
export interface GeoJsonGeometry {
  type: GeometryType;
  coordinates: any;
}

export interface SpatialFeature<T = Record<string, any>> {
  type: 'Feature';
  id: string;
  layerId: string;
  geometry: GeoJsonGeometry;
  properties: T;
  createdAt?: string;
  updatedAt?: string;
}

export interface FeatureCollectionResponse<T = Record<string, any>> {
  type: 'FeatureCollection';
  totalFeatures: number;
  page: number;
  pageSize: number;
  features: SpatialFeature<T>[];
}

// Spatial Analysis Contract
export type SpatialDistanceUnit = 'meters' | 'kilometers' | 'feet' | 'miles';

export interface BufferAnalysisRequest {
  sourceLayerId: string;
  bufferDistance: number;
  distanceUnit: SpatialDistanceUnit;
  dissolve: boolean;
  outputLayerTitle: string;
}

export interface IntersectAnalysisRequest {
  primaryLayerId: string;
  overlayLayerId: string;
  outputLayerTitle: string;
}

export interface SpatialJobStatus {
  taskId: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  progressPercent: number;
  result?: {
    layerId: string;
    layerCode: string;
    totalFeatures: number;
    geometryType: GeometryType;
  };
  errorMessage?: string;
}

// Offline PWA & Dexie.js Sync Schema
export interface OfflineDraftFeature {
  localId?: number;
  layerId: string;
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  serverId?: string;
  geometry: GeoJsonGeometry;
  properties: Record<string, any>;
  timestamp: number;
  synced: boolean;
}
