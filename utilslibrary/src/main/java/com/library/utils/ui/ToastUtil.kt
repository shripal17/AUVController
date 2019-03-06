package com.library.utils.ui

import android.content.Context
import org.jetbrains.anko.longToast

/**
 * Created by shripal17 on 29/10/17.
 */
object ToastUtil {
  object Success {
    fun show(ctx: Context, message: String = "Success") {
      /*val coolToast = CoolToast(ctx)
      coolToast.setStyle(CoolToast.SUCCESS)
      coolToast.setDuration(CoolToast.LONG)
      coolToast.make(message)*/
      ctx.longToast(message)
    }
  }

  object Error {
    fun show(ctx: Context, message: String = "Failed") {
      /*val coolToast = CoolToast(ctx)
      coolToast.setStyle(CoolToast.DANGER)
      coolToast.setDuration(CoolToast.LONG)
      coolToast.make(message)*/
      ctx.longToast(message)
    }
  }
}