package com.invenscan.app.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.invenscan.app.R

object CustomToast {

    enum class Type { SUCCESS, ERROR, WARNING, INFO }

    fun show(context: Context, message: String, type: Type = Type.INFO) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_custom, null)

        layout.findViewById<TextView>(R.id.tvToastMessage).text = message

        val iconView = layout.findViewById<ImageView>(R.id.ivToastIcon)
        val container = layout.findViewById<android.view.View>(R.id.toastContainer)

        when (type) {
            Type.SUCCESS -> {
                container.setBackgroundResource(R.drawable.bg_toast_success)
                iconView.setImageResource(R.drawable.ic_check_circle)
            }
            Type.ERROR -> {
                container.setBackgroundResource(R.drawable.bg_toast_error)
                iconView.setImageResource(R.drawable.ic_error_circle)
            }
            Type.WARNING -> {
                container.setBackgroundResource(R.drawable.bg_toast_warning)
                iconView.setImageResource(R.drawable.ic_warning_circle)
            }
            Type.INFO -> {
                container.setBackgroundResource(R.drawable.bg_toast_info)
                iconView.setImageResource(R.drawable.ic_info_circle)
            }
        }

        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
        }.show()
    }
}
