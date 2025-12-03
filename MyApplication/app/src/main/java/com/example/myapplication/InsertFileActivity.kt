package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*

class InsertFileActivity : AppCompatActivity() {

    // Activity Result Launcher for selecting a file (PDF, TXT, PPT)
    private lateinit var pickFileLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.insert_file)

        // 1️⃣ Find the "Pick File" button from your XML layout
        val pickFileButton = findViewById<Button>(R.id.pickPdfButton)

        // 2️⃣ Register a launcher that will open the file picker
        pickFileLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            // This callback runs after the user selects a file
            if (uri != null) {
                // Grant your app permission to read the selected file persistently
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                // 3️⃣ Extract file name (like myfile.pdf or notes.txt)
                val fileName = getFileName(uri)

                // 4️⃣ Create a temporary copy of the selected file inside app’s cache folder
                val file = File(cacheDir, fileName)
                val inputStream = contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                // 5️⃣ Upload that file to your Python backend
                uploadFileToServer(file)
            } else {
                // User didn’t select any file
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }

        // 6️⃣ When button is clicked → open the file picker
        pickFileButton.setOnClickListener {
            pickFileLauncher.launch(
                arrayOf(
                    "application/pdf", // PDF
                    "text/plain", // TXT
                    "application/vnd.ms-powerpoint", // PPT
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation" // PPTX
                )
            )
        }



    }

    /**
     * Helper function to get the actual display name of the selected file
     * (Android’s content resolver hides the file name inside metadata)
     */
    private fun getFileName(uri: Uri): String {
        var name = "file"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
        return name
    }

    /**
     * Sends the selected file to a Python backend using an HTTP POST request (multipart form).
     * Uses OkHttp for reliable network calls.
     */
    private fun uploadFileToServer(file: File) {
        // 1️⃣ Create an OkHttp client (used to send requests)
        val client = OkHttpClient()

        // 2️⃣ Create multipart request body with file data
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", // parameter name expected by the Python backend
                file.name, // original file name
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull()) // file content
            )
            .build()

        // 3️⃣ Build the POST request
        val request = Request.Builder()
            .url("") // <-- Replace with your Flask/FastAPI URL
            .post(requestBody)
            .build()

        // 4️⃣ Send the request asynchronously
        client.newCall(request).enqueue(object : Callback {
            // If upload fails (e.g. no internet, server down)
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@InsertFileActivity,
                        "Upload failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // If upload succeeds and server responds
            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string()
                runOnUiThread {
                    Toast.makeText(
                        this@InsertFileActivity,
                        "Server response: $responseText",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }
}
