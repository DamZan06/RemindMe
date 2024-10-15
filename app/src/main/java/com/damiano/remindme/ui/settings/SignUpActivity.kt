package com.damiano.remindme.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.damiano.remindme.R
import com.damiano.remindme.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val name = binding.etName.text.toString()

            if (checkAllFields()) {
                createUser(email, password, name)
            }
        }
    }

    private fun createUser(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                    if (profileTask.isSuccessful) {
                        val userId = user.uid
                        val db = FirebaseFirestore.getInstance()
                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email
                        )

                        Toast.makeText(this,
                            getString(R.string.neues_konto_erfolgreich_erstellt), Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        showErrorToast(
                            getString(
                                R.string.fehler_beim_aktualisieren_des_profils,
                                profileTask.exception?.message
                            ))
                    }
                }
            } else {
                Log.e("error", task.exception.toString())
                showErrorToast(
                    getString(
                        R.string.registrierung_fehlgeschlagen,
                        task.exception?.message
                    ))
            }
        }
    }

    private fun checkAllFields(): Boolean {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val name = binding.etName.text.toString()

        return when {
            name.isEmpty() -> {
                binding.textInputLayoutName.error = getString(R.string.name_erforderlich)
                false
            }
            email.isEmpty() -> {
                binding.textInputLayoutEmail.error = getString(R.string.e_mail_feld_erforderlich)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.textInputLayoutEmail.error = getString(R.string.ung_ltiges_e_mail_format)
                false
            }
            password.isEmpty() -> {
                binding.textInputLayoutPassword.error =
                    getString(R.string.passwortfeld_erforderlich)
                false
            }
            password.length < 6 -> {
                binding.textInputLayoutPassword.error =
                    getString(R.string.das_passwort_muss_mindestens_6_zeichen_lang_sein)
                false
            }
            confirmPassword.isEmpty() -> {
                binding.textInputLayoutConfirmPassword.error =
                    getString(R.string.best_tigungsfeld_f_r_das_passwort_erforderlich)
                false
            }
            password != confirmPassword -> {
                binding.textInputLayoutPassword.error =
                    getString(R.string.die_passw_rter_stimmen_nicht_berein)
                false
            }
            else -> true
        }
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
