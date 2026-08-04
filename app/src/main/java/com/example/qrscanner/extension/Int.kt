package com.example.qrscanner.extension

fun Int?.orZero(): Int {
    return this ?: 0
}