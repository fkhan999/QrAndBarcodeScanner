package com.example.qrscanner.extension

fun Boolean?.orFalse(): Boolean {
    return this ?: false
}