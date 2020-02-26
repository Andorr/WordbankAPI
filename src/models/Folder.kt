package com.wordbank.models

data class Folder(val name: String): Base() {
    val folders : MutableList<String> = ArrayList()
    val words : MutableList<String> = ArrayList()
    val tags : MutableList<String> = ArrayList()
}