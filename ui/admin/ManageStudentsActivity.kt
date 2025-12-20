package com.rsservice.muconnect.ui.admin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.rsservice.muconnect.R

class ManageStudentsActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    private val allStudents = mutableListOf<Student>()   // 🔑 source of truth
    private val filteredStudents = mutableListOf<Student>()

    private lateinit var adapter: StudentAdapter

    private val classList = mutableListOf("All")
    private val batchList = mutableListOf("All")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_students)

        val rv = findViewById<RecyclerView>(R.id.rvStudents)
        val spinnerClass = findViewById<Spinner>(R.id.spinnerFilterClass)
        val spinnerBatch = findViewById<Spinner>(R.id.spinnerFilterBatch)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        adapter = StudentAdapter(filteredStudents) { student ->
            val i = Intent(this, AddStudentActivity::class.java)
            i.putExtra("MODE", "EDIT")
            i.putExtra("GR_NO", student.grNo)
            startActivity(i)
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadClasses(spinnerClass, spinnerBatch)
        loadStudents()

        // 🔄 FILTER CHANGE
        spinnerClass.onItemSelectedListener = simpleListener {
            applyFilters(
                spinnerClass.selectedItem.toString(),
                spinnerBatch.selectedItem.toString(),
                etSearch.text.toString()
            )
        }

        spinnerBatch.onItemSelectedListener = simpleListener {
            applyFilters(
                spinnerClass.selectedItem.toString(),
                spinnerBatch.selectedItem.toString(),
                etSearch.text.toString()
            )
        }

        // 🔍 SEARCH
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                applyFilters(
                    spinnerClass.selectedItem.toString(),
                    spinnerBatch.selectedItem.toString(),
                    s.toString()
                )
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        // 🗑️ SWIPE DELETE
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val student = filteredStudents[viewHolder.adapterPosition]

                MaterialAlertDialogBuilder(this@ManageStudentsActivity)
                    .setTitle("Delete Student")
                    .setMessage(
                        "Delete this student?\n\n" +
                                "Name: ${student.fullName}\n" +
                                "GR No: ${student.grNo}"
                    )
                    .setPositiveButton("Delete") { _, _ ->
                        firestore.collection("students")
                            .document(student.grNo)
                            .delete()
                            .addOnSuccessListener { loadStudents() }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                    }
                    .setCancelable(false)
                    .show()
            }
        }).attachToRecyclerView(rv)
    }

    override fun onResume() {
        super.onResume()
        loadStudents()
    }

    // ---------- LOAD STUDENTS ----------
    private fun loadStudents() {
        firestore.collection("students")
            .get()
            .addOnSuccessListener {
                allStudents.clear()
                allStudents.addAll(it.toObjects(Student::class.java))

                applyFilters("All", "All", "")
            }
    }

    // ---------- APPLY FILTERS ----------
    private fun applyFilters(
        selectedClass: String,
        selectedBatch: String,
        searchText: String
    ) {
        filteredStudents.clear()

        filteredStudents.addAll(
            allStudents.filter {

                val classOk =
                    selectedClass == "All" || it. `class` == selectedClass

                val batchOk =
                    selectedBatch == "All" || it.batch == selectedBatch

                val searchOk =
                    searchText.isBlank() ||
                            it.fullName.contains(searchText, true) ||
                            it.grNo.contains(searchText, true)

                classOk && batchOk && searchOk
            }
        )

        adapter.notifyDataSetChanged()
    }

    // ---------- LOAD CLASSES ----------
    private fun loadClasses(classSpinner: Spinner, batchSpinner: Spinner) {
        firestore.collection("classes")
            .get()
            .addOnSuccessListener {
                classList.clear()
                classList.add("All")
                for (d in it) classList.add(d.id)

                classSpinner.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    classList
                )

                classSpinner.onItemSelectedListener =
                    simpleListener {
                        loadBatches(
                            classSpinner.selectedItem.toString(),
                            batchSpinner
                        )
                    }
            }
    }

    // ---------- LOAD BATCHES ----------
    private fun loadBatches(classId: String, batchSpinner: Spinner) {

        batchList.clear()
        batchList.add("All")

        if (classId == "All") {
            batchSpinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                batchList
            )
            return
        }

        firestore.collection("classes")
            .document(classId)
            .collection("batches")
            .get()
            .addOnSuccessListener {
                for (d in it) batchList.add(d.id)

                batchSpinner.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    batchList
                )
            }
    }

    private fun simpleListener(onChange: () -> Unit) =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) = onChange()

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
}
