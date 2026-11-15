package com.vendkit.shelfcount.scan

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/** A barcode found by ML Kit, already reduced to what the app stores. */
data class ScanResult(val value: String, val format: String)

object BarcodeReader {
    val client: BarcodeScanner by lazy {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(
                Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39, Barcode.FORMAT_QR_CODE,
            ).build()
        )
    }

    fun toResult(barcode: Barcode): ScanResult? {
        val value = barcode.rawValue?.trim().orEmpty()
        if (value.isEmpty()) return null
        val format = when (barcode.format) {
            Barcode.FORMAT_EAN_13 -> "EAN-13"
            Barcode.FORMAT_EAN_8 -> "EAN-8"
            Barcode.FORMAT_UPC_A -> "UPC-A"
            Barcode.FORMAT_UPC_E -> "UPC-E"
            Barcode.FORMAT_CODE_128 -> "Code 128"
            Barcode.FORMAT_CODE_39 -> "Code 39"
            Barcode.FORMAT_QR_CODE -> "QR"
            else -> "Other"
        }
        return ScanResult(value, format)
    }
}

/** CameraX analyzer: reads each camera frame with ML Kit and reports the first barcode it sees. */
class BarcodeAnalyzer(private val onFound: (ScanResult) -> Unit) : ImageAnalysis.Analyzer {
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(proxy: ImageProxy) {
        val media = proxy.image
        if (media == null) { proxy.close(); return }
        val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
        BarcodeReader.client.process(image)
            .addOnSuccessListener { list -> list.firstNotNullOfOrNull(BarcodeReader::toResult)?.let(onFound) }
            .addOnCompleteListener { proxy.close() }
    }
}
