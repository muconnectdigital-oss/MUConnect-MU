package com.rsservice.muconnect.ui.admin

import android.content.Intent
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.rsservice.muconnect.R
import androidx.appcompat.app.AppCompatActivity
class EditStudentActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        val grNo = intent.getStringExtra("grNo") ?: return

        firestore.collection("students").document(grNo)
            .get()
            .addOnSuccessListener {
                // populate fields, update on save
            }
    }
}
