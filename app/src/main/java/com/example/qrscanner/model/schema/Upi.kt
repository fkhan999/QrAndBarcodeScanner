package com.example.qrscanner.model.schema

import android.net.Uri
import com.example.qrscanner.extension.appendQueryParameterIfNotNullOrBlank

class Upi(
    val payeeVpa: String,
    val payeeName: String? = null,
    val amount: String? = null,
    val currency: String? = null,
    val note: String? = null,
    val transactionId: String? = null,
    val transactionRef: String? = null,
    val merchantCode: String? = null,
    val payeeUrl: String? = null
) : Schema {

    companion object {
        private const val SCHEME = "upi"
        private const val AUTHORITY_PAY = "pay"

        private const val KEY_PA = "pa"
        private const val KEY_PN = "pn"
        private const val KEY_AM = "am"
        private const val KEY_CU = "cu"
        private const val KEY_TN = "tn"
        private const val KEY_TID = "tid"
        private const val KEY_TR = "tr"
        private const val KEY_MC = "mc"
        private const val KEY_URL = "url"

        fun parse(text: String): Upi? {
            val uri = Uri.parse(text)

            if (!uri.scheme.equals(SCHEME, ignoreCase = true)) {
                return null
            }
            if (!uri.authority.equals(AUTHORITY_PAY, ignoreCase = true)) {
                return null
            }

            val payeeVpa = uri.getQueryParameter(KEY_PA)
            if (payeeVpa.isNullOrBlank()) {
                return null
            }

            return Upi(
                payeeVpa = payeeVpa,
                payeeName = uri.getQueryParameter(KEY_PN),
                amount = uri.getQueryParameter(KEY_AM),
                currency = uri.getQueryParameter(KEY_CU),
                note = uri.getQueryParameter(KEY_TN),
                transactionId = uri.getQueryParameter(KEY_TID),
                transactionRef = uri.getQueryParameter(KEY_TR),
                merchantCode = uri.getQueryParameter(KEY_MC),
                payeeUrl = uri.getQueryParameter(KEY_URL)
            )
        }
    }

    override val schema = BarcodeSchema.UPI

    override fun toFormattedText(): String {
        return listOfNotNull(
            payeeName,
            "UPI ID: $payeeVpa",
            amount?.let { amt ->
                val cur = currency ?: "INR"
                "$amt $cur"
            },
            note
        ).joinToString("\n")
    }

    override fun toBarcodeText(): String {
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(AUTHORITY_PAY)
            .appendQueryParameter(KEY_PA, payeeVpa)
            .appendQueryParameterIfNotNullOrBlank(KEY_PN, payeeName)
            .appendQueryParameterIfNotNullOrBlank(KEY_AM, amount)
            .appendQueryParameterIfNotNullOrBlank(KEY_CU, currency)
            .appendQueryParameterIfNotNullOrBlank(KEY_TN, note)
            .appendQueryParameterIfNotNullOrBlank(KEY_TID, transactionId)
            .appendQueryParameterIfNotNullOrBlank(KEY_TR, transactionRef)
            .appendQueryParameterIfNotNullOrBlank(KEY_MC, merchantCode)
            .appendQueryParameterIfNotNullOrBlank(KEY_URL, payeeUrl)
            .build()
            .toString()
    }
}
