package com.invenscan.app.data.model

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("userId") val userId: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("role") val role: String,
    @SerializedName("expiresAt") val expiresAt: String?
)
