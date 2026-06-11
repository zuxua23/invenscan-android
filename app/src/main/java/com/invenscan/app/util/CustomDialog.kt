package com.invenscan.app.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.invenscan.app.R

object CustomDialog {

    fun show(
        context: Context,
        title: String,
        message: String,
        positiveText: String = context.getString(R.string.dialog_yes),
        negativeText: String? = context.getString(R.string.dialog_no),
        onPositive: () -> Unit,
        onNegative: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.tvDialogTitle).text = title
        view.findViewById<TextView>(R.id.tvDialogMessage).text = message

        val btnPositive = view.findViewById<MaterialButton>(R.id.btnDialogPositive)
        val btnNegative = view.findViewById<MaterialButton>(R.id.btnDialogNegative)

        btnPositive.text = positiveText
        btnPositive.setOnClickListener {
            dialog.dismiss()
            onPositive()
        }

        if (negativeText != null) {
            btnNegative.text = negativeText
            btnNegative.setOnClickListener {
                dialog.dismiss()
                onNegative?.invoke()
            }
        } else {
            btnNegative.visibility = android.view.View.GONE
        }

        dialog.show()
    }
}
