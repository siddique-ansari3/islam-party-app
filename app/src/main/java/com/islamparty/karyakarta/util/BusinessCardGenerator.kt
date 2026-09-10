package com.islamparty.karyakarta.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.islamparty.karyakarta.data.model.WorkerDetail
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Renders a printable business card (name, phone, designation, city, party branding, photo)
 * for a worker as a PNG and/or single-page PDF, sized like a standard 3.5in x 2in card at 300dpi.
 */
object BusinessCardGenerator {

    private const val CARD_WIDTH_PX = 1050 // 3.5in * 300dpi
    private const val CARD_HEIGHT_PX = 600 // 2in * 300dpi

    fun generateCardBitmap(worker: WorkerDetail, photo: Bitmap?, partyName: String = "Islam Party"): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH_PX, CARD_HEIGHT_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)
        val accentPaint = Paint().apply { color = Color.parseColor("#1B5E20") }
        canvas.drawRect(0f, 0f, CARD_WIDTH_PX.toFloat(), 90f, accentPaint)
        canvas.drawRect(0f, (CARD_HEIGHT_PX - 30).toFloat(), CARD_WIDTH_PX.toFloat(), CARD_HEIGHT_PX.toFloat(), accentPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        canvas.drawText(partyName, 30f, 62f, titlePaint)

        // Photo
        val photoSize = 260
        val photoLeft = 40f
        val photoTop = 140f
        if (photo != null) {
            val scaled = Bitmap.createScaledBitmap(photo, photoSize, photoSize, true)
            canvas.drawBitmap(scaled, photoLeft, photoTop, null)
        } else {
            val placeholder = Paint().apply { color = Color.parseColor("#E0E0E0") }
            canvas.drawRect(photoLeft, photoTop, photoLeft + photoSize, photoTop + photoSize, placeholder)
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B5E20")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(photoLeft, photoTop, photoLeft + photoSize, photoTop + photoSize, borderPaint)

        // Text block
        val nameTextX = photoLeft + photoSize + 40f
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 32f
        }

        var y = 200f
        canvas.drawText(worker.fullName, nameTextX, y, namePaint)
        y += 55f
        if (!worker.designation.isNullOrBlank()) {
            canvas.drawText(worker.designation, nameTextX, y, labelPaint)
            y += 45f
        }
        canvas.drawText("Ph: ${worker.mobileNumber}", nameTextX, y, labelPaint)
        y += 45f
        canvas.drawText(worker.city, nameTextX, y, labelPaint)

        return bitmap
    }

    fun saveAsPng(context: Context, bitmap: Bitmap, fileName: String): File {
        val dir = File(context.cacheDir, "cards").apply { mkdirs() }
        val file = File(dir, "$fileName.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }

    fun saveAsPdf(context: Context, bitmap: Bitmap, fileName: String): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = document.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)

        val dir = File(context.cacheDir, "cards").apply { mkdirs() }
        val file = File(dir, "$fileName.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun uriForFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** Sends an already-rendered PDF file straight to the Android print dialog. */
    fun printPdf(context: Context, pdfFile: File, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: android.os.Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    FileInputStream(pdfFile).use { input ->
                        FileOutputStream(destination?.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
    }
}
