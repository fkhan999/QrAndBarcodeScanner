package com.example.qrscanner.extension

fun Double?.orZero(): Double {
    return this ?: 0.0
}