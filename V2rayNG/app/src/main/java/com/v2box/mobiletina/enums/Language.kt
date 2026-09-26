package com.v2box.mobiletina.enums

enum class Language(val code: String) {
    AUTO("auto"),
    ENGLISH("en"),
    PERSIAN("fa");

    companion object {
        fun fromCode(code: String): Language {
            return entries.find { it.code == code } ?: AUTO
        }
    }
}
