package com.library.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import org.jetbrains.anko.runOnUiThread
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothCommunicator(
  val ctx: Context,
  address: String,
  val onConnectionStatus: (status: Boolean, e: Exception?) -> Unit,
  val onMessageReceived: (message: String) -> Unit,
  val onDeviceDisconnected: () -> Unit
) : Thread() {

  private val UUID_WELL_KNOWN_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

  private val adapter = BluetoothAdapter.getDefaultAdapter()
  private val device = adapter.getRemoteDevice(address)
  private val btSocket = device.createRfcommSocketToServiceRecord(UUID_WELL_KNOWN_SPP)
  var connected = false
  private var data: String? = ""
  private lateinit var inputStream: InputStream
  private lateinit var outputStream: OutputStream

  override fun run() {
    try {
      btSocket.connect()
      connected = true
      inputStream = btSocket.inputStream
      outputStream = btSocket.outputStream
      ctx.runOnUiThread {
        onConnectionStatus(true, null)
      }
      while (connected) {
        try {
          data += read()
          if (data != null) {
            if ((data!!.isNotBlank() || data!!.isNotEmpty()) && data!!.contains("\r\n")) {
              val parts = data?.split("\r\n")
              for (i in 0 until parts!!.size - 1) {
                ctx.runOnUiThread {
                  onMessageReceived(parts[i])
                }
              }
              data = parts.last()
            }
          }
        } catch (e: Exception) {
          e.printStackTrace()
          ctx.runOnUiThread {
            onDeviceDisconnected()
            release()
          }
        }
      }
    } catch (e: Exception) {
      connected = false
      ctx.runOnUiThread {
        onConnectionStatus(false, e)
      }
    }
  }

  private fun read(): String? {
    val buffer = ByteArray(1024)
    var bytes = 0
    val recvd: String
    try {
      bytes = inputStream.read(buffer)
    } catch (e: Exception) {
      release()
      ctx.runOnUiThread {
        onDeviceDisconnected()
      }
      e.printStackTrace()
    }
    recvd = String(buffer, 0, bytes)
    return recvd
  }

  fun release() {
    if (connected) {
      connected = false
      btSocket.close()
      inputStream.close()
      outputStream.close()
    }
  }

  fun write(msg: String): Boolean {
    var status = false
    val msg2 = msg + "\r\n"
    if (connected && data != null) {
      status = try {
        outputStream.write(msg2.toByteArray())
        true
      } catch (e: Exception) {
        e.printStackTrace()
        false
      }
    }
    return status
  }
}