package com.wordbank.models

import java.util.*

data class PersistedGrant(
    val key: String,
    val type: String,
    val subjectId: String,
    val creationTime: Date,
    val expirationTime: Date
)