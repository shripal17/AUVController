package com.library.bluetooth.adapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.library.bluetooth.R
import org.jetbrains.anko.find

class DevicesAdapter(val ctx: Context, val data: ArrayList<BluetoothDevice>, val onClick: (position: Int, device: BluetoothDevice) -> Unit) : RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

  fun addDevice(device: BluetoothDevice) {
    if (!data.contains(device)) {
      data.add(device)
      notifyItemInserted(data.size - 1)
    } else {
      data.find {
        it.address == device.address
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder = DeviceViewHolder(LayoutInflater.from(ctx).inflate(R.layout.device_item, parent, false))

  override fun getItemCount(): Int = data.size

  override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
    holder.bind(position, onClick)
  }

  inner class DeviceViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    var name: TextView
    var mac: TextView
    var pairStatus: ImageView

    init {
      name = v.find(R.id.device_name)
      mac = v.find(R.id.device_mac)
      pairStatus = v.find(R.id.imageView)
    }

    fun bind(position: Int, onClick: (position: Int, device: BluetoothDevice) -> Unit) {
      val device = data[position]
      name.text = device.name
      mac.text = device.address
      if (device.bondState == BluetoothDevice.BOND_BONDED) {
        pairStatus.setColorFilter(Color.parseColor("#2962FF"))
      }
      itemView.setOnClickListener {
        onClick(position, device)
      }
    }
  }
}