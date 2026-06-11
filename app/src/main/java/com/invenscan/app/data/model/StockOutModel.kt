package com.invenscan.app.data.model

import com.google.gson.annotations.SerializedName

data class StockOutTagInfo(
    @SerializedName("tagId") val tagId: String,
    @SerializedName("epcTag") val epcTag: String?,
    @SerializedName("itemId") val itemId: Long,
    @SerializedName("itemCode") val itemCode: String,
    @SerializedName("itemName") val itemName: String,
    @SerializedName("locationId") val locationId: Long,
    @SerializedName("locationName") val locationName: String?,
    @SerializedName("status") val status: String?
)

data class StockOutSubmitRequest(
    @SerializedName("locationId") val locationId: Long,
    @SerializedName("notes") val notes: String?,
    @SerializedName("details") val details: List<StockOutDetailRequest>
)

data class StockOutDetailRequest(
    @SerializedName("scannedCode") val scannedCode: String,
    @SerializedName("scanType") val scanType: String,
    @SerializedName("itemId") val itemId: Long?
)

data class StockOutBulkInfoRequest(
    @SerializedName("codes") val codes: List<String>,
    @SerializedName("scanType") val scanType: String
)
