package com.rsservice.muconnect.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rsservice.muconnect.R

class CreateClassActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_class)

        val etTitle = findViewById<TextInputEditText>(R.id.etClassTitle)
        val etSem = findViewById<TextInputEditText>(R.id.etSemester)
        val etBatch = findViewById<TextInputEditText>(R.id.etBatch)
        val fabSave = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabSave)
        val loadingOverlay = findViewById<View>(R.id.loadingOverlay)

        fabSave.bringToFront()

        fabSave.setOnClickListener {

            val title = etTitle.text.toString().trim()
            val sem = etSem.text.toString().trim()
            val batch = etBatch.text.toString().trim()

            if (title.isEmpty() || sem.isEmpty() || batch.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loadingOverlay.visibility = View.VISIBLE

            val classRef = firestore.collection("classes").document(title)

            // 1️⃣ Ensure class document exists (no overwrite issue)
            classRef.set(
                mapOf(
                    "title" to title,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )

            // 2️⃣ Add batch under subcollection
            val batchData = hashMapOf(
                "semester" to sem,
                "batch" to batch,
                "createdAt" to FieldValue.serverTimestamp()
            )

            classRef.collection("batches")
                .document(batch) // batch as unique key
                .set(batchData)
                .addOnSuccessListener {
                    loadingOverlay.visibility = View.GONE
                    Toast.makeText(this, "Batch added to $title", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    loadingOverlay.visibility = View.GONE
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
