-- ============================================================
-- HARIS GIS - DATABASE INITIALIZATION SCRIPT (PostGIS Schema)
-- ============================================================

-- 1. Enable Required Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";
CREATE EXTENSION IF NOT EXISTS "postgis_topology";

-- 2. Master Table: Roles (RBAC)
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    permissions JSONB NOT NULL DEFAULT '{}'
);

INSERT INTO roles (name, description, permissions) VALUES
('ADMIN', 'Akses administrator penuh ke seluruh modul dan konfigurasi sistem', '{"all": true}'),
('OPERATOR', 'Dapat mengunggah data, CRUD fitur spasial, dan membuat layer baru', '{"layers:create": true, "layers:edit": true, "features:write": true}'),
('VIEWER', 'Hanya melihat peta, kueri atribut, dan analisis spasial baca', '{"layers:read": true, "features:read": true}')
ON CONFLICT (name) DO NOTHING;

-- 3. Master Table: Users
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    role_id INT NOT NULL REFERENCES roles(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Layer Groups / Categories
CREATE TABLE IF NOT EXISTS layer_groups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Layers Metadata
CREATE TABLE IF NOT EXISTS layers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(100) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    group_id UUID REFERENCES layer_groups(id) ON DELETE SET NULL,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    geometry_type VARCHAR(50) NOT NULL, -- Point, LineString, Polygon, MultiPolygon
    srid INT NOT NULL DEFAULT 4326,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    is_visible_default BOOLEAN NOT NULL DEFAULT TRUE,
    default_opacity FLOAT NOT NULL DEFAULT 1.0 CHECK (default_opacity BETWEEN 0.0 AND 1.0),
    schema_definition JSONB NOT NULL DEFAULT '[]',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Spatial Features Table
CREATE TABLE IF NOT EXISTS spatial_features (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    layer_id UUID NOT NULL REFERENCES layers(id) ON DELETE CASCADE,
    geom GEOMETRY(Geometry, 4326) NOT NULL,
    properties JSONB NOT NULL DEFAULT '{}',
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Spatial Indexes (GIST) & JSONB Indexes (GIN)
CREATE INDEX IF NOT EXISTS idx_spatial_features_geom ON spatial_features USING GIST (geom);
CREATE INDEX IF NOT EXISTS idx_spatial_features_layer_id ON spatial_features (layer_id);
CREATE INDEX IF NOT EXISTS idx_spatial_features_props ON spatial_features USING GIN (properties);

-- 7. Symbology & Layer Styles
CREATE TABLE IF NOT EXISTS layer_styles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    layer_id UUID NOT NULL REFERENCES layers(id) ON DELETE CASCADE,
    style_type VARCHAR(50) NOT NULL DEFAULT 'simple',
    style_rules JSONB NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Bookmarks & Map States
CREATE TABLE IF NOT EXISTS bookmarks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    center_coordinates JSONB NOT NULL, -- [lon, lat]
    zoom_level FLOAT NOT NULL,
    active_layer_ids JSONB DEFAULT '[]',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. Audit Logging
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    target_entity VARCHAR(100) NOT NULL,
    target_id UUID,
    change_delta JSONB DEFAULT '{}',
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
