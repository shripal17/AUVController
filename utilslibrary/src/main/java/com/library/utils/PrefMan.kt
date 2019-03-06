package com.library.utils

import android.content.Context

class PrefMan(ctx: Context, fileName: String = "prefs") {
  val prefs = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
  val editor = prefs.edit()

  fun saveString(key: String, value: String) {
    editor.putString(key, value)
    editor.apply()
  }

  fun saveInt(key: String, value: Int) {
    editor.putInt(key, value)
    editor.apply()
  }

  fun saveFloat(key: String, value: Float) {
    editor.putFloat(key, value)
    editor.apply()
  }

  fun saveBool(key: String, value: Boolean) {
    editor.putBoolean(key, value)
    editor.apply()
  }

  fun getString(key: String, defValue: String? = ""): String? = prefs.getString(key, defValue)

  fun getInt(key: String): Int = prefs.getInt(key, 0)

  fun getFloat(key: String): Float = prefs.getFloat(key, 0f)

  fun getBool(key: String): Boolean = prefs.getBoolean(key, false)

  fun saveUsername(username: String) {
    saveString("username", username)

  }

  fun getUsername(): String? = getString("username")

  fun saveSession() = saveBool("logged_in", true)

  fun deleteSession() = saveBool("logged_in", false)

  fun getSession(): Boolean = getBool("logged_in")

  fun getDevice(): String? = getString("device")

  fun saveDevice(device: String) = saveString("device", device)

  fun removeDevice() {
    editor.remove("device")
    editor.apply()
  }

  fun clear() {
    editor.clear()
    editor.apply()
  }
}