package com.wordbank.dtos

data class PagedList<T>(
    val items: Iterable<T>,
    val totalCount: Int,
    val pageSize: Int,
    val hasNextPage: Boolean
)

data class PagedConfig(
    var page: Int = 1,
    var limit: Int = 20
)