package com.wordbank.controllers

import com.wordbank.models.Word
import com.wordbank.models.WordType
import com.wordbank.services.Dictionary
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

fun Route.dict(kodein: Kodein) {
    val dict : Dictionary by kodein.instance<Dictionary>();

    route("api/v1/encyc/dict") {

        get() {

        }

    }
}