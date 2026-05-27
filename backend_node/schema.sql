-- ==========================================
-- KENYARENT POSTGRESQL PRODUCTION SCHEMA
-- ==========================================

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Define Enums
CREATE TYPE user_role AS ENUM ('SEEKER', 'LANDLORD', 'ADMIN');
CREATE TYPE inquiry_status AS ENUM ('Pending', 'Responded');

-- 1. Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role user_role DEFAULT 'SEEKER'::user_role,
    is_verified_2fa BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexing for fast credential lookups
CREATE INDEX idx_users_email ON users(email);

-- 2. Properties Table
CREATE TABLE properties (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- Apartment, Flat, Bungalow, Mansion, Studio, Bedsitter, Townhouse, Villa
    county VARCHAR(50) NOT NULL, -- Nairobi, Kiambu, Mombasa, Nakuru, Eldoret, Kisumu, Kajiado, Machakos
    estate VARCHAR(255) NOT NULL, -- Estate/Sub-location (free text)
    rent_amount DECIMAL(12, 2) NOT NULL CHECK (rent_amount >= 0),
    is_negotiable BOOLEAN DEFAULT FALSE,
    bedrooms INTEGER NOT NULL CHECK (bedrooms >= 0),
    bathrooms INTEGER NOT NULL CHECK (bathrooms >= 0),
    size_sqft INTEGER,
    amenities TEXT[], -- PostgreSQL Array e.g. {'WiFi', 'Security', 'Parking'}
    nearby TEXT[], -- e.g. {'Schools', 'Hospitals'}
    is_pet_friendly BOOLEAN DEFAULT FALSE,
    available_from DATE NOT NULL,
    photos_list TEXT[] NOT NULL DEFAULT '{}'::TEXT[], -- Compressed photo URLs (Max 10)
    video_url VARCHAR(512),
    landlord_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_taken BOOLEAN DEFAULT FALSE,
    is_flagged BOOLEAN DEFAULT FALSE,
    flag_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Compound indexing for optimized instant search metrics
CREATE INDEX idx_properties_search ON properties(county, type, rent_amount, bedrooms);
CREATE INDEX idx_properties_landlord ON properties(landlord_id);

-- 3. Bookmarks / Favourites Table
CREATE TABLE bookmarks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_bookmark UNIQUE (user_id, property_id)
);

CREATE INDEX idx_bookmarks_user ON bookmarks(user_id);

-- 4. Inquiries / Viewing Requests Table
CREATE TABLE inquiries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    seeker_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    status inquiry_status DEFAULT 'Pending'::inquiry_status,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inquiries_property ON inquiries(property_id);
CREATE INDEX idx_inquiries_seeker ON inquiries(seeker_id);

-- 5. Chat Messages Table (Direct Real-time Messaging)
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_messages_conversation ON chat_messages(property_id, sender_id, receiver_id);

-- 6. Saved Searches / Alerts Table
CREATE TABLE saved_searches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    query_name VARCHAR(255) NOT NULL,
    filters JSONB NOT NULL, -- Flexible query storage: { "county": "Nairobi", "rent": 50000 }
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_saved_searches_user ON saved_searches(user_id);

-- 7. Reviews & Ratings Table
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reviews_property ON reviews(property_id);

-- 2. AUTOMATIC TRIGGERS FOR UPDATE TIMESTAMP
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_modtime BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_properties_modtime BEFORE UPDATE ON properties FOR EACH ROW EXECUTE FUNCTION update_modified_column();
