package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File

class generate : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate)

        val messageView = findViewById<TextView>(R.id.pdfReader)

        // Get extras from previous activity
        val filePath = intent.getStringExtra("uploaded_file_path")
        val fileName = intent.getStringExtra("uploaded_file_name")
        val serverMessage = intent.getStringExtra("response_message")

        // Initialize PDFBox for Android
        com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(applicationContext)

        if (filePath != null && filePath.endsWith(".pdf", ignoreCase = true)) {
            try {
                val file = File(filePath)
                val document = PDDocument.load(file)

                // Extract text from PDF
                val stripper = PDFTextStripper()
                val text = stripper.getText(document)
                document.close()

                messageView.text = "✅ File: $fileName\n\n$serverMessage\n\n📄 PDF content:\n\n$text"
            } catch (e: Exception) {
                messageView.text = "Error reading PDF: ${e.message}"
            }
        } else {
            messageView.text = "This is not a PDF file or file path missing."
        }
    }
}
