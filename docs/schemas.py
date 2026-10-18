"""
HARIS GIS - Core Pydantic Schemas (v2)
Kontrak validasi data untuk FastAPI Backend Service
"""

from typing import Any, Dict, List, Literal, Optional
from uuid import UUID
from datetime import datetime
from pydantic import BaseModel, Field, EmailStr

# Auth & User
class UserBase(BaseModel):
    email: EmailStr
    full_name: str
    role_id: int
    is_active: bool = True

class UserCreate(UserBase):
    password: str = Field(..., min_length=8)

class UserResponse(UserBase):
    id: UUID
    created_at: datetime
    role_code: Optional[str] = None

    class Config:
        from_attributes = True

class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    expires_in_seconds: int

# Layer & Metadata
GeometryTypeEnum = Literal["Point", "MultiPoint", "LineString", "MultiLineString", "Polygon", "MultiPolygon"]

class LayerFieldDef(BaseModel):
    field: str
    type: Literal["string", "number", "boolean", "date"]
    label: str
    required: bool = False

class LayerCreate(BaseModel):
    code: str = Field(..., max_length=100)
    title: str = Field(..., max_length=200)
    description: Optional[str] = None
    group_id: Optional[UUID] = None
    geometry_type: GeometryTypeEnum
    srid: int = 4326
    is_public: bool = False
    default_opacity: float = Field(1.0, ge=0.0, le=1.0)
    schema_definition: List[LayerFieldDef] = []

class LayerResponse(LayerCreate):
    id: UUID
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

# GeoJSON & Spatial Feature
class GeoJSONGeometry(BaseModel):
    type: GeometryTypeEnum
    coordinates: Any

class SpatialFeatureCreate(BaseModel):
    geometry: GeoJSONGeometry
    properties: Dict[str, Any] = {}

class SpatialFeatureResponse(BaseModel):
    type: str = "Feature"
    id: UUID
    geometry: GeoJSONGeometry
    properties: Dict[str, Any]

class FeatureCollectionResponse(BaseModel):
    type: str = "FeatureCollection"
    totalFeatures: int
    page: int
    pageSize: int
    features: List[SpatialFeatureResponse]

# Spatial Analysis
class BufferAnalysisRequest(BaseModel):
    source_layer_id: UUID
    buffer_distance: float = Field(..., gt=0)
    distance_unit: Literal["meters", "kilometers"] = "meters"
    dissolve: bool = True
    output_layer_title: str

class IntersectAnalysisRequest(BaseModel):
    primary_layer_id: UUID
    overlay_layer_id: UUID
    output_layer_title: str

class SpatialJobStatusResponse(BaseModel):
    task_id: UUID
    status: Literal["PENDING", "PROCESSING", "COMPLETED", "FAILED"]
    progress_percent: int
    result: Optional[Dict[str, Any]] = None
    error_message: Optional[str] = None
