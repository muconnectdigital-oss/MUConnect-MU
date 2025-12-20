package com.rsservice.muconnect.ui.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rsservice.muconnect.R
import java.text.SimpleDateFormat
import java.util.*

class AddStudentActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    private val classList = mutableListOf<String>()
    private val batchList = mutableListOf<String>()

    private var isEditMode = false
    private var editGrNo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        // -------- VIEWS --------
        val etGrNo = findViewById<TextInputEditText>(R.id.etGrNo)
        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etDob = findViewById<TextInputEditText>(R.id.etDob)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)

        val spinnerClass = findViewById<Spinner>(R.id.spinnerClass)
        val spinnerBatch = findViewById<Spinner>(R.id.spinnerBatch)

        val fabSave = findViewById<FloatingActionButton>(R.id.fabSave)

        // -------- MODE CHECK --------
        isEditMode = intent.getStringExtra("MODE") == "EDIT"
        editGrNo = intent.getStringExtra("GR_NO")

        if (isEditMode) {
            title = "Edit Student"
            etGrNo.isEnabled = false // 🔒 primary key
        }

        // -------- DOB PICKER --------
        etDob.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date of Birth")
                .build()

            picker.show(supportFragmentManager, "DOB_PICKER")

            picker.addOnPositiveButtonClickListener {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                etDob.setText(sdf.format(Date(it)))
            }
        }

        // -------- LOAD CLASS + BATCH --------
        loadClasses(spinnerClass, spinnerBatch) {
            if (isEditMode && editGrNo != null) {
                loadStudentForEdit(
                    editGrNo!!,
                    etGrNo, etName, etDob, etEmail, etPhone,
                    spinnerClass, spinnerBatch
                )
            }
        }

        // -------- SAVE --------
        fabSave.setOnClickListener {

            val grNo = etGrNo.text.toString().trim()
            val name = etName.text.toString().trim()
            val dob = etDob.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            val className = spinnerClass.selectedItem?.toString() ?: ""
            val batch = spinnerBatch.selectedItem?.toString() ?: ""

            if (grNo.isEmpty() || name.isEmpty() || dob.isEmpty()) {
                Toast.makeText(this, "Required fields missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val studentData = hashMapOf(
                "grNo" to grNo,
                "fullName" to name,
                "dob" to dob,
                "class" to className,
                "batch" to batch,
                "email" to email,
                "phone" to phone,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("students")
                .document(grNo)
                .set(studentData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        if (isEditMode) "Student updated" else "Student added",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }
    }

    // -------- LOAD STUDENT FOR EDIT --------
    private fun loadStudentForEdit(
        grNo: String,
        etGrNo: EditText,
        etName: EditText,
        etDob: EditText,
        etEmail: EditText,
        etPhone: EditText,
        spinnerClass: Spinner,
        spinnerBatch: Spinner
    ) {
        firestore.collection("students")
            .document(grNo)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                etGrNo.setText(doc.getString("grNo"))
                etName.setText(doc.getString("fullName"))
                etDob.setText(doc.getString("dob"))
                etEmail.setText(doc.getString("email"))
                etPhone.setText(doc.getString("phone"))

                val className = doc.getString("class") ?: ""
                val batch = doc.getString("batch") ?: ""

                spinnerClass.setSelection(classList.indexOf(className))
                loadBatches(className, spinnerBatch) {
                    spinnerBatch.setSelection(batchList.indexOf(batch))
                }
            }
    }

    // -------- FETCH CLASSES --------
    private fun loadClasses(
        spinnerClass: Spinner,
        spinnerBatch: Spinner,
        onLoaded: () -> Unit
    ) {
        firestore.collection("classes")
            .get()
            .addOnSuccessListener {
                classList.clear()
                for (d in it) classList.add(d.id)

                spinnerClass.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    classList
                )

                spinnerClass.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p: AdapterView<*>, v: android.view.View?,
                            pos: Int, id: Long
                        ) {
                            loadBatches(classList[pos], spinnerBatch)
                        }

                        override fun onNothingSelected(p: AdapterView<*>) {}
                    }

                onLoaded()
            }
    }

    // -------- FETCH BATCHES --------
    private fun loadBatches(
        classId: String,
        spinnerBatch: Spinner,
        onLoaded: (() -> Unit)? = null
    ) {
        firestore.collection("classes")
            .document(classId)
            .collection("batches")
            .get()
            .addOnSuccessListener {
                batchList.clear()
                for (d in it) batchList.add(d.id)

                spinnerBatch.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    batchList
                )

                onLoaded?.invoke()
            }
    }
}
