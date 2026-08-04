package com.example.qrscanner.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.qrscanner.R
import com.example.qrscanner.extension.isNotBlank
import com.example.qrscanner.extension.textString
import com.example.qrscanner.feature.tabs.create.BaseCreateBarcodeFragment
import com.example.qrscanner.model.schema.Schema
import com.example.qrscanner.model.schema.Upi
import kotlinx.android.synthetic.main.fragment_create_qr_code_upi.*

class CreateQrCodeUpiFragment : BaseCreateBarcodeFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_qr_code_upi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUpiIdEditText()
        handleTextChanged()
    }

    override fun getBarcodeSchema(): Schema {
        return Upi(
            payeeVpa = edit_text_upi_id.textString,
            payeeName = edit_text_payee_name.textString,
            amount = edit_text_amount.textString,
            currency = edit_text_currency.textString,
            note = edit_text_transaction_note.textString
        )
    }

    private fun initUpiIdEditText() {
        edit_text_upi_id.requestFocus()
    }

    private fun handleTextChanged() {
        edit_text_upi_id.addTextChangedListener { toggleCreateBarcodeButton() }
    }

    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled = edit_text_upi_id.isNotBlank()
    }
}
