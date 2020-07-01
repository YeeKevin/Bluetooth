package com.example.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList


class MainActivity : AppCompatActivity() {

    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    // array list to hold devices
    var mBTDevices = ArrayList<BluetoothDevice>()
    var mDeviceListAdapter: DeviceListAdapter? = null
    // var lvNewDevices: ListView? = null

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                /*
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.

                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                }

                 */
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            Log.d("Testing", "onReceive: STATE OFF")
                        }
                        BluetoothAdapter.STATE_TURNING_OFF -> {
                            Log.d("Testing", "receiver: STATE TURNING OFF")
                        }
                        BluetoothAdapter.STATE_ON -> {
                            Log.d("Testing", "receiver: STATE ON")
                        }
                        BluetoothAdapter.STATE_TURNING_ON -> {
                            Log.d("Testing", "receiver: STATE TURNING ON")
                        }
                    }
                }
            }
        }
    }

    // BroadCastReceiver for Discoverability
    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver2 = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {

                    val mode =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)

                    when (mode) {
                        // device is in discoverable mode
                        BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> {
                            Log.d("Testing", "receiver2: Discoverability enabled")
                        }
                        // device not in discoverable mode
                        BluetoothAdapter.SCAN_MODE_CONNECTABLE -> {
                            Log.d(
                                "Testing",
                                "receiver2: Discoverability disabled, able to receive connections"
                            )
                        }
                        BluetoothAdapter.SCAN_MODE_NONE -> {
                            Log.d(
                                "Testing",
                                "receiver2: Discoverability disabled, not able to receive connections"
                            )
                        }
                        BluetoothAdapter.STATE_CONNECTING -> {
                            Log.d("Testing", "receiver2: Connecting...")
                        }
                        BluetoothAdapter.STATE_CONNECTED -> {
                            Log.d("Testing", "receiver2: Connected")
                        }
                    }
                }
            }
        }
    }

    // for discovering devices
    private val receiver3 = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    mBTDevices?.add(device)
                    Log.d("Testing", "onReceive: " + device.name + ": " + device.address)
                    mDeviceListAdapter =
                        DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices)
                    lvNewDevices.adapter = mDeviceListAdapter

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnONOFF.setOnClickListener {
            Log.d("Testing", "onClick: enabling/disabling bluetooth")
            enableDisableBT()
        }

        btnDiscoverable.setOnClickListener {
            Log.d("Testing", "btnDiscoverable: Making device discoverable for 300 seconds")

            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            startActivity(discoverableIntent)

            // broadcast receiver will intercept state changes
            val intentFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
            registerReceiver(receiver2, intentFilter)
        }

        btnFindUnpairedDevices.setOnClickListener {
            Log.d("Testing", "btnFindUnpairedDevices: Looking for unpaired devices")

            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isDiscovering) {
                    bluetoothAdapter.cancelDiscovery()
                    Log.d("Testing", "btnFindUnpairedDevices: Cancelling discovery")

                    checkBTPermissions()

                    bluetoothAdapter.startDiscovery()
                    val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
                    registerReceiver(receiver3, discoverDevicesIntent)
                }
            }
            if (bluetoothAdapter != null) {
                Log.d("Testing", "btnFindUnpairedDevices: Now attempting to discover")
                if(!bluetoothAdapter.isDiscovering) {
                    checkBTPermissions()
                    bluetoothAdapter.startDiscovery()
                    val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
                    registerReceiver(receiver3, discoverDevicesIntent)
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d("Testing", "onDestroy: called.")
        super.onDestroy()
        unregisterReceiver(receiver)
        unregisterReceiver(receiver2)
    }

    // using broadcast receiver
    fun enableDisableBT() {
        // device does not have bluetooth
        if (bluetoothAdapter == null) {
            Log.d("Testing", "Device does not have bluetooth")
        }
        // if not enabled
        if (bluetoothAdapter?.isEnabled == false) {
            Log.d("Testing", "enableDisableBT: enabling BT")
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableIntent)

            // filter intercepts changes to Bluetooth status, logs
            val BTIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(receiver, BTIntent)
        }
        // if Bluetooth already enabled, disable
        if (bluetoothAdapter?.isEnabled == true) {
            Log.d("Testing", "enableDisableBT: disabling BT")
            bluetoothAdapter.disable()

            val BTIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            registerReceiver(receiver, BTIntent)
        }
    }

    private fun checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            var permissionCheck = checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION")
            permissionCheck += checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION")
            if (permissionCheck != 0) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 1001
                ) //Any number
            }
        } else {
            Log.d(
                "Testing",
                "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP."
            )
        }
    }
}