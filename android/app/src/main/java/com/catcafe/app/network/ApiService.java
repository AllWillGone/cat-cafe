package com.catcafe.app.network;

import com.catcafe.app.model.BatchOrderResponse;
import com.catcafe.app.model.ChangePasswordRequest;
import com.catcafe.app.model.AdminUserListItem;
import com.catcafe.app.model.AdminUserUpdateRequest;
import com.catcafe.app.model.CatWriteRequest;
import com.catcafe.app.model.CatDetail;
import com.catcafe.app.model.CommentAuditRequest;
import com.catcafe.app.model.CommentCreateRequest;
import com.catcafe.app.model.CommentDetail;
import com.catcafe.app.model.LikeCreateRequest;
import com.catcafe.app.model.LikeDetail;
import com.catcafe.app.model.LoginRequest;
import com.catcafe.app.model.LoginResponse;
import com.catcafe.app.model.OrderCreateRequest;
import com.catcafe.app.model.OrderUpdateRequest;
import com.catcafe.app.model.PaginatedCats;
import com.catcafe.app.model.PaginatedComments;
import com.catcafe.app.model.PaginatedLikes;
import com.catcafe.app.model.PaginatedOrders;
import com.catcafe.app.model.PaginatedProducts;
import com.catcafe.app.model.PaginatedUsers;
import com.catcafe.app.model.ProductDetail;
import com.catcafe.app.model.ProductWriteRequest;
import com.catcafe.app.model.RegisterRequest;
import com.catcafe.app.model.ResetPasswordByPhoneRequest;
import com.catcafe.app.model.SendSmsCodeRequest;
import com.catcafe.app.model.UserDetail;
import com.catcafe.app.model.UserUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("api/cats")
    Call<PaginatedCats> getCats(@Query("keyword") String keyword, @Query("skip") int skip, @Query("limit") int limit);

    @GET("api/cats")
    Call<PaginatedCats> getCats(@Query("keyword") String keyword, @Query("includeAll") Boolean includeAll, @Query("skip") int skip, @Query("limit") int limit);

    @GET("api/cats/{id}")
    Call<CatDetail> getCat(@Path("id") long id);

    @GET("api/products")
    Call<PaginatedProducts> getProducts(@Query("category") Integer category, @Query("keyword") String keyword, @Query("skip") int skip, @Query("limit") int limit);

    @GET("api/products/{id}")
    Call<ProductDetail> getProduct(@Path("id") long id);

    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    @GET("api/user/me")
    Call<UserDetail> getMe();

    @PUT("api/user/me")
    Call<UserDetail> updateMe(@Body UserUpdateRequest request);

    @PUT("api/user/me/password")
    Call<java.util.Map<String, Object>> changePassword(@Body ChangePasswordRequest request);

    @POST("api/send-sms-code")
    Call<java.util.Map<String, Object>> sendSmsCode(@Body SendSmsCodeRequest request);

    @POST("api/reset-password-by-phone")
    Call<java.util.Map<String, Object>> resetPasswordByPhone(@Body ResetPasswordByPhoneRequest request);

    @GET("api/comments")
    Call<PaginatedComments> getComments(@Query("targetType") Integer targetType, @Query("targetId") Long targetId, @Query("skip") int skip, @Query("limit") int limit);

    @GET("api/comments/my")
    Call<PaginatedComments> getMyComments(@Query("targetType") Integer targetType, @Query("skip") int skip, @Query("limit") int limit);

    @POST("api/comments")
    Call<CommentDetail> createComment(@Body CommentCreateRequest request);

    @DELETE("api/comments/{id}")
    Call<java.util.Map<String, Object>> deleteComment(@Path("id") long id);

    @GET("api/likes")
    Call<PaginatedLikes> getLikes(@Query("likeType") Integer likeType, @Query("keyword") String keyword, @Query("skip") int skip, @Query("limit") int limit);

    @POST("api/likes")
    Call<LikeDetail> createLike(@Body LikeCreateRequest request);

    @DELETE("api/likes/{id}")
    Call<java.util.Map<String, Object>> deleteLike(@Path("id") long id);

    @POST("api/orders")
    Call<BatchOrderResponse> createOrder(@Body OrderCreateRequest request);

    @GET("api/orders")
    Call<PaginatedOrders> getOrders(@Query("orderStatus") Integer orderStatus, @Query("keyword") String keyword, @Query("skip") int skip, @Query("limit") int limit);

    @GET("api/orders/{id}")
    Call<BatchOrderResponse> getOrder(@Path("id") long id);

    @PUT("api/orders/{id}")
    Call<BatchOrderResponse> updateOrder(@Path("id") long id, @Body OrderUpdateRequest request);

    @DELETE("api/orders/{id}")
    Call<java.util.Map<String, Object>> deleteOrder(@Path("id") long id);

    @GET("api/admin/users")
    Call<PaginatedUsers> adminGetUsers(@Query("keyword") String keyword, @Query("userType") Integer userType,
                                       @Query("sortBy") String sortBy, @Query("sortOrder") String sortOrder,
                                       @Query("skip") int skip, @Query("limit") int limit);

    @PUT("api/admin/users/{id}")
    Call<AdminUserListItem> adminUpdateUser(@Path("id") long id, @Body AdminUserUpdateRequest request);

    @DELETE("api/admin/users/{id}")
    Call<java.util.Map<String, Object>> adminDeleteUser(@Path("id") long id);

    @GET("api/admin/products")
    Call<PaginatedProducts> adminGetProducts(@Query("category") Integer category, @Query("status") Integer status,
                                             @Query("keyword") String keyword, @Query("skip") int skip, @Query("limit") int limit);

    @POST("api/admin/products")
    Call<ProductDetail> adminCreateProduct(@Body ProductWriteRequest request);

    @PUT("api/admin/products/{id}")
    Call<ProductDetail> adminUpdateProduct(@Path("id") long id, @Body ProductWriteRequest request);

    @DELETE("api/admin/products/{id}")
    Call<java.util.Map<String, Object>> adminDeleteProduct(@Path("id") long id);

    @PUT("api/admin/products/{id}/off")
    Call<java.util.Map<String, Object>> adminDelistProduct(@Path("id") long id);

    @GET("api/admin/cats")
    Call<PaginatedCats> adminGetCats(@Query("status") Integer status, @Query("keyword") String keyword,
                                     @Query("skip") int skip, @Query("limit") int limit);

    @POST("api/admin/cats")
    Call<CatDetail> adminCreateCat(@Body CatWriteRequest request);

    @PUT("api/admin/cats/{id}")
    Call<CatDetail> adminUpdateCat(@Path("id") long id, @Body CatWriteRequest request);

    @DELETE("api/admin/cats/{id}")
    Call<java.util.Map<String, Object>> adminDeleteCat(@Path("id") long id);

    @GET("api/admin/comments")
    Call<PaginatedComments> adminGetComments(@Query("auditStatus") Integer auditStatus, @Query("keyword") String keyword,
                                             @Query("skip") int skip, @Query("limit") int limit);

    @PUT("api/admin/comments/{id}/audit")
    Call<CommentDetail> adminAuditComment(@Path("id") long id, @Body CommentAuditRequest request);

    @GET("api/admin/orders")
    Call<PaginatedOrders> adminGetOrders(@Query("orderStatus") Integer orderStatus, @Query("keyword") String keyword,
                                         @Query("skip") int skip, @Query("limit") int limit);
}
