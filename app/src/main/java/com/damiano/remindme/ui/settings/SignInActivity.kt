package com.damiano.remindme.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.damiano.remindme.R
import com.damiano.remindme.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        auth = Firebase.auth

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (checkAllField()) {
                signInUser(email, password)
            }
        }
    }

    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, getString(R.string.successfully_signed_in), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Log.e("error", task.exception.toString())
                Toast.makeText(this,
                    getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAllField(): Boolean {
        val email = binding.etEmail.text.toString()
        if (email.isEmpty()) {
            binding.textInputLayoutEmail.error = getString(R.string.this_is_required_field)
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayoutEmail.error = getString(R.string.check_email_format)
            return false
        }

        if (binding.etPassword.text.toString().isEmpty()) {
            binding.textInputLayoutPassword.error = getString(R.string.this_is_required_field)
            binding.textInputLayoutPassword.errorIconDrawable = null
            return false
        }

        if (binding.etPassword.length() <= 6) {
            binding.textInputLayoutPassword.error =
                getString(R.string.password_should_at_least_6_characters_long)
            binding.textInputLayoutPassword.errorIconDrawable = null
            return false
        }

        return true
    }
}
