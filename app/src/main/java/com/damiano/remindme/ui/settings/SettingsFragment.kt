package com.damiano.remindme.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.damiano.remindme.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SettingsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var imageView2: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var languageSpinner: Spinner
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val btnSignIn: Button = view.findViewById(R.id.btnSignIn)
        val btnSignUp: Button = view.findViewById(R.id.btnSignUp)
        val btnSignOut: Button = view.findViewById(R.id.btnSignOut)
        val btnDeleteAccount: Button = view.findViewById(R.id.btnDeleteAccount)
        imageView2 = view.findViewById(R.id.imageView2)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        languageSpinner = view.findViewById(R.id.language_spinner)

        sharedPreferences = requireContext().getSharedPreferences("Settings", 0)

        updateAccountIconVisibility()

        btnSignIn.setOnClickListener {
            startActivity(Intent(activity, SignInActivity::class.java))
        }

        btnSignUp.setOnClickListener {
            startActivity(Intent(activity, SignUpActivity::class.java))
        }

        btnSignOut.setOnClickListener {
            auth.signOut()
            updateAccountIconVisibility()
        }

        btnDeleteAccount.setOnClickListener {
            val user = Firebase.auth.currentUser
            user?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(),
                        getString(R.string.account_successfully_deleted), Toast.LENGTH_SHORT).show()
                    updateAccountIconVisibility()
                } else {
                    Log.e("error", task.exception.toString())
                    Toast.makeText(requireContext(),
                        getString(R.string.error_deleting_account, task.exception?.message), Toast.LENGTH_LONG).show()
                }
            }
        }

        // Setup lo spinner per la selezione della lingua
        setupLanguageSpinner()
    }

    private fun updateAccountIconVisibility() {
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            imageView2.visibility = View.GONE
            tvUserEmail.visibility = View.VISIBLE
            tvUserEmail.text = getString(R.string.account, currentUser.email)

            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString(getString(R.string.name))
                        tvUserName.text = getString(R.string.user, name)
                    } else {
                        tvUserName.text =
                            getString(R.string.user_, currentUser.displayName ?: getString(R.string.no_name))
                    }
                }
                .addOnFailureListener {
                    tvUserName.text =
                        getString(R.string.user__, currentUser.displayName ?: getString(R.string.no_name))
                }
        } else {
            imageView2.visibility = View.VISIBLE
            tvUserName.text = getString(R.string.no_user)
            tvUserEmail.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        updateAccountIconVisibility()
    }

    private var previousLanguage: String? = null // Track the previous language selection

    private fun setupLanguageSpinner() {
        // Array delle lingue
        val languages = arrayOf("English", "Deutsch", "Italiano", "Français", "Español", "Português", "中文", "العربية", "Русский")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        // Recupera la lingua salvata dalle SharedPreferences
        val savedLanguage = sharedPreferences.getString("App_Language", "en")
        previousLanguage = savedLanguage // Store the previous language for comparison
        val selectedPosition = when (savedLanguage) {
            "en" -> 0 // English
            "de" -> 1 // Deutsch
            "it" -> 2 // Italiano
            "fr" -> 3 // Français
            "es" -> 4 // Español
            "pt" -> 5 // Português
            "zh" -> 6 // 中文
            "ar" -> 7 // العربية
            "ru" -> 8 // Русский
            else -> 0
        }
        languageSpinner.setSelection(selectedPosition)

        // Gestisce la selezione della lingua
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Determine the newly selected language
                val selectedLanguage = when (position) {
                    0 -> "en" // English
                    1 -> "de" // Deutsch
                    2 -> "it" // Italiano
                    3 -> "fr" // Français
                    4 -> "es" // Español
                    5 -> "pt" // Português
                    6 -> "zh" // 中文
                    7 -> "ar" // العربية
                    8 -> "ru" // Русский
                    else -> "en"
                }

                // Compare with previous selection to determine if a change occurred
                if (selectedLanguage != previousLanguage) {
                    showConfirmationDialog(selectedLanguage) // Show confirmation dialog only on actual change
                } else {
                    previousLanguage = selectedLanguage // Update previous language
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Non fare nulla
            }
        }
    }

    private fun showConfirmationDialog(selectedLanguage: String) {
        // Get the title and messages based on the selected language
        val title = when (selectedLanguage) {
            "de" -> getString(R.string.confirm_language_change_de)
            "it" -> getString(R.string.confirm_language_change_it)
            "en" -> getString(R.string.confirm_language_change_en)
            "fr" -> getString(R.string.confirm_language_change_fr)
            "es" -> getString(R.string.confirm_language_change_es)
            "pt" -> getString(R.string.confirm_language_change_pt)
            "zh" -> getString(R.string.confirm_language_change_zh)
            "ar" -> getString(R.string.confirm_language_change_ar)
            "ru" -> getString(R.string.confirm_language_change_ru)
            else -> getString(R.string.confirm_language_change_en)
        }

        val message = when (selectedLanguage) {
            "de" -> getString(R.string.sprache_ausgew_hlt_deutsch_wird_nach_neustart_angewendet)
            "it" -> getString(R.string.lingua_selezionata_italiano_sar_applicata_al_riavvio)
            "en" -> getString(R.string.language_selected_english_will_be_applied_after_restart)
            "fr" -> getString(R.string.lingua_selezionata_francese_sar_applicata_al_riavvio)
            "es" -> getString(R.string.language_selected_spanish_will_be_applied_after_restart)
            "pt" -> getString(R.string.language_selected_portuguese_will_be_applied_after_restart)
            "zh" -> getString(R.string.language_selected_chinese_will_be_applied_after_restart)
            "ar" -> getString(R.string.language_selected_arabic_will_be_applied_after_restart)
            "ru" -> getString(R.string.language_selected_russian_will_be_applied_after_restart)
            else -> getString(R.string.language_selected_english_will_be_applied_after_restart)
        }

        val confirmText = when (selectedLanguage) {
            "de" -> getString(R.string.confirm_de)
            "it" -> getString(R.string.confirm_it)
            "en" -> getString(R.string.confirm_en)
            "fr" -> getString(R.string.confirm_fr)
            "es" -> getString(R.string.confirm_es)
            "pt" -> getString(R.string.confirm_pt)
            "zh" -> getString(R.string.confirm_zh)
            "ar" -> getString(R.string.confirm_ar)
            "ru" -> getString(R.string.confirm_ru)
            else -> getString(R.string.confirm_en)
        }

        val cancelText = when (selectedLanguage) {
            "de" -> getString(R.string.cancel_de)
            "it" -> getString(R.string.cancel_it)
            "en" -> getString(R.string.cancel_en)
            "fr" -> getString(R.string.cancel_fr)
            "es" -> getString(R.string.cancel_es)
            "pt" -> getString(R.string.cancel_pt)
            "zh" -> getString(R.string.cancel_zh)
            "ar" -> getString(R.string.cancel_ar)
            "ru" -> getString(R.string.cancel_ru)
            else -> getString(R.string.cancel_en)
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(confirmText) { _, _ ->
                // Save the selected language in SharedPreferences
                with(sharedPreferences.edit()) {
                    putString("App_Language", selectedLanguage)
                    putBoolean("LanguageChanged", true)  // Indicate that the language has changed
                    apply()
                }

                // Close the app
                requireActivity().finishAffinity() // Close all activities
            }
            .setNegativeButton(cancelText) { dialog, _ ->
                dialog.dismiss() // Close the dialog
                // Restore the previous selection
                restorePreviousSelection()
            }
        builder.create().show()
    }


    private fun restorePreviousSelection() {
        // Recupera la lingua salvata dalle SharedPreferences
        val savedLanguage = sharedPreferences.getString("App_Language", "en")
        val selectedPosition = when (savedLanguage) {
            "en" -> 0 // English
            "de" -> 1 // Deutsch
            "it" -> 2 // English
            "fr" -> 3 // Français
            "es" -> 4 // Español
            "pt" -> 5 // Português
            "zh" -> 6 // 中文
            "ar" -> 7 // العربية
            "ru" -> 8 // Русский
            else -> 0
        }
        languageSpinner.setSelection(selectedPosition) // Ripristina la selezione precedente
    }

}