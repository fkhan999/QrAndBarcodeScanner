package com.example.qrscanner.feature.tabs.history.export

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.qrscanner.R
import com.example.qrscanner.di.barcodeImporter
import com.example.qrscanner.extension.applySystemWindowInsets
import com.example.qrscanner.extension.showError
import com.example.qrscanner.feature.BaseActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_import_history.*

class ImportHistoryActivity : BaseActivity() {
    private val disposable = CompositeDisposable()

    companion object {
        private const val REQUEST_CHOOSE_FILE = 101

        fun start(context: Context) {
            val intent = Intent(context, ImportHistoryActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_history)
        supportEdgeToEdge()
        initToolbar()
        initImportButton()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHOOSE_FILE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            importHistory(uri)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun supportEdgeToEdge() {
        root_view.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun initToolbar() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initImportButton() {
        button_import.setOnClickListener {
            chooseFile()
        }
    }

    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "text/*",
                    "text/csv",
                    "text/comma-separated-values",
                    "application/json",
                    "application/octet-stream"
                )
            )
        }
        startActivityForResult(intent, REQUEST_CHOOSE_FILE)
    }

    private fun importHistory(uri: Uri) {
        showLoading(true)
        barcodeImporter
            .importBarcodeHistory(this, uri)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { count ->
                    showHistoryImported(count)
                },
                { error ->
                    showLoading(false)
                    showError(error)
                }
            )
            .addTo(disposable)
    }

    private fun showLoading(isLoading: Boolean) {
        progress_bar_loading.isVisible = isLoading
        scroll_view.isVisible = isLoading.not()
    }

    private fun showHistoryImported(count: Int) {
        Toast.makeText(
            this,
            getString(R.string.activity_import_history_imported, count),
            Toast.LENGTH_LONG
        ).show()
        finish()
    }
}
