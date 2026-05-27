package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class UserRole {
    SEEKER, LANDLORD, ADMIN
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val phone: String,
    val role: UserRole,
    val isVerified2FA: Boolean = false,
    val isVerifiedLandlord: Boolean = false,
    val isBanned: Boolean = false,
    val accountCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: String, // Apartment, Flat, Bungalow, Mansion, Studio, Bedsitter, Townhouse, Villa
    val county: String, // Nairobi, Kiambu, Mombasa, etc.
    val estate: String, // Estate/Sub-location
    val rentAmount: Double,
    val isNegotiable: Boolean = false,
    val bedrooms: Int, // 1 to 5+
    val bathrooms: Int,
    val sizeSqft: Int = 850, // Dimensions in square feet
    val amenitiesCsv: String = "", // e.g. "WiFi,Parking,Security,Water 24/7"
    val nearbyPlacesCsv: String = "", // e.g. "Schools,Hospitals,Shopping Malls"
    val isPetFriendly: Boolean = false,
    val availableDate: String = "", // YYYY-MM-DD
    val photosCsv: String = "", // Delimited photo URLs or resource identifiers
    val videoPath: String? = null,
    val landlordId: String,
    val landlordName: String,
    val landlordPhone: String,
    val landlordEmail: String,
    val isTaken: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis(),
    val isFlagged: Boolean = false,
    val flagCount: Int = 0,
    val viewCount: Int = 0,
    
    // AI Verification Module Fields
    val verificationStatus: String = "VERIFIED", // PENDING, VERIFIED, NEEDS_REVIEW, REJECTED, SUSPENDED
    val latitude: Double = -1.2921,
    val longitude: Double = 36.8219,
    val resolvedAddress: String = "Nairobi, Kenya",
    val verificationReason: String = "Approved or verified automatically",
    val aiConfidence: Int = 100,
    val streetViewImageUrl: String = ""
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val propertyId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "inquiries")
data class InquiryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val propertyTitle: String,
    val landlordId: String,
    val seekerId: String,
    val seekerName: String,
    val seekerPhone: String,
    val seekerEmail: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending" // Pending, Responded
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propertyId: String = "",
    val senderId: String,
    val receiverId: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_searches")
data class SavedSearchEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val query: String = "",
    val queryName: String = "",
    val county: String? = null,
    val propertyType: String? = null,
    val maxPrice: Double = 200000.0,
    val minBedrooms: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val userId: String,
    val userName: String,
    val rating: Int, // 1–5
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AuditLogEntity(
    val id: String = UUID.randomUUID().toString(),
    val actionKey: String,
    val actorId: String,
    val actorName: String,
    val details: String,
    val timestamp: String
)

data class ChatRoom(
    val id: String,
    val recipientName: String,
    val lastMessageSnip: String,
    val lastTimestamp: String
)

@Entity(tableName = "landlord_notifications")
data class LandlordNotificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val landlordId: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
