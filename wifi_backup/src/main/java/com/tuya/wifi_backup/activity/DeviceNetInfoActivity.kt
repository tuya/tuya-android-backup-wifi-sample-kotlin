/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Tuya Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.tuya.wifi_backup.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.tuya.smart.android.common.utils.L
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.interior.device.bean.CommunicationEnum
import com.tuya.smart.sdk.api.ITuyaDataCallback
import com.tuya.smart.sdk.api.wifibackup.api.ITuyaWifiBackup
import com.tuya.smart.sdk.api.wifibackup.api.bean.CurrentWifiInfoBean
import com.tuya.wifi_backup.R

/**
 * Device net info activity
 *
 * @author zhantang <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/19 4:57 PM
 */
class DeviceNetInfoActivity : AppCompatActivity() {
    private val TAG = "DeviceNetInfoActivity"
    private var wifiBackup: ITuyaWifiBackup? = null
    private var deviceId: String? = null

    private val tvInstructions: TextView? = null
    private var llStandbyWrapper: LinearLayout? = null
    private var llDeviceNetWrapper: LinearLayout? = null
    private var tvDeviceNet: TextView? = null
    private var ivNet: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.net_pool_activity_deveice_info)
        deviceId = intent.getStringExtra("deviceId")
        wifiBackup = TuyaHomeSdk.getWifiBackupManager(deviceId)
        llStandbyWrapper = findViewById(R.id.ll_standby_wrapper)
        llDeviceNetWrapper = findViewById(R.id.ll_device_net_wrapper)
        tvDeviceNet = findViewById(R.id.tv_device_net_name)
        ivNet = findViewById(R.id.iv_net)
        initClick()
    }

    private fun initClick() {
        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }
        llStandbyWrapper?.setOnClickListener {
            val intent = Intent(this, StandbyNetActivity::class.java)
            intent.putExtra("devId", deviceId)
            startActivity(intent)
        }
        llDeviceNetWrapper?.setOnClickListener {
            val intent = Intent(this, DeviceNetListActivity::class.java)
            intent.putExtra("devId", deviceId)
            startActivity(intent)
        }
    }


    override fun onResume() {
        super.onResume()
        getDeviceNetInfo()
    }

    fun getDeviceNetInfo() {
        val dev = TuyaHomeSdk.getDataInstance().getDeviceBean(deviceId)
        dev?.let {
            if (!it.getCommunicationOnline(CommunicationEnum.LAN) && !it.getCommunicationOnline(
                    CommunicationEnum.MQTT
                )
            ) {
                Toast.makeText(this, getString(R.string.ty_net_pool_offine_tip), Toast.LENGTH_LONG)
                    .show()
                return
            }
        }



        wifiBackup?.getCurrentWifiInfo(object : ITuyaDataCallback<CurrentWifiInfoBean> {
            override fun onSuccess(result: CurrentWifiInfoBean) {
                //0 means WiFi and 1 means wired
                if (result.network == 0) {
                    showWifiInfo(result)
                } else if (result.network == 1) {
                    showWiredNetwork(result)
                }
            }

            override fun onError(errorCode: String, errorMessage: String) {
                L.i(TAG, errorMessage)
            }
        })

    }

    fun showWifiInfo(data: CurrentWifiInfoBean) {
        ivNet?.setVisibility(View.VISIBLE)
        tvDeviceNet?.setText(data.ssid)
        llDeviceNetWrapper?.setEnabled(true)
        tvInstructions?.setVisibility(View.VISIBLE)
        llStandbyWrapper?.setVisibility(View.VISIBLE)
    }

    fun showWiredNetwork(data: CurrentWifiInfoBean?) {
        ivNet?.setVisibility(View.GONE)
        llStandbyWrapper?.setVisibility(View.GONE)
        tvInstructions?.setVisibility(View.GONE)
        tvDeviceNet?.setText(getString(R.string.ty_net_pool_wired_net))
        llDeviceNetWrapper?.setEnabled(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiBackup?.onDestroy()
    }
}