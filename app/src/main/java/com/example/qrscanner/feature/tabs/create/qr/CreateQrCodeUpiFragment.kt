package com.example.qrscanner.feature.tabs.create.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.qrscanner.R
import com.example.qrscanner.extension.isNotBlank
import com.example.qrscanner.extension.textString
import com.example.qrscanner.feature.tabs.create.BaseCreateBarcodeFragment
import com.example.qrscanner.model.schema.Schema
import com.example.qrscanner.model.schema.Upi
import kotlinx.android.synthetic.main.fragment_create_qr_code_upi.*

class CreateQrCodeUpiFragment : BaseCreateBarcodeFragment() {

    companion object {
        private const val ADDRESS_TYPE_UPI_ID = 0
        private const val ADDRESS_TYPE_ACCOUNT_IFSC = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_qr_code_upi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAddressTypeSpinner()
        handleTextChanged()
        toggleAddressFields()
        toggleCreateBarcodeButton()
    }

    override fun getBarcodeSchema(): Schema {
        return Upi(
            payeeVpa = getVpa(),
            payeeName = edit_text_payee_name.textString,
            amount = edit_text_amount.textString,
            currency = edit_text_currency.textString,
            note = edit_text_transaction_note.textString
        )
    }

    private fun initAddressTypeSpinner() {
        spinner_address_type.adapter = ArrayAdapter.createFromResource(
            requireContext(), R.array.fragment_create_qr_code_upi_address_types, R.layout.item_spinner
        ).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }

        spinner_address_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                toggleAddressFields()
                toggleCreateBarcodeButton()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun toggleAddressFields() {
        val isAccountMode = isAccountMode()
        text_input_layout_upi_id.isVisible = isAccountMode.not()
        layout_account_ifsc.isVisible = isAccountMode
        text_view_vpa_preview.isVisible = isAccountMode
        updateVpaPreview()
    }

    private fun updateVpaPreview() {
        if (isAccountMode().not()) {
            return
        }
        text_view_vpa_preview.text = buildVpaFromAccount()
            ?.let { getString(R.string.fragment_create_qr_code_upi_vpa_preview, it) }
    }

    private fun isAccountMode(): Boolean {
        return spinner_address_type.selectedItemPosition == ADDRESS_TYPE_ACCOUNT_IFSC
    }

    private fun getVpa(): String {
        return if (isAccountMode()) {
            buildVpaFromAccount().orEmpty()
        } else {
            edit_text_upi_id.textString
        }
    }

    private fun buildVpaFromAccount(): String? {
        val accountNumber = edit_text_account_number.textString.trim()
        val ifsc = edit_text_ifsc.textString.trim()
        if (accountNumber.isEmpty() || ifsc.isEmpty()) {
            return null
        }
        // UPI VPA constructed from a bank account: account@ifsc.ifsc.npci
        return "$accountNumber@${ifsc.lowercase()}.ifsc.npci"
    }

    private fun handleTextChanged() {
        edit_text_upi_id.addTextChangedListener { toggleCreateBarcodeButton() }
        edit_text_account_number.addTextChangedListener {
            toggleCreateBarcodeButton()
            updateVpaPreview()
        }
        edit_text_ifsc.addTextChangedListener {
            toggleCreateBarcodeButton()
            updateVpaPreview()
        }
    }

    private fun toggleCreateBarcodeButton() {
        parentActivity.isCreateBarcodeButtonEnabled =
            if (isAccountMode()) {
                edit_text_account_number.isNotBlank() && edit_text_ifsc.isNotBlank()
            } else {
                edit_text_upi_id.isNotBlank()
            }
    }
}
