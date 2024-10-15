package com.damiano.remindme.business

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.damiano.remindme.R

object DialogUtils {
    fun showDeleteConfirmationDialog(context: Context, onConfirm: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.l_schbest_tigung))
            .setMessage(context.getString(R.string.bist_du_sicher_dass_du_dieses_ereignis_l_schen_m_chtest))
            .setPositiveButton(context.getString(R.string.ja)) { _, _ ->
                onConfirm()
            }
            .setNegativeButton(context.getString(R.string.nein), null)
            .show()
    }
}
