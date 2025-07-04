package com.hanpass.updatecryoto

object Mapping {

}

fun String.idMapping(): String {
    return when {
        else -> {
            this
        }
    }
}

fun String.beforeFirebaseMapping(): String {
    return if (this == "game") {
        "game2"
    } else {
        this
    }
}