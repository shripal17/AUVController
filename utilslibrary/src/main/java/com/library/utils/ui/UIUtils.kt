package com.library.utils.ui

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object UIUtils {
  fun setStatusBarTranslucent(activity: Activity, makeTranslucent: Boolean) {
    val window = activity.window
    if (makeTranslucent) {
      window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
      window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
    } else {
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
  }

  fun hideKeyboard(activity: Activity) {
    val v = activity.currentFocus
    getIMM(activity).hideSoftInputFromWindow(v.windowToken, 0)
  }

  fun showKeyboard(editText: EditText) {
    editText.requestFocus()
    getIMM(editText.context).showSoftInput(editText, InputMethodManager.SHOW_FORCED)
  }

  fun getIMM(ctx: Context) = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
}