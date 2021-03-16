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

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tuya.smart.android.common.utils.SHA256Util
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.api.ITuyaDataCallback
import com.tuya.smart.sdk.api.wifibackup.api.ITuyaWifiBackup
import com.tuya.smart.sdk.api.wifibackup.api.ITuyaWifiSwitch
import com.tuya.smart.sdk.api.wifibackup.api.bean.BackupWifiBean
import com.tuya.smart.sdk.api.wifibackup.api.bean.BackupWifiListInfo
import com.tuya.smart.sdk.api.wifibackup.api.bean.CurrentWifiInfoBean
import com.tuya.smart.sdk.api.wifibackup.api.bean.SwitchWifiResultBean
import com.tuya.smart.sdk.bean.DeviceBean
import com.tuya.wifi_backup.R
import com.tuya.wifi_backup.adapter.BackupAdapter
import com.tuya.wifi_backup.dialog.AddWifiDialog
import java.util.*

/**
 * Device net list activity
 *
 * @author zhantang <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/20 9:40 AM
 */
class DeviceNetListActivity : AppCompatActivity() {
    private var wifiSwitchManager: ITuyaWifiSwitch? = null
    private var wifiBackupManager: ITuyaWifiBackup? = null
    private var devId: String? = null
    private var dev: DeviceBean? = null
    private lateinit var runnable: Runnable
    private var switchCallback: ITuyaDataCallback<SwitchWifiResultBean>? = null
    private var currentCallback: ITuyaDataCallback<CurrentWifiInfoBean>? = null
    private var backupCallback: ITuyaDataCallback<BackupWifiListInfo>? = null

    private var tvCurrentTip: TextView? = null
    private  var tvStandbyTip:TextView? = null
    private  var tvCurrentSSID:TextView? = null
    private var rlOther: RelativeLayout? = null
    private var ivIntensity: ImageView? = null
    private var llDevWifiWrapper: LinearLayout? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: BackupAdapter? = null
    private val handler = Handler()
    private var devSSID: String? = null
    private val backupList: ArrayList<BackupWifiBean> = ArrayList()
    private val timeout = 120000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.net_pool_activity_net_info)
        init()
        initView()
        initData()
        initClick()
    }

    fun initView(){
        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }
        tvCurrentTip = findViewById(R.id.tv_current_tip)
        tvStandbyTip = findViewById(R.id.tv_standby_tip)
        tvCurrentSSID = findViewById(R.id.tv_current_ssid)
        rlOther = findViewById(R.id.rl_other)
        llDevWifiWrapper = findViewById(R.id.ll_dev_current_wifi_wrapper)
        ivIntensity = findViewById(R.id.iv_intensity)
        recyclerView = findViewById(R.id.rv_recycler_device_standby_net)
        adapter = BackupAdapter()
        adapter?.setOnClickItemListener(object : BackupAdapter.OnClickItemListener {
            override fun clickItem(data: BackupWifiBean?) {
                switchToBackupWifi(data)
            }
        })


        recyclerView?.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        recyclerView?.setAdapter(adapter)
    }

    fun initClick(){
        rlOther?.setOnClickListener{
            val addWifiDialog : AddWifiDialog = AddWifiDialog(this)
            addWifiDialog.setClickAddWifiDialogListener(object :AddWifiDialog.ClickAddWifiDialogListener{
                override fun onClickConnect(ssid: String?, pwd: String?) {

                    val hash = isContainBackup(ssid, pwd)
                    if (hash == null) {
                        wifiSwitchManager?.switchToNewWifi(ssid, pwd, switchCallback)
                    } else {
                        wifiSwitchManager?.switchToBackupWifi(hash, switchCallback)
                    }
                }
            })
            addWifiDialog.show()
        }
    }

    fun init(){
        devId = intent.getStringExtra("devId")
        dev = TuyaHomeSdk.getDataInstance().getDeviceBean(devId)
        wifiSwitchManager = TuyaHomeSdk.getWifiSwitchManager(devId)
        wifiBackupManager = TuyaHomeSdk.getWifiBackupManager(devId)

        runnable = Runnable {
            Toast.makeText(this,"timeout",Toast.LENGTH_LONG).show()
        }


        switchCallback = object : ITuyaDataCallback<SwitchWifiResultBean> {
            override fun onSuccess(result: SwitchWifiResultBean?) {
                handler.removeCallbacks(runnable)
                getDeviceCurrentNet()
                getBackupList()
                Toast.makeText(this@DeviceNetListActivity,getString(R.string.ty_net_pool_swich_net_success_toast),Toast.LENGTH_LONG).show()
            }

            override fun onError(errorCode: String, errorMessage: String) {
                handler.removeCallbacks(runnable)
                if (TextUtils.equals("2", errorCode)) {
                    Toast.makeText(this@DeviceNetListActivity,getString(R.string.ty_net_pool_switch_failed),Toast.LENGTH_LONG).show()
                }
            }
        }

        currentCallback = object : ITuyaDataCallback<CurrentWifiInfoBean> {
            override fun onSuccess(result: CurrentWifiInfoBean?) {
                if (result != null) {
                    setDeviceWifiView(result.ssid, result.signal)
                } else {
                    setDeviceWifiView(null, 0)
                }
            }

            override fun onError(errorCode: String, errorMessage: String) {
                setDeviceWifiView(null, 0)
            }
        }


        backupCallback = object : ITuyaDataCallback<BackupWifiListInfo> {
            override fun onSuccess(result: BackupWifiListInfo) {
                backupList.clear()
                backupList.addAll(result.backupList)
                setBackupWifiView(result.backupList)
            }

            override fun onError(errorCode: String, errorMessage: String) {
            }
        }


    }

    fun initData() {
        getDeviceCurrentNet()
        getBackupList()
    }

    fun switchToBackupWifi(data: BackupWifiBean?) {
        Toast.makeText(this@DeviceNetListActivity,"switch wifi",Toast.LENGTH_LONG).show()
        wifiSwitchManager?.switchToBackupWifi(data?.hash, switchCallback)
        runnable?.let {
            handler.postDelayed(runnable, timeout.toLong())
        }
    }

    private fun getDeviceCurrentNet() {
        wifiBackupManager?.getCurrentWifiInfo(currentCallback)
    }

    private fun getBackupList() {
        wifiBackupManager?.getBackupWifiList(backupCallback)
    }

    fun setDeviceWifiView(ssid: String?, intensity: Int) {
        devSSID = ssid
        if (!TextUtils.isEmpty(ssid)) {
            tvCurrentTip?.visibility = View.VISIBLE
            llDevWifiWrapper?.visibility = View.VISIBLE
            ivIntensity?.visibility = View.VISIBLE
            tvCurrentSSID?.text = ssid

        } else {
            tvCurrentTip?.visibility = View.GONE
            llDevWifiWrapper?.visibility = View.GONE
            ivIntensity?.visibility = View.GONE
        }
    }



    fun setBackupWifiView(backupWifiBeans: List<BackupWifiBean>?) {
        if (backupWifiBeans == null || backupWifiBeans.size == 0) {
            tvStandbyTip?.visibility = View.GONE
        } else {
            tvStandbyTip?.visibility = View.VISIBLE
        }
        adapter?.setData(backupWifiBeans)
    }

    /**
     * 如果返回值不为空，说明备用网络包含
     * @param ssid
     * @param pwd
     * @return
     */
    private fun isContainBackup(ssid: String?, pwd: String?): String? {
        if (dev == null) {
            return null
        }
        val hash = SHA256Util.getBase64Hash(dev?.getLocalKey() + ssid + pwd)
        for (backupWifiBean in backupList) {
            if (TextUtils.equals(backupWifiBean.hash, hash)) {
                return hash
            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiSwitchManager?.onDestroy()
        wifiBackupManager?.onDestroy()
    }
}