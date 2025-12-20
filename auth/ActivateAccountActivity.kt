package com.rsservice.muconnect.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.rsservice.muconnect.databinding.ActivityActivateAccountBinding
import java.util.concurrent.TimeUnit

class ActivateAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActivateAccountBinding
    private val db = FirebaseFirestore.getInstance()
    private var verificationId: String? = null
    private var pendingUserDocRef: DocumentReference? = null
    private var pendingRole: String? = null  // "students" / "teachers"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActivateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSendOtp.setOnClickListener { startActivation() }
        binding.btnVerifyOtp.setOnClickListener { verifyOtpAndActivate() }
    }

    private fun startActivation() {
        val id = binding.etId.text.toString().trim().uppercase()
        val dob = binding.etDob.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (id.isEmpty() || dob.isEmpty() || phone.isEmpty()) {
            toast("Enter all fields")
            return
        }

        // ROLE DETECTION
        val role = when {
            id.startsWith("S") -> "students"
            id.startsWith("T") -> "teachers"
            id.startsWith("A") -> "admins"
            else -> {
                toast("Invalid ID format")
                return
            }
        }
        pendingRole = role

        // FIRESTORE USER PATH
        val docRef = db.collection("users")
            .document(role)
            .collection("records")
            .document(id)

        docRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                toast("Details not found")
                return@addOnSuccessListener
            }

            val storedDob = doc.getString("dob") ?: ""
            val storedPhone = doc.getString("phone") ?: ""

            if (storedDob != dob || storedPhone != phone) {
                toast("Details not found")
                return@addOnSuccessListener
            }

            pendingUserDocRef = docRef
            sendOtp(phone)

        }.addOnFailureListener {
            toast("Failed to verify details")
        }
    }

    private fun sendOtp(phone: String) {
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(cred: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(cred, auto = true)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    toast("OTP send failed: ${e.message}")
                }

                override fun onCodeSent(
                    verId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = verId
                    toast("OTP sent successfully")
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyOtpAndActivate() {
        val code = binding.etOtp.text.toString().trim()
        val verId = verificationId

        if (verId.isNullOrEmpty() || code.isEmpty()) {
            toast("Enter OTP")
            return
        }

        val credential = PhoneAuthProvider.getCredential(verId, code)
        signInWithPhoneAuthCredential(credential, auto = false)
    }

    private fun signInWithPhoneAuthCredential(cred: PhoneAuthCredential, auto: Boolean) {
        FirebaseAuth.getInstance().signInWithCredential(cred)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // MARK USER ACTIVE
                    pendingUserDocRef?.update("isActive", true)

                    toast("Account Activated Successfully")

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()

                } else {
                    toast("OTP verification failed")
                }
            }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
