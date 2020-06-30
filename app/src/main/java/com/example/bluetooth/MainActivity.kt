package com.example.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnONOFF.setOnClickListener {
            enableDisableBT()
        }

    }

    fun enableDisableBT() {
        // device does not have bluetooth
        if (bluetoothAdapter == null) {
            Log.d("Testing", "Device does not have bluetooth")
        }
        // if not enabled
        if (bluetoothAdapter?.isEnabled == false) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableIntent)
        }

    }
}