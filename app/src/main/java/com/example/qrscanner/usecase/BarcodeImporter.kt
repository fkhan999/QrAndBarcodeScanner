package com.example.qrscanner.usecase

import android.content.Context
import android.net.Uri
import com.example.qrscanner.extension.parseOrNull
import com.example.qrscanner.extension.unsafeLazy
import com.example.qrscanner.model.Barcode
import com.example.qrscanner.model.ExportBarcode
import com.google.zxing.BarcodeFormat
import io.reactivex.Single
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale

object BarcodeImporter {
    private val dateFormatter by unsafeLazy {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    }

    fun importBarcodeHistory(context: Context, uri: Uri): Single<Int> {
        return Single.fromCallable {
            val content = readText(context, uri)
            val exports = parse(content)
            val barcodes = exports.map(::toBarcode)
            var saved = 0
            barcodes.forEach { barcode ->
                // Block on each insert so Room work stays on this IO thread.
                BarcodeDatabase.getInstance(context)
                    .saveIfNotPresent(barcode)
                    .blockingGet()
                saved++
            }
            saved
        }
    }

    private fun readText(context: Context, uri: Uri): String {
        val stream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open file")
        return stream.use { input ->
            BufferedReader(InputStreamReader(input)).readText()
        }
    }

    private fun parse(content: String): List<ExportBarcode> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            return emptyList()
        }
        return if (trimmed.startsWith("[")) {
            parseJson(trimmed)
        } else {
            parseCsv(trimmed)
        }
    }

    private fun parseJson(content: String): List<ExportBarcode> {
        val array = JSONArray(content)
        return (0 until array.length()).mapNotNull { index ->
            val obj = array.optJSONObject(index) ?: return@mapNotNull null
            val dateText = obj.optString("date")
            val formatName = obj.optString("format")
            val text = obj.optString("text")
            toExportBarcode(dateText, formatName, text)
        }
    }

    private fun parseCsv(content: String): List<ExportBarcode> {
        return content
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filterNot { it.equals("Date,Format,Text", ignoreCase = true) }
            .mapNotNull { line ->
                // Export writes date,format,text without escaping; split on the
                // first two commas so commas inside the barcode text are kept.
                val first = line.indexOf(',')
                val second = if (first >= 0) line.indexOf(',', first + 1) else -1
                if (first < 0 || second < 0) {
                    return@mapNotNull null
                }
                val dateText = line.substring(0, first)
                val formatName = line.substring(first + 1, second)
                val text = line.substring(second + 1)
                toExportBarcode(dateText, formatName, text)
            }
            .toList()
    }

    private fun toExportBarcode(dateText: String, formatName: String, text: String): ExportBarcode? {
        if (formatName.isBlank() || text.isEmpty()) {
            return null
        }
        val format = try {
            BarcodeFormat.valueOf(formatName.trim())
        } catch (ex: Exception) {
            return null
        }
        val date = dateFormatter.parseOrNull(dateText.trim())?.time
            ?: System.currentTimeMillis()
        return ExportBarcode(date = date, format = format, text = text)
    }

    private fun toBarcode(export: ExportBarcode): Barcode {
        val schema = BarcodeParser.parseSchema(export.format, export.text)
        return Barcode(
            text = export.text,
            formattedText = schema.toFormattedText(),
            format = export.format,
            schema = schema.schema,
            date = export.date,
            isGenerated = false,
            isFavorite = false
        )
    }
}
