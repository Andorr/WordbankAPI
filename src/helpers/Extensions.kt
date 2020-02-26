package com.wordbank.helpers

import com.wordbank.dtos.PagedConfig
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveOrNull
import java.lang.Exception

inline fun <T : Any?> T.whenNull(f: () -> Unit): T   {
    if(this == null) {
        f()
    }
    return this
}

suspend inline fun <reified T: Any> ApplicationCall.bindOrNull(): T?{
    return try {
        this.receiveOrNull<T>()
    } catch(e: Exception) {
        null
    }
}

suspend fun ApplicationCall.bindPagination() : PagedConfig {
    return this.request.queryParameters
        .let {
            PagedConfig()
                .apply {
                    page = if (it.contains("page")) it["page"]!!.toInt() else this.page
                    limit = if (it.contains("limit")) it["limit"]!!.toInt() else this.limit
                }
        }
}