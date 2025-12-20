package com.rsservice.muconnect.auth

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rsservice.muconnect.databinding.ActivitySplashBinding
import com.rsservice.muconnect.ui.admin.AdminDashboardActivity
import com.rsservice.muconnect.ui.student.StudentDashboardActivity
import com.rsservice.muconnect.ui.teacher.TeacherDashboardActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playCinematicAnimation()
    }

    private fun playCinematicAnimation() {

        // 1️⃣ Background fade-in
        binding.rootLayout.animate()
            .alpha(1f)
            .setDuration(700)
            .start()

        // 2️⃣ Glow appear
        binding.orbitGlow.animate()
            .alpha(0.8f)
            .setDuration(700)
            .setStartDelay(500)
            .start()

        // 3️⃣ Rotating glow ring
        binding.orbitGlow.animate()
            .rotationBy(360f)
            .setDuration(2800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // 4️⃣ Shield pop-in bounce
        binding.shieldBg.apply {
            alpha = 0f
            scaleX = 0.3f
            scaleY = 0.3f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(900)
                .setStartDelay(600)
                .setInterpolator(BounceInterpolator())
                .start()
        }

        // 5️⃣ Shine sweep
        binding.shineView.animate()
            .alpha(1f)
            .translationX(300f)
            .setDuration(600)
            .setStartDelay(1200)
            .withEndAction {
                binding.shineView.alpha = 0f
                binding.shineView.translationX = 0f
            }
            .start()

        // 6️⃣ MU fade-in
        binding.muText.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(900)
            .start()

        // 7️⃣ MARWADI UNIVERSITY fade-in
        binding.marwadiText.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(1050)
            .start()

        // 8️⃣ MU CONNECT reveal
        binding.logoText.animate()
            .alpha(1f)
            .translationYBy(-20f)
            .setDuration(700)
            .setStartDelay(1300)
            .start()

        // 9️⃣ AFTER ANIMATION → SESSION CHECK
        binding.rootLayout.postDelayed({
            handleSession()
        }, 2600)
    }

    // --------------------------------------------------
    // SESSION HANDLER
    // --------------------------------------------------
    private fun handleSession() {
        val user = auth.currentUser

        if (user == null || !user.isEmailVerified) {
            goToLogin()
            return
        }

        restoreSession(user.uid)
    }

    private fun restoreSession(uid: String) {
        val roles = listOf("admins", "teachers", "students")

        fun check(index: Int) {
            if (index >= roles.size) {
                auth.signOut()
                goToLogin()
                return
            }

            val role = roles[index]

            db.collection("users")
                .document(role)
                .collection("records")
                .whereEqualTo("uid", uid)
                .limit(1)
                .get()
                .addOnSuccessListener { snap ->
                    if (!snap.isEmpty) {
                        redirect(role)
                    } else {
                        check(index + 1)
                    }
                }
                .addOnFailureListener {
                    goToLogin()
                }
        }

        check(0)
    }

    private fun redirect(role: String) {
        val intent = when (role) {
            "admins" -> Intent(this, AdminDashboardActivity::class.java)
            "teachers" -> Intent(this, TeacherDashboardActivity::class.java)
            "students" -> Intent(this, StudentDashboardActivity::class.java)
            else -> Intent(this, LoginActivity::class.java)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}
