package com.islamparty.karyakarta.ui.screens.businesscard

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.imageLoader
import coil.request.ImageRequest
import com.islamparty.karyakarta.data.repository.WorkerRepository
import com.islamparty.karyakarta.util.BusinessCardGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessCardScreen(workerId: String, repository: WorkerRepository, baseUrl: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cardBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(workerId) {
        try {
            val worker = repository.getWorker(workerId)
            val photoBitmap: Bitmap? = worker.photoUrl?.let { path ->
                val request = ImageRequest.Builder(context).data(baseUrl.trimEnd('/') + path).build()
                val result = context.imageLoader.execute(request)
                (result.drawable as? BitmapDrawable)?.bitmap
            }
            cardBitmap = BusinessCardGenerator.generateCardBitmap(worker, photoBitmap)
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to generate business card"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Card") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                errorMessage != null -> Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                cardBitmap == null -> CircularProgressIndicator()
                else -> {
                    Image(bitmap = cardBitmap!!.asImageBitmap(), contentDescription = "Business card preview")

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                        Button(onClick = {
                            scope.launch {
                                val file = BusinessCardGenerator.saveAsPdf(context, cardBitmap!!, "card_$workerId")
                                sharePdf(context, file)
                            }
                        }) { Text("Share PDF") }

                        Button(onClick = {
                            scope.launch {
                                val file = BusinessCardGenerator.saveAsPng(context, cardBitmap!!, "card_$workerId")
                                sharePng(context, file)
                            }
                        }, modifier = Modifier.padding(start = 8.dp)) { Text("Share PNG") }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val file = BusinessCardGenerator.saveAsPdf(context, cardBitmap!!, "card_$workerId")
                                BusinessCardGenerator.printPdf(context, file, "Business Card")
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) { Text("Print") }
                }
            }
        }
    }
}

private fun sharePdf(context: android.content.Context, file: java.io.File) {
    val uri = BusinessCardGenerator.uriForFile(context, file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share business card"))
}

private fun sharePng(context: android.content.Context, file: java.io.File) {
    val uri = BusinessCardGenerator.uriForFile(context, file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share business card"))
}
