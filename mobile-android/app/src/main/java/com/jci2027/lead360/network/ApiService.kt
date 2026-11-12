package com.jci2027.lead360.network

import com.jci2027.lead360.model.TemplatesResponse
import com.jci2027.lead360.vendor.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val user: UserDto?
)

data class UserDto(
    val id: String?,
    val name: String?,
    val email: String?,
    val role: String?,
    val chapterName: String?,
    val phone: String? = null,
    val photoUrl: String? = null,
    val zone: String? = null,
    val businessName: String? = null,
    val gstin: String? = null,
    val serviceAreas: String? = null,
    val logoUrl: String? = null,
    val bankDetails: BankDetails? = null
)

data class UsersListResponse(
    val success: Boolean,
    val users: List<UserDto>
)

data class SaveContactRequest(
    val id: String,
    val name: String,
    val company: String,
    val designation: String,
    val phone: String,
    val email: String,
    val website: String,
    val address: String,
    val eventTag: String,
    val locationName: String,
    val city: String,
    val notes: String,
    val followUpDate: String,
    val photoUrl: String? = null
)

data class SaveContactResponse(
    val success: Boolean,
    val message: String?,
    val contact: SaveContactRequest?
)

data class GetContactsResponse(
    val success: Boolean,
    val contacts: List<SaveContactRequest>?
)

data class UploadImageResponse(
    val success: Boolean,
    val fileUrl: String?,
    val filename: String?
)

data class UpdateOfficerRequest(
    val userId: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val role: String? = null
)

data class UpdateChapterRequest(
    val chapterName: String? = null,
    val zone: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val logoUrl: String? = null
)

data class GenericSuccessResponse(
    val success: Boolean,
    val message: String?
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val type: String? = "order",
    val read: Boolean = false,
    val contactId: String? = null,
    val data: Map<String, Any?>? = null,
    val createdAt: String? = null,
    val timestamp: String? = null
)

data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<NotificationItem>
)

interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("/api/templates")
    suspend fun getTemplates(
        @Header("Authorization") token: String
    ): Response<TemplatesResponse>

    @POST("/api/contacts")
    suspend fun saveContact(
        @Header("Authorization") token: String,
        @Body contact: SaveContactRequest
    ): Response<SaveContactResponse>

    @PUT("/api/contacts/{id}")
    suspend fun updateContact(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body contact: SaveContactRequest
    ): Response<SaveContactResponse>

    @DELETE("/api/contacts/{id}")
    suspend fun deleteContact(
        @Path("id") id: String
    ): Response<GenericSuccessResponse>

    @GET("/api/contacts")
    suspend fun getContacts(
        @Header("Authorization") token: String
    ): Response<GetContactsResponse>

    @Multipart
    @POST("/api/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<UploadImageResponse>

    @POST("/api/users/officer-details")
    suspend fun updateOfficerDetails(
        @Header("Authorization") token: String,
        @Body request: UpdateOfficerRequest
    ): Response<GenericSuccessResponse>

    @POST("/api/users/chapter-details")
    suspend fun updateChapterDetails(
        @Header("Authorization") token: String,
        @Body request: UpdateChapterRequest
    ): Response<GenericSuccessResponse>

    // Vendor & Catalog APIs
    @GET("/api/vendor/catalog")
    suspend fun getCatalog(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<CatalogResponse>

    @POST("/api/vendor/catalog")
    suspend fun saveCatalogItem(
        @Header("Authorization") token: String,
        @Body item: CatalogItem
    ): Response<GenericSuccessResponse>

    @DELETE("/api/vendor/catalog/{id}")
    suspend fun deleteCatalogItem(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<GenericSuccessResponse>

    @GET("/api/vendor/orders")
    suspend fun getVendorOrders(
        @Header("Authorization") token: String
    ): Response<OrdersResponse>

    @POST("/api/vendor/orders")
    suspend fun placeOrder(
        @Header("Authorization") token: String,
        @Body order: PlaceOrderRequest
    ): Response<GenericSuccessResponse>

    @PUT("/api/vendor/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("id") orderId: String,
        @Body statusBody: Map<String, String>
    ): Response<GenericSuccessResponse>

    @GET("/api/vendor/rfqs")
    suspend fun getVendorRfqs(
        @Header("Authorization") token: String
    ): Response<RfqsResponse>

    @PUT("/api/vendor/profile")
    suspend fun updateVendorProfile(
        @Header("Authorization") token: String,
        @Body profile: VendorProfile
    ): Response<GenericSuccessResponse>

    @GET("/api/vendor/notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): Response<NotificationsResponse>

    @POST("/api/vendor/notifications/read-all")
    suspend fun markAllNotificationsRead(
        @Header("Authorization") token: String
    ): Response<GenericSuccessResponse>

    @POST("/api/users/fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<GenericSuccessResponse>

    // Super Admin APIs
    @GET("/api/users")
    suspend fun getAllUsers(
        @Header("Authorization") token: String
    ): Response<UsersListResponse>

    // Chapter Members & Approvals APIs
    @GET("/api/members")
    suspend fun getMembers(
        @Header("Authorization") token: String
    ): Response<MembersResponse>

    @GET("/api/members/celebrations")
    suspend fun getCelebrations(
        @Header("Authorization") token: String
    ): Response<CelebrationsResponse>

    @POST("/api/members")
    suspend fun saveMember(
        @Header("Authorization") token: String,
        @Body member: MemberDto
    ): Response<GenericSuccessResponse>

    @PUT("/api/members/{id}")
    suspend fun updateMember(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body member: MemberDto
    ): Response<GenericSuccessResponse>

    @POST("/api/members/{id}/approve")
    suspend fun approveMember(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<GenericSuccessResponse>

    @DELETE("/api/members/{id}")
    suspend fun deleteMember(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<GenericSuccessResponse>
}

data class MemberDto(
    val id: String,
    val name: String,
    val phone: String,
    val email: String? = "",
    val role: String? = "Member",
    val membershipType: String? = "Active Member",
    val joinYear: String? = "2027",
    val bloodGroup: String? = "O+",
    val dob: String? = "",
    val anniversaryDate: String? = "",
    val chapterName: String? = "",
    val chapterId: String? = "",
    val zone: String? = "",
    val businessName: String? = "",
    val designation: String? = "",
    val businessCategory: String? = "",
    val businessPhone: String? = "",
    val businessAddress: String? = "",
    val city: String? = "",
    val website: String? = "",
    val services: String? = "",
    val status: String? = "Active",
    val photoUrl: String? = null,
    val lat: Double? = 13.6288,
    val lng: Double? = 79.4192,
    val spouseName: String? = "",
    val daysUntil: Int? = 0,
    val celebrationType: String? = ""
)

data class CelebrationsData(
    val todayCount: Int,
    val todayBirthdays: List<MemberDto>,
    val todayAnniversaries: List<MemberDto>,
    val upcomingBirthdays: List<MemberDto>,
    val upcomingAnniversaries: List<MemberDto>
)

data class CelebrationsResponse(
    val success: Boolean,
    val data: CelebrationsData
)

data class MembersResponse(
    val success: Boolean,
    val count: Int,
    val members: List<MemberDto>
)
