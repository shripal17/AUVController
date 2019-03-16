package com.shripal17.auvcontroller

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.library.bluetooth.BluetoothCommunicator
import com.library.bluetooth.BluetoothSelectorActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {
  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

  }

  override fun onSensorChanged(event: SensorEvent?) {
    when (event?.sensor?.type) {
      Sensor.TYPE_ACCELEROMETER -> {
        mGravity = event.values
      }

      Sensor.TYPE_GYROSCOPE -> {
        gyroX = event.values[0].toDouble()
        gyroY = event.values[1].toDouble()
        gyroZ = event.values[2].toDouble()
      }

      Sensor.TYPE_LIGHT -> {
        lightIntensity = event.values[0].toDouble()
      }

      Sensor.TYPE_MAGNETIC_FIELD -> {
        mGeomagnetic = event.values
      }
    }
    val R = FloatArray(9)
    val I = FloatArray(9)
    if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {
      orientation = FloatArray(3)
      SensorManager.getOrientation(R, orientation)
      val azimuth = orientation[0]

      degrees = ((Math.toDegrees(azimuth.toDouble()) + 360) % 360).toFloat()
    }
  }

  private val sensorManager by lazy {
    getSystemService(Context.SENSOR_SERVICE) as SensorManager
  }

  var gyroX = 0.0
  var gyroY = 0.0
  var gyroZ = 0.0

  var mGravity = FloatArray(3)

  var orientation = FloatArray(3)

  var lightIntensity = 0.0

  var degrees = 0f

  private var mGeomagnetic = FloatArray(3)

  private val sensors = arrayOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_LIGHT, Sensor.TYPE_MAGNETIC_FIELD)

  private val timer by lazy {
    Timer()
  }

  private var timer2 = Timer()

  var communicator: BluetoothCommunicator? = null

  private val deviceName = "raspberrypi"

  var resumed = false

  val df = DecimalFormat.getInstance().apply {
    minimumFractionDigits = 5
    maximumFractionDigits = 5
    minimumIntegerDigits = 3
    maximumIntegerDigits = 3
    roundingMode = RoundingMode.DOWN
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    for (sensor in sensors) {
      sensorManager.getDefaultSensor(sensor)?.let {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(sensor), SensorManager.SENSOR_DELAY_FASTEST)
      }
    }

    timer.scheduleAtFixedRate(object : TimerTask() {
      override fun run() {
        runOnUiThread {
          val text = "AccelerometerX: ${mGravity[0].toDouble().format()}\n" +
              "AccelerometerY: ${mGravity[1].toDouble().format()}\n" +
              "AccelerometerZ: ${mGravity[2].toDouble().format()}\n\n" +
              "GyroX: ${gyroX.format()}\n" +
              "GyroY: ${gyroY.format()}\n" +
              "GyroZ: ${gyroZ.format()}\n\n" +
              "Light: ${lightIntensity.format()}\n" +
              "Degrees: ${degrees.toDouble().format()}\n\n" +
              "Azimuth: ${orientation[0].toDouble().format()}\n" +
              "Pitch: ${orientation[1].toDouble().format()}\n" +
              "Roll: ${orientation[2].toDouble().format()}\n"
          main.text = text

          communicator?.let {
            if (it.isAlive && it.connected) {
              val stringBuilder = StringBuilder().apply {
                append("Acc,")
                append(mGravity[0].toDouble().format())
                append(",")
                append(mGravity[1].toDouble().format())
                append(",")
                append(mGravity[2].toDouble().format())
              }
              it.write(stringBuilder.toString())
              stringBuilder.clear().apply {
                append("Gyr,")
                append(gyroX.format())
                append(",")
                append(gyroY.format())
                append(",")
                append(gyroZ.format())
              }
              it.write(stringBuilder.toString())
              stringBuilder.clear().apply {
                append("LDi,")
                append(lightIntensity.format())
                append(",")
                append(degrees.toDouble().format())
              }
              it.write(stringBuilder.toString())
              stringBuilder.clear().apply {
                append("Ori,")
                append(orientation[0].toDouble().format())
                append(",")
                append(orientation[1].toDouble().format())
                append(",")
                append(orientation[2].toDouble().format())
              }
              it.write(stringBuilder.toString())
            }
          }
        }
      }
    }, 0, 100)

    BluetoothSelectorActivity.openBluetoothPicker(this, deviceName)
  }

  private fun Double.format(): String {
    if (this < 0) {
      df.minimumIntegerDigits = 2
      df.maximumIntegerDigits = 2
    } else {
      df.minimumIntegerDigits = 3
      df.maximumIntegerDigits = 3
    }
    return df.format(this)
  }


  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == BluetoothSelectorActivity.requestCode) {
      val device = BluetoothSelectorActivity.parseResult(requestCode, resultCode, data)
      device?.let {
        communicator?.release()
        communicator = BluetoothCommunicator(
          this, it.address,
          { success, e ->
            if (success) {
              toast("${device.name} connected")
              timer2.destroy()
            } else {
              startBluetoothPickerTimer()
              e?.printStackTrace()
            }
          },
          {
            Log.d("received", it)
          },
          {
            startBluetoothPickerTimer()
          }).also {
          it.start()
        }
      }
    }
  }

  private fun openBluetoothPicker() = Runnable {
    //    if (resumed) {
    communicator?.let {
      if (!it.connected) {
        BluetoothSelectorActivity.openBluetoothPicker(this, deviceName)
      }
    }
//    }
  }

  private fun startBluetoothPickerTimer() {
    timer2.destroy()
    timer2 = Timer()
    timer2.scheduleAtFixedRate(object : TimerTask() {
      override fun run() {
        openBluetoothPicker().run()
      }
    }, 0, 10_000)
  }

  private fun Timer.destroy() {
    try {
      this.cancel()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun onStart() {
    super.onStart()
    resumed = true
  }

  override fun onStop() {
    super.onStop()
    resumed = false
  }

  override fun onDestroy() {
    super.onDestroy()
    sensorManager.unregisterListener(this)
    timer.destroy()
    timer2.destroy()
    communicator?.release()
  }
}
