package com.rsservice.muconnect.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rsservice.muconnect.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { loginUser() }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun loginUser() {
        val id = binding.etUsername.text.toString().trim().uppercase()
        val password = binding.etPassword.text.toString().trim()

        if (id.isEmpty() || password.isEmpty()) {
            toast("Enter all fields")
            return
        }

        val role = when {
            id.startsWith("A") -> "admins"
            id.startsWith("T") -> "teachers"
            id.startsWith("S") -> "students"
            else -> {
                toast("Invalid ID format")
                return
            }
        }

        db.collection("users")
            .document(role)
            .collection("records")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    toast("User not found")
                    return@addOnSuccessListener
                }

                val email = doc.getString("email") ?: run {
                    toast("Email missing")
                    return@addOnSuccessListener
                }

                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val user = result.user!!

                        if (!user.isEmailVerified) {
                            toast("Please verify your email")
                            auth.signOut()
                            return@addOnSuccessListener
                        }

                        // Save UID for session restore
                        doc.reference.update("uid", user.uid)

                        // Go back to Splash for routing
                        startActivity(Intent(this, SplashActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        toast("Invalid credentials")
                    }
            }
            .addOnFailureListener {
                toast("Database error")
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
