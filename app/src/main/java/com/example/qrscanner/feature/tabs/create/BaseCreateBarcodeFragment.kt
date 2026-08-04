package com.example.qrscanner.feature.tabs.create

import androidx.fragment.app.Fragment
import com.example.qrscanner.extension.*
import com.example.qrscanner.model.Contact
import com.example.qrscanner.model.schema.Other
import com.example.qrscanner.model.schema.Schema

abstract class BaseCreateBarcodeFragment : Fragment() {
    protected val parentActivity by unsafeLazy { requireActivity() as CreateBarcodeActivity }

    open val latitude: Double? = null
    open val longitude: Double? = null

    open fun getBarcodeSchema(): Schema = Other("")
    open fun showPhone(phone: String) {}
    open fun showContact(contact: Contact) {}
    open fun showLocation(latitude: Double?, longitude: Double?) {}
}