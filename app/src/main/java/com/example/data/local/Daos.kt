package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>
}

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties WHERE isFlagged = 0 ORDER BY dateCreated DESC")
    fun getAllAvailablePropertiesFlow(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE landlordId = :landlordId ORDER BY dateCreated DESC")
    fun getLandlordPropertiesFlow(landlordId: String): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE id = :propertyId LIMIT 1")
    suspend fun getPropertyById(propertyId: String): PropertyEntity?

    @Query("SELECT * FROM properties WHERE id = :propertyId LIMIT 1")
    fun getPropertyByIdFlow(propertyId: String): Flow<PropertyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity)

    @Update
    suspend fun updateProperty(property: PropertyEntity)

    @Delete
    suspend fun deleteProperty(property: PropertyEntity)

    @Query("DELETE FROM properties WHERE id = :propertyId")
    suspend fun deletePropertyById(propertyId: String)

    @Query("UPDATE properties SET isTaken = :isTaken WHERE id = :propertyId")
    suspend fun updatePropertyStatus(propertyId: String, isTaken: Boolean)

    @Query("UPDATE properties SET isFlagged = 1, flagCount = flagCount + 1 WHERE id = :propertyId")
    suspend fun flagProperty(propertyId: String)

    @Query("SELECT * FROM properties WHERE isFlagged = 1")
    fun getFlaggedPropertiesFlow(): Flow<List<PropertyEntity>>

    @Query("DELETE FROM properties WHERE landlordId = :userId")
    suspend fun deletePropertiesByLandlord(userId: String)
}

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE userId = :userId AND propertyId = :propertyId")
    suspend fun deleteBookmark(userId: String, propertyId: String)

    @Query("SELECT properties.* FROM properties INNER JOIN bookmarks ON properties.id = bookmarks.propertyId WHERE bookmarks.userId = :userId")
    fun getBookmarkedPropertiesFlow(userId: String): Flow<List<PropertyEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE userId = :userId AND propertyId = :propertyId)")
    fun isBookmarkedFlow(userId: String, propertyId: String): Flow<Boolean>

    @Query("DELETE FROM bookmarks WHERE userId = :userId")
    suspend fun deleteBookmarksByUser(userId: String)
}

@Dao
interface InquiryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: InquiryEntity)

    @Query("SELECT * FROM inquiries WHERE landlordId = :landlordId ORDER BY timestamp DESC")
    fun getInquiriesForLandlordFlow(landlordId: String): Flow<List<InquiryEntity>>

    @Query("SELECT * FROM inquiries WHERE seekerId = :seekerId ORDER BY timestamp DESC")
    fun getInquiriesForSeekerFlow(seekerId: String): Flow<List<InquiryEntity>>

    @Query("UPDATE inquiries SET status = :status WHERE id = :inquiryId")
    suspend fun updateInquiryStatus(inquiryId: String, status: String)

    @Query("DELETE FROM inquiries WHERE seekerId = :userId OR landlordId = :userId")
    suspend fun deleteInquiriesByUser(userId: String)

    @Query("SELECT * FROM inquiries ORDER BY timestamp DESC")
    fun getAllInquiriesFlow(): Flow<List<InquiryEntity>>
}

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("""
        SELECT * FROM chat_messages 
        WHERE propertyId = :propertyId AND 
        ((senderId = :userA AND receiverId = :userB) OR (senderId = :userB AND receiverId = :userA)) 
        ORDER BY timestamp ASC
    """)
    fun getChatMessagesFlow(propertyId: String, userA: String, userB: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllChatMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Query("""
        SELECT * FROM chat_messages 
        WHERE (senderId = :userA AND receiverId = :userB) OR (senderId = :userB AND receiverId = :userA) 
        ORDER BY timestamp ASC
    """)
    fun getDirectChatMessagesFlow(userA: String, userB: String): Flow<List<ChatMessageEntity>>

    @Query("""
        SELECT DISTINCT propertyId FROM chat_messages 
        WHERE senderId = :userId OR receiverId = :userId
    """)
    fun getActiveChatPropertiesFlow(userId: String): Flow<List<String>>

    @Query("DELETE FROM chat_messages WHERE senderId = :userId OR receiverId = :userId")
    suspend fun deleteMessagesByUser(userId: String)
}

@Dao
interface SavedSearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSearch(search: SavedSearchEntity)

    @Query("SELECT * FROM saved_searches WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSavedSearchesFlow(userId: String): Flow<List<SavedSearchEntity>>

    @Query("DELETE FROM saved_searches WHERE id = :searchId")
    suspend fun deleteSavedSearch(searchId: String)

    @Query("DELETE FROM saved_searches WHERE userId = :userId")
    suspend fun deleteSavedSearchesByUser(userId: String)
}

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("SELECT * FROM reviews WHERE propertyId = :propertyId ORDER BY timestamp DESC")
    fun getReviewsForPropertyFlow(propertyId: String): Flow<List<ReviewEntity>>

    @Query("DELETE FROM reviews WHERE userId = :userId")
    suspend fun deleteReviewsByUser(userId: String)
}

@Dao
interface LandlordNotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: LandlordNotificationEntity)

    @Query("SELECT * FROM landlord_notifications WHERE landlordId = :landlordId ORDER BY timestamp DESC")
    fun getNotificationsFlow(landlordId: String): Flow<List<LandlordNotificationEntity>>

    @Query("UPDATE landlord_notifications SET isRead = 1 WHERE landlordId = :landlordId")
    suspend fun markAllAsRead(landlordId: String)

    @Query("DELETE FROM landlord_notifications WHERE landlordId = :landlordId")
    suspend fun clearNotifications(landlordId: String)
}
