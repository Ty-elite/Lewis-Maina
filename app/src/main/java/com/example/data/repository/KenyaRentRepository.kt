package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class KenyaRentRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val propertyDao = database.propertyDao()
    private val bookmarkDao = database.bookmarkDao()
    private val inquiryDao = database.inquiryDao()
    private val chatMessageDao = database.chatMessageDao()
    private val savedSearchDao = database.savedSearchDao()
    private val reviewDao = database.reviewDao()
    private val landlordNotificationDao = database.landlordNotificationDao()

    // --- Authentication & Users ---

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    suspend fun registerUser(user: UserEntity) = userDao.insertUser(user)

    suspend fun getUserById(userId: String): UserEntity? = userDao.getUserById(userId)

    val allUsersFlow: Flow<List<UserEntity>> = userDao.getAllUsersFlow()

    /**
     * Enforces GDPR Privacy compliance. Deletes the user profile and recursively cleans up all
     * associated listings, bookmarks, chat histories, reviews, and inquiry records.
     */
    suspend fun eraseUserAccount(userId: String) {
        val user = userDao.getUserById(userId) ?: return
        if (user.role == UserRole.LANDLORD) {
            // Remove properties listed by this landlord
            propertyDao.deletePropertiesByLandlord(userId)
        }
        bookmarkDao.deleteBookmarksByUser(userId)
        inquiryDao.deleteInquiriesByUser(userId)
        chatMessageDao.deleteMessagesByUser(userId)
        savedSearchDao.deleteSavedSearchesByUser(userId)
        reviewDao.deleteReviewsByUser(userId)
        userDao.deleteUserById(userId)
    }

    // --- Properties ---

    val allPropertiesFlow: Flow<List<PropertyEntity>> = propertyDao.getAllAvailablePropertiesFlow()

    fun getLandlordProperties(landlordId: String): Flow<List<PropertyEntity>> =
        propertyDao.getLandlordPropertiesFlow(landlordId)

    suspend fun getPropertyById(propertyId: String): PropertyEntity? =
        propertyDao.getPropertyById(propertyId)

    fun getPropertyByIdFlow(propertyId: String): Flow<PropertyEntity?> =
        propertyDao.getPropertyByIdFlow(propertyId)

    suspend fun addProperty(property: PropertyEntity) = propertyDao.insertProperty(property)

    suspend fun updateProperty(property: PropertyEntity) = propertyDao.updateProperty(property)

    suspend fun deleteProperty(propertyId: String) = propertyDao.deletePropertyById(propertyId)

    suspend fun updatePropertyAvailability(propertyId: String, isTaken: Boolean) =
        propertyDao.updatePropertyStatus(propertyId, isTaken)

    suspend fun reportAndFlagProperty(propertyId: String) = propertyDao.flagProperty(propertyId)

    val flaggedPropertiesFlow: Flow<List<PropertyEntity>> = propertyDao.getFlaggedPropertiesFlow()

    // --- Bookmarks / Saved Listings ---

    fun getBookmarkedProperties(userId: String): Flow<List<PropertyEntity>> =
        bookmarkDao.getBookmarkedPropertiesFlow(userId)

    fun isBookmarked(userId: String, propertyId: String): Flow<Boolean> =
        bookmarkDao.isBookmarkedFlow(userId, propertyId)

    suspend fun addBookmark(userId: String, propertyId: String) =
        bookmarkDao.insertBookmark(BookmarkEntity(userId = userId, propertyId = propertyId))

    suspend fun removeBookmark(userId: String, propertyId: String) =
        bookmarkDao.deleteBookmark(userId, propertyId)

    // --- Inquiries ---

    fun getInquiriesForLandlord(landlordId: String): Flow<List<InquiryEntity>> =
        inquiryDao.getInquiriesForLandlordFlow(landlordId)

    fun getInquiriesForSeeker(seekerId: String): Flow<List<InquiryEntity>> =
        inquiryDao.getInquiriesForSeekerFlow(seekerId)

    suspend fun submitInquiry(inquiry: InquiryEntity) {
        inquiryDao.insertInquiry(inquiry)
        // Also seed an initial response chat message to simulate a real connection
        val initialChatMessage = ChatMessageEntity(
            propertyId = inquiry.propertyId,
            senderId = inquiry.landlordId,
            receiverId = inquiry.seekerId,
            message = "Jambo ${inquiry.seekerName}! Thank you for inquiring about '${inquiry.propertyTitle}'. Yes, it is available for viewing. When would you be free?"
        )
        chatMessageDao.insertMessage(initialChatMessage)
    }

    suspend fun updateInquiryStatus(inquiryId: String, status: String) =
        inquiryDao.updateInquiryStatus(inquiryId, status)

    // --- In-App real-time style Messaging ---

    fun getChatMessages(propertyId: String, userA: String, userB: String): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getChatMessagesFlow(propertyId, userA, userB)

    fun getAllChatMessagesFlow(): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getAllChatMessagesFlow()

    fun getDirectChatMessages(userA: String, userB: String): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getDirectChatMessagesFlow(userA, userB)

    fun getAllInquiriesFlow(): Flow<List<InquiryEntity>> =
        inquiryDao.getAllInquiriesFlow()

    suspend fun sendMessage(message: ChatMessageEntity) = chatMessageDao.insertMessage(message)

    fun getActiveChatProperties(userId: String): Flow<List<String>> =
        chatMessageDao.getActiveChatPropertiesFlow(userId)

    // --- Saved Searches ---

    fun getSavedSearches(userId: String): Flow<List<SavedSearchEntity>> =
        savedSearchDao.getSavedSearchesFlow(userId)

    suspend fun saveSearchQuery(search: SavedSearchEntity) = savedSearchDao.insertSavedSearch(search)

    suspend fun deleteSavedSearch(searchId: String) = savedSearchDao.deleteSavedSearch(searchId)

    // --- Reviews & Ratings ---

    fun getReviews(propertyId: String): Flow<List<ReviewEntity>> =
        reviewDao.getReviewsForPropertyFlow(propertyId)

    suspend fun submitReview(review: ReviewEntity) = reviewDao.insertReview(review)

    // --- Landlord Notifications ---
    fun getLandlordNotifications(landlordId: String): Flow<List<LandlordNotificationEntity>> =
        landlordNotificationDao.getNotificationsFlow(landlordId)

    suspend fun addLandlordNotification(notification: LandlordNotificationEntity) =
        landlordNotificationDao.insertNotification(notification)

    suspend fun markLandlordNotificationsAsRead(landlordId: String) =
        landlordNotificationDao.markAllAsRead(landlordId)

    suspend fun clearLandlordNotifications(landlordId: String) =
        landlordNotificationDao.clearNotifications(landlordId)

    // --- Database Initialization (Seed Mock Properties if empty) ---

    suspend fun seedInitialDataIfRequired() {
        val existing = allPropertiesFlow.first()
        if (existing.isEmpty()) {
            // Standard seed landlords
            val landLordId1 = "landlord_peter"
            val landLordId2 = "landlord_sarah"
            val adminId = "admin_user"

            // Insert initial default landlords/admin so searching accounts acts fully verified
            userDao.insertUser(
                UserEntity(
                    id = landLordId1,
                    email = "peter@kenyarent.co.ke",
                    passwordHash = "SecurePass1!",
                    fullName = "Peter Kamau",
                    phone = "0712345678",
                    role = UserRole.LANDLORD,
                    isVerified2FA = true
                )
            )
            userDao.insertUser(
                UserEntity(
                    id = landLordId2,
                    email = "sarah@kenyarent.co.ke",
                    passwordHash = "SecurePass1!",
                    fullName = "Sarah Mwangi",
                    phone = "0787654321",
                    role = UserRole.LANDLORD,
                    isVerified2FA = true
                )
            )
            userDao.insertUser(
                UserEntity(
                    id = adminId,
                    email = "admin@kenyarent.com",
                    passwordHash = "AdminPass1!",
                    fullName = "KenyaRent Admin",
                    phone = "0700000000",
                    role = UserRole.ADMIN,
                    isVerified2FA = true
                )
            )

            // Seed Properties
            val defaultProperties = listOf(
                PropertyEntity(
                    id = "p1",
                    title = "Spacious 2-Bedroom Apartment in Westlands",
                    type = "Apartment",
                    county = "Nairobi",
                    estate = "Westlands, Rhapta Road",
                    rentAmount = 85000.0,
                    isNegotiable = true,
                    bedrooms = 2,
                    bathrooms = 2,
                    sizeSqft = 1200,
                    amenitiesCsv = "WiFi,Parking,Security,Water 24/7,Gym,Balcony",
                    nearbyPlacesCsv = "Schools,Shopping Malls,Public Transport",
                    isPetFriendly = true,
                    availableDate = "2026-06-01",
                    photosCsv = "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00,https://images.unsplash.com/photo-1512917774080-9991f1c4c750",
                    videoPath = null,
                    landlordId = landLordId1,
                    landlordName = "Peter Kamau",
                    landlordPhone = "0712345678",
                    landlordEmail = "peter@kenyarent.co.ke",
                    isTaken = false,
                    viewCount = 142
                ),
                PropertyEntity(
                    id = "p2",
                    title = "Cozy Modern Studio in Ruaka near Two Rivers Mall",
                    type = "Studio",
                    county = "Kiambu",
                    estate = "Ruaka, Joyland Estate",
                    rentAmount = 22000.0,
                    isNegotiable = false,
                    bedrooms = 1,
                    bathrooms = 1,
                    sizeSqft = 450,
                    amenitiesCsv = "WiFi,Parking,Security,Balcony",
                    nearbyPlacesCsv = "Hospitals,Shopping Malls,Public Transport",
                    isPetFriendly = false,
                    availableDate = "2026-05-31",
                    photosCsv = "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267",
                    videoPath = null,
                    landlordId = landLordId1,
                    landlordName = "Peter Kamau",
                    landlordPhone = "0712345678",
                    landlordEmail = "peter@kenyarent.co.ke",
                    isTaken = false,
                    viewCount = 98
                ),
                PropertyEntity(
                    id = "p3",
                    title = "Luxury 4-Bedroom Villa with Pool",
                    type = "Villa",
                    county = "Mombasa",
                    estate = "Nyali, Links Road",
                    rentAmount = 180000.0,
                    isNegotiable = true,
                    bedrooms = 4,
                    bathrooms = 4,
                    sizeSqft = 4200,
                    amenitiesCsv = "WiFi,Parking,Security,Water 24/7,Backup Generator,Swimming Pool,Balcony,Garden,Gym,Furnished",
                    nearbyPlacesCsv = "Schools,Hospitals,Shopping Malls,Public Transport",
                    isPetFriendly = true,
                    availableDate = "2026-06-15",
                    photosCsv = "https://images.unsplash.com/photo-1613977257363-707ba9348227,https://images.unsplash.com/photo-1580587771525-78b9dba3b914",
                    videoPath = "local_sample_video.mp4",
                    landlordId = landLordId2,
                    landlordName = "Sarah Mwangi",
                    landlordPhone = "0787654321",
                    landlordEmail = "sarah@kenyarent.co.ke",
                    isTaken = false,
                    viewCount = 310
                ),
                PropertyEntity(
                    id = "p4",
                    title = "Executive 5-Bedroom Mansion in Nakuru Milimani",
                    type = "Mansion",
                    county = "Nakuru",
                    estate = "Milimani Sector C",
                    rentAmount = 115000.0,
                    isNegotiable = true,
                    bedrooms = 5,
                    bathrooms = 5,
                    sizeSqft = 3600,
                    amenitiesCsv = "Parking,Security,Water 24/7,Backup Generator,Garden,Balcony",
                    nearbyPlacesCsv = "Schools,Hospitals,Public Transport",
                    isPetFriendly = true,
                    availableDate = "2026-06-05",
                    photosCsv = "https://images.unsplash.com/photo-1564013799919-ab600027ffc6",
                    videoPath = null,
                    landlordId = landLordId2,
                    landlordName = "Sarah Mwangi",
                    landlordPhone = "0787654321",
                    landlordEmail = "sarah@kenyarent.co.ke",
                    isTaken = false,
                    viewCount = 87
                ),
                PropertyEntity(
                    id = "p5",
                    title = "Charming 3-Bedroom Bungalow with Lush Garden",
                    type = "Bungalow",
                    county = "Eldoret",
                    estate = "Elgon View",
                    rentAmount = 45000.0,
                    isNegotiable = true,
                    bedrooms = 3,
                    bathrooms = 2,
                    sizeSqft = 1800,
                    amenitiesCsv = "Parking,Security,Water 24/7,Garden",
                    nearbyPlacesCsv = "Schools,Hospitals,Public Transport",
                    isPetFriendly = true,
                    availableDate = "2026-06-10",
                    photosCsv = "https://images.unsplash.com/photo-1568605114967-8130f3a36994",
                    videoPath = null,
                    landlordId = landLordId1,
                    landlordName = "Peter Kamau",
                    landlordPhone = "0712345678",
                    landlordEmail = "peter@kenyarent.co.ke",
                    isTaken = false,
                    viewCount = 74
                ),
                PropertyEntity(
                    id = "p6",
                    title = "Elegant 3-Bedroom Flat Overlooking Kisumu Lakefront",
                    type = "Flat",
                    county = "Kisumu",
                    estate = "Milimani Lakeview",
                    rentAmount = 60000.0,
                    isNegotiable = false,
                    bedrooms = 3,
                    bathrooms = 3,
                    sizeSqft = 1600,
                    amenitiesCsv = "WiFi,Parking,Security,Water 24/7,Balcony",
                    nearbyPlacesCsv = "Shopping Malls,Public Transport",
                    isPetFriendly = false,
                    availableDate = "2026-06-01",
                    photosCsv = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688",
                    videoPath = null,
                    landlordId = landLordId1,
                    landlordName = "Peter Kamau",
                    landlordPhone = "0712345678",
                    landlordEmail = "peter@kenyarent.co.ke",
                    isTaken = false,
                    viewCount = 105
                ),
                PropertyEntity(
                    id = "p7",
                    title = "Affordable 1-Bedroom Apartment near SGR station",
                    type = "Apartment",
                    county = "Machakos",
                    estate = "Syokimau Phase 2",
                    rentAmount = 18000.0,
                    isNegotiable = false,
                    bedrooms = 1,
                    bathrooms = 1,
                    sizeSqft = 600,
                    amenitiesCsv = "WiFi,Parking,Security,Water 24/7",
                    nearbyPlacesCsv = "Shopping Malls,Public Transport",
                    isPetFriendly = false,
                    availableDate = "2026-05-28",
                    photosCsv = "https://images.unsplash.com/photo-1484154218962-a197022b5858",
                    videoPath = null,
                    landlordId = landLordId2,
                    landlordName = "Sarah Mwangi",
                    landlordPhone = "0787654321",
                    landlordEmail = "sarah@kenyarent.co.ke",
                    isTaken = false,
                    viewCount = 203
                )
            )

            for (p in defaultProperties) {
                propertyDao.insertProperty(p)
            }

            // Seed reviews
            reviewDao.insertReview(
                ReviewEntity(
                    propertyId = "p1",
                    userId = "seeker_brian",
                    userName = "Brian Omondi",
                    rating = 5,
                    comment = "Peter is an amazing landlord. The apartment is extremely clean, and water is always running 24/7! Location is very secure."
                )
            )
            reviewDao.insertReview(
                ReviewEntity(
                    propertyId = "p3",
                    userId = "seeker_amina",
                    userName = "Amina Hassan",
                    rating = 4,
                    comment = "Beautiful Nyali villa, absolutely breathtaking swimming pool. Slightly expensive but definitely worth the premium price."
                )
            )
        }
    }
}
