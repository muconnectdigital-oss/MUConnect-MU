package com.rsservice.muconnect.auth

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rsservice.muconnect.R
import com.rsservice.muconnect.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val ALLOWED_DOMAIN = "@marwadiuniversity.ac.in"
    }

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var loadingDialog: Dialog

    private var userRole = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupLoader()
        setupTextWatchers()

        binding.btnRegister.setOnClickListener {
            hideKeyboard()
            registerUser()
        }
    }

    // --------------------------------------------------
    // Loader
    // --------------------------------------------------
    private fun setupLoader() {
        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.dialog_loade)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showLoader() {
        if (!loadingDialog.isShowing) loadingDialog.show()
    }

    private fun hideLoader() {
        if (loadingDialog.isShowing) loadingDialog.dismiss()
    }

    // --------------------------------------------------
    // Keyboard
    // --------------------------------------------------
    private fun hideKeyboard() {
        val view = currentFocus
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    // --------------------------------------------------
    // Text Watchers
    // --------------------------------------------------
    private fun setupTextWatchers() {

        binding.etId.addTextChangedListener {
            detectRole()
            validateFields()
        }

        binding.etName.addTextChangedListener { validateFields() }
        binding.etPhone.addTextChangedListener { validateFields() }
        binding.etDob.addTextChangedListener { validateFields() }

        binding.etEmail.addTextChangedListener {
            val email = it.toString().trim()

            if (email.isNotEmpty() && !email.endsWith(ALLOWED_DOMAIN)) {
                binding.etEmail.error =
                    "Only @marwadiuniversity.ac.in email allowed"
            } else {
                binding.etEmail.error = null
            }

            validateFields()
        }
    }

    // --------------------------------------------------
    // Validate Fields
    // --------------------------------------------------
    private fun validateFields() {
        val email = binding.etEmail.text.toString().trim()

        val valid =
            binding.etId.text.isNotEmpty() &&
                    userRole.isNotEmpty() &&
                    binding.etName.text.isNotEmpty() &&
                    email.endsWith(ALLOWED_DOMAIN) &&
                    binding.etPhone.text.length == 10 &&
                    binding.etDob.text.length == 8

        binding.btnRegister.isEnabled = valid
        binding.btnRegister.alpha = if (valid) 1f else 0.5f
    }

    // --------------------------------------------------
    // Role Detection
    // --------------------------------------------------
    private fun detectRole() {
        val id = binding.etId.text.toString().trim().uppercase()

        when {
            id.startsWith("S") -> {
                userRole = ""
                binding.etName.visibility = View.GONE
                binding.tvStudentNotAllowed.visibility = View.VISIBLE
            }

            id.startsWith("T") -> {
                userRole = "teachers"
                binding.etName.visibility = View.VISIBLE
                binding.tvStudentNotAllowed.visibility = View.GONE
            }

            id.startsWith("A") -> {
                userRole = "admins"
                binding.etName.visibility = View.VISIBLE
                binding.tvStudentNotAllowed.visibility = View.GONE
            }

            else -> {
                userRole = ""
                binding.etName.visibility = View.GONE
                binding.tvStudentNotAllowed.visibility = View.GONE
            }
        }
    }

    // --------------------------------------------------
    // Firebase Registration
    // --------------------------------------------------
    private fun registerUser() {
        val email = binding.etEmail.text.toString().trim()

        if (!email.endsWith(ALLOWED_DOMAIN)) {
            toast("Use official Marwadi University email")
            return
        }

        val password = binding.etDob.text.toString().trim()
        val id = binding.etId.text.toString().trim().uppercase()

        showLoader()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                saveUserProfile(result.user!!.uid, id)
                result.user!!.sendEmailVerification()
            }
            .addOnFailureListener {
                hideLoader()
                toast(it.message ?: "Registration failed")
            }
    }

    // --------------------------------------------------
    // Save User Profile
    // --------------------------------------------------
    private fun saveUserProfile(uid: String, id: String) {

        val userMap = hashMapOf(
            "uid" to uid,
            "id" to id,
            "name" to binding.etName.text.toString().trim(),
            "email" to binding.etEmail.text.toString().trim(),
            "phone" to binding.etPhone.text.toString().trim(),
            "dob" to binding.etDob.text.toString().trim(),
            "role" to userRole,
            "isActive" to true
        )

        db.collection("users")
            .document(userRole)
            .collection("records")
            .document(id)
            .set(userMap)
            .addOnSuccessListener {
                hideLoader()
                toast("Registration successful. Verify your email.")
                finish()
            }
            .addOnFailureListener {
                hideLoader()
                toast("Failed to save user data")
            }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
