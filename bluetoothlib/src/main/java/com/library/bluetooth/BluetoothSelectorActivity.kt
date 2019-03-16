package com.library.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Surface
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.github.ybq.android.spinkit.style.Pulse
import com.library.bluetooth.adapter.DevicesAdapter
import com.library.utils.PrefMan
import com.library.utils.location.LocationEnabledUtil
import com.library.utils.ui.AlertUtil
import com.library.utils.ui.ToastUtil
import kotlinx.android.synthetic.main.activity_bluetooth_selector.*
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast

class BluetoothSelectorActivity : AppCompatActivity(), LocationEnabledUtil.OnLocationActionCallback {

  lateinit var bluetooth: BluetoothAdapter
  lateinit var adapter: DevicesAdapter
  lateinit var prefMan: PrefMan
  lateinit var searchProgress: ProgressBar
  lateinit var searchProgressItem: MenuItem
  val LOCATION_ENABLE_RC = 89
  val REQUEST_ENABLE_BT = 56
  var pairingDialog: AwesomeProgressDialog? = null
  var deviceNameToAutoConnect: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bluetooth_selector)

    prefMan = PrefMan(this, PREF_FILE_NAME)

    askPermission {
      LocationEnabledUtil.checkAndAskLocation(this@BluetoothSelectorActivity, this@BluetoothSelectorActivity, 89)
    }

    bluetooth = BluetoothAdapter.getDefaultAdapter()

    deviceNameToAutoConnect = intent.getStringExtra(AUTO_CONNECT_DEVICE_NAME)
  }

  override fun onResume() {
    super.onResume()
    if (!bluetooth.isEnabled) {
      val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
      startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
    } else {
      listDevices()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.device_selector_menu, menu)
    searchProgressItem = menu!!.findItem(R.id.searching)
    searchProgress = searchProgressItem.actionView as ProgressBar
    searchProgressItem.isVisible = false
    return true
  }

  override fun onEnabled() {
  }

  override fun onCancelled() {
    toast("Please enable location")
    LocationEnabledUtil.checkAndAskLocation(this@BluetoothSelectorActivity, this@BluetoothSelectorActivity, LOCATION_ENABLE_RC)
  }


  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == LOCATION_ENABLE_RC && resultCode != Activity.RESULT_OK) {
      this@BluetoothSelectorActivity.onCancelled()
    } else if (requestCode == REQUEST_ENABLE_BT) {
      if (resultCode == Activity.RESULT_OK) {
        listDevices()
      } else {
        toast("Please enable bluetooth to use this app")
        setResult(Activity.RESULT_CANCELED)
        this@BluetoothSelectorActivity.finish()
      }
    }
  }

  private fun listDevices() {
    search_devices.setOnClickListener {
      if (!bluetooth.isDiscovering) {
        val filter = IntentFilter()

        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        registerReceiver(mReceiver, filter)
        bluetooth.startDiscovery()
      }
    }

    if (!prefMan.getDevice().isNullOrEmpty()) {
      ToastUtil.Success.show(this@BluetoothSelectorActivity, "Connecting to saved device")
      connectToDevice(prefMan.getDevice()!!)
      return
    }
    val list = ArrayList<BluetoothDevice>()
    list.addAll(bluetooth.bondedDevices)
    adapter = DevicesAdapter(this@BluetoothSelectorActivity, list) { _, device ->
      connectToDevice(device)
    }
    if (deviceNameToAutoConnect != null) {
      list.find { it.name == deviceNameToAutoConnect }?.let {
        connectToDevice(it)
      }
    }
    devices_recycler.layoutManager = when (windowManager.defaultDisplay.rotation) {
      Surface.ROTATION_90, Surface.ROTATION_180 -> GridLayoutManager(this, 4)
      Surface.ROTATION_0, Surface.ROTATION_270 -> LinearLayoutManager(this)
      else -> LinearLayoutManager(this)
    }
    devices_recycler.adapter = adapter
  }

  private fun connectToDevice(device: BluetoothDevice) {
    if (device.bondState == BluetoothDevice.BOND_NONE) {
      val intent = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
      pairingDialog = AlertUtil.ProgressAlert.show(this@BluetoothSelectorActivity, "Please wait", "Pairing...")
      registerReceiver(mPairReceiver, intent)
      device.setPin("1234".toByteArray())
      device.createBond()
    } else {
      connectToDevice(device.address)
    }
  }

  private fun connectToDevice(address: String) {
    if (save_device.isChecked) {
      prefMan.saveDevice(address)
    }
    Log.d("device", address)
    val i = Intent()
    i.putExtra("device", bluetooth.getRemoteDevice(address))
    setResult(Activity.RESULT_OK, i)
    this@BluetoothSelectorActivity.finish()
  }

  private val mReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val action = intent.action

      when (action) {
        BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
          val pulse = Pulse()
          pulse.color = Color.WHITE
          searchProgressItem.isVisible = true
          searchProgress.indeterminateDrawable = pulse
        }
        BluetoothAdapter.ACTION_DISCOVERY_FINISHED ->
          searchProgressItem.isVisible = false
        BluetoothDevice.ACTION_FOUND -> {
          val device = intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
          adapter.addDevice(device)
        }
      }
    }
  }

  private val mPairReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val action = intent.action

      if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
        val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
        val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

        if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
          pairingDialog?.hide()
          val device = intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
          connectToDevice(device.address)
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    try {
      unregisterReceiver(mReceiver)
      unregisterReceiver(mPairReceiver)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  companion object {
    val requestCode = 59
    val PREF_FILE_NAME = "device"
    val AUTO_CONNECT_DEVICE_NAME = "auto_connect_device_name"

    fun openBluetoothPicker(ctx: Activity, deviceNameToAutoConnect: String? = null, rc: Int = requestCode) {
      ctx.startActivityForResult<BluetoothSelectorActivity>(requestCode, AUTO_CONNECT_DEVICE_NAME to deviceNameToAutoConnect)
    }

    fun openBluetoothPicker(ctx: Fragment, deviceNameToAutoConnect: String? = null, rc: Int = requestCode) {
      val i = Intent(ctx.context, BluetoothSelectorActivity::class.java).also { it.putExtra(AUTO_CONNECT_DEVICE_NAME, deviceNameToAutoConnect) }
      ctx.startActivityForResult(i, requestCode)
    }

    fun parseResult(requestCode: Int, resultCode: Int, data: Intent?): BluetoothDevice? {
      var device: BluetoothDevice? = null
      if (resultCode == Activity.RESULT_OK && data != null) {
        device = data.getParcelableExtra(PREF_FILE_NAME)
      }
      return device
    }
  }
}