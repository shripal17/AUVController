package com.library.utils.ui

import android.content.Context
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeErrorDialog
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog
import com.library.utils.R

/**
 * Created by shripal17 on 29/10/17.
 */
object AlertUtil {

  var color = R.color.colorPrimaryDark

  object ProgressAlert {
    fun show(ctx: Context, title: String = "Please wait", message: String = "Loading...", showNow: Boolean = true): AwesomeProgressDialog {
      val dialog = AwesomeProgressDialog(ctx)
          .setTitle(title)
          .setColoredCircle(color)
          .setMessage(message)
          .setCancelable(false)
      if (showNow) {
        dialog.show()
      }
      return dialog
    }

    fun hide(dialog: AwesomeProgressDialog) = dialog.hide()
  }

  object ErrorAlert {
    fun show(ctx: Context, title: String, message: String, button: String = "Ok", onButtonClick: () -> Unit = { }, showNow: Boolean = true): AwesomeErrorDialog {
      val dialog = AwesomeErrorDialog(ctx)
          .setTitle(title)
          .setMessage(message)
          .setColoredCircle(color)
          .setButtonBackgroundColor(color)
          .setCancelable(false)
          .setButtonText(button)
          .setErrorButtonClick({
            onButtonClick()
          })
      if (showNow) {
        dialog.show()
      }
      return dialog
    }

    fun hide(dialog: AwesomeErrorDialog) = dialog.hide()

  }

  object SuccessAlert {
    fun show(ctx: Context, title: String, message: String, doneButton: String = "Ok", onButtonClick: () -> Unit = { }, showNow: Boolean = true): AwesomeSuccessDialog {
      val dialog = AwesomeSuccessDialog(ctx)
          .setTitle(title)
          .setMessage(message)
          .setCancelable(false)
          .setColoredCircle(color)
          .setDoneButtonbackgroundColor(color)
          .setDoneButtonText(doneButton)
          .setDoneButtonClick {
            onButtonClick()
          }
      if (showNow) {
        dialog.show()
      }
      return dialog
    }

    fun hide(dialog: AwesomeSuccessDialog) = dialog.hide()
  }
}