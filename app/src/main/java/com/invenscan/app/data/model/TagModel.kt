package com.invenscan.app.data.model

import com.google.gson.annotations.SerializedName

data class TagModel(
    @SerializedName("id") val id: Long,
    @SerializedName("tagId") val tagId: String,
    @SerializedName("epcTag") val epcTag: String,
    @SerializedName("itemId") val itemId: Long,
    @SerializedName("itemCode") val itemCode: String?,
    @SerializedName("itemName") val itemName: String?,
    @SerializedName("locationId") val locationId: Long,
    @SerializedName("locationName") val locationName: String?,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class TagRegisterRequest(
    @SerializedName("tags") val tags: List<TagRegisterItem>
)

data class TagRegisterItem(
    @SerializedName("epcTag") val epcTag: String,
    @SerializedName("itemId") val itemId: Long,
    @SerializedName("locationId") val locationId: Long
)

data class StockInTagInfo(
    @SerializedName("tagId") val tagId: String,
    @SerializedName("epcTag") val epcTag: String?,
    @SerializedName("itemId") val itemId: Long,
    @SerializedName("itemCode") val itemCode: String,
    @SerializedName("itemName") val itemName: String,
    @SerializedName("locationId") val locationId: Long,
    @SerializedName("locationName") val locationName: String?,
    @SerializedName("status") val status: String?
)

data class StockInSubmitRequest(
    @SerializedName("locationId") val locationId: Long,
    @SerializedName("notes") val notes: String?,
    @SerializedName("details") val details: List<StockInDetailRequest>
)

data class StockInDetailRequest(
    @SerializedName("scannedCode") val scannedCode: String,
    @SerializedName("scanType") val scanType: String,
    @SerializedName("itemId") val itemId: Long?
)

data class StockInBulkInfoRequest(
    @SerializedName("codes") val codes: List<String>,
    @SerializedName("scanType") val scanType: String
)
