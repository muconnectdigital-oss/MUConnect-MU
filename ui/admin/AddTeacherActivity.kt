package com.rsservice.muconnect.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.rsservice.muconnect.R

class AddTeacherActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_teacher)

        val etEmpNo = findViewById<TextInputEditText>(R.id.etEmpNo)
        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val fabSave = findViewById<FloatingActionButton>(R.id.fabSave)

        fabSave.setOnClickListener {

            val empNo = etEmpNo.text.toString().trim()
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (empNo.isEmpty() || name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "EMP No, Name and Email are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val teacherData = hashMapOf(
                "empNo" to empNo,
                "fullName" to name,
                "email" to email,
                "phone" to phone
            )

            // 🔑 EMP No as PRIMARY KEY
            database.child("teachers").child(empNo)
                .setValue(teacherData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Teacher Added Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
