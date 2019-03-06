package com.shripal17.auvcontroller

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
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

  var mGeomagnetic = FloatArray(3)

  val sensors = arrayOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_LIGHT, Sensor.TYPE_MAGNETIC_FIELD)

  val timer by lazy {
    Timer()
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
          val text = "AccelerometerX: ${mGravity[0]}\n" +
              "AccelerometerY: ${mGravity[1]}\n" +
              "AccelerometerZ: ${mGravity[2]}\n\n" +
              "GyroX: $gyroX\n" +
              "GyroY: $gyroY\n" +
              "GyroZ: $gyroZ\n\n" +
              "Light: $lightIntensity\n\n" +
              "Azimuth: ${orientation[0]}\n" +
              "Pitch: ${orientation[1]}\n" +
              "Roll: ${orientation[2]}\n" +
              "Degrees: $degrees"
          main.text = text
        }
      }
    }, 0, 100)
  }

  override fun onDestroy() {
    super.onDestroy()
    sensorManager.unregisterListener(this)
    timer.cancel()
  }
}
