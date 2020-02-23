package com.wordbank.helpers

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
