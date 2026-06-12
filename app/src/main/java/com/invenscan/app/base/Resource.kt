package com.invenscan.app.base

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int = -1) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
