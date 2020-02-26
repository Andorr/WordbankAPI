package com.wordbank.helpers

import com.wordbank.dtos.PagedConfig
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
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

fun ApplicationCall.bindPagination() : PagedConfig {
    return this.request.queryParameters
        .let {
            PagedConfig()
                .apply {
                    page = if (it.contains("page")) it["page"]!!.toInt() else this.page
                    limit = if (it.contains("limit")) it["limit"]!!.toInt() else this.limit
                }
        }
}

fun ApplicationCall.principalSubject(): String? {
    return this.authentication.principal<JWTPrincipal>()?.payload?.subject
}