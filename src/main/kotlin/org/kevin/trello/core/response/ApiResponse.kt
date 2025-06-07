package org.kevin.trello.core.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class ApiResponse<T>(
    responseCode: ResponseCode,
    val message: String? = null,
    val data: T? = null
) {
    val code = responseCode.code

    override fun toString(): String {
        return "ApiResponse(message=$message, data=$data, code=$code)"
    }

    companion object {
        fun <T> success(message: String? = null, data: T? = null): ApiResponse<T> {
            return ApiResponse(ResponseCode.SUCCESS, message, data)
        }

        fun <T> error(message: String? = null, data: T? = null): ApiResponse<T> {
            return ApiResponse(ResponseCode.ERROR, message, data)
        }
    }
}