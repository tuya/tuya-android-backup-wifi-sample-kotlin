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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tuya.smart.android.common.utils.SHA256Util
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.interior.device.bean.CommunicationEnum
import com.tuya.smart.sdk.api.ITuyaDataCallback
import com.tuya.smart.sdk.api.wifibackup.api.ITuyaWifiBackup
import com.tuya.smart.sdk.api.wifibackup.api.bean.BackupWifiBean
import com.tuya.smart.sdk.api.wifibackup.api.bean.BackupWifiListInfo
import com.tuya.smart.sdk.api.wifibackup.api.bean.BackupWifiResultBean
import com.tuya.smart.sdk.api.wifibackup.api.bean.CurrentWifiInfoBean
import com.tuya.smart.sdk.bean.DeviceBean
import com.tuya.wifi_backup.R
import com.tuya.wifi_backup.adapter.StandbyNetAdapter
import com.tuya.wifi_backup.dialog.AddWifiDialog
import java.util.*

/**
 * Set up the alternate network callback
 *
 * @author zhantang <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/20 4:38 PM
 */
class StandbyNetActivity : AppCompatActivity() {
    var recyclerView : RecyclerView? =  null
    private var standbyNetAdapter: StandbyNetAdapter? = null
    private var dev: DeviceBean? = null
    private var wifiBackup: ITuyaWifiBackup? = null
    private val backupWifiBeans: ArrayList<BackupWifiBean> = ArrayList()
    private var currentWifiInfoBean: BackupWifiBean? = null
    private var callback: ITuyaDataCallback<BackupWifiResultBean>? = null
    private var currentCallback: ITuyaDataCallback<CurrentWifiInfoBean>? = null
    private var devId: String? = null
    private var hasChange = false
    private val handler = Handler()
    private var runnable: Runnable? = null

    //添加备用网络上限
    private var max = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.net_pool_activity_standby_net)
        initView()
        init()
        initData()
    }

    fun init(){
        this.devId = intent.getStringExtra("devId")

        dev = TuyaHomeSdk.getDataInstance().getDeviceBean(devId)
        if(dev == null){
            finish()
            return
        }
        wifiBackup = TuyaHomeSdk.getWifiBackupManager(devId)
        callback = object : ITuyaDataCallback<BackupWifiResultBean> {
            override fun onSuccess(result: BackupWifiResultBean) {
                runnable?.let {
                    handler.removeCallbacks(it)
                }
                Toast.makeText(this@StandbyNetActivity,getString(R.string.ty_net_pool_upload_standby_success_tip),Toast.LENGTH_LONG).show()
                finish()
            }

            override fun onError(errorCode: String, errorMessage: String) {
                runnable?.let {
                    handler.removeCallbacks(it)
                }
                Toast.makeText(this@StandbyNetActivity,getString(R.string.ty_net_pool_upload_standby_fail_tip),Toast.LENGTH_LONG).show()
            }
        }

        currentCallback = object : ITuyaDataCallback<CurrentWifiInfoBean> {
            override fun onSuccess(result: CurrentWifiInfoBean) {
                currentWifiInfoBean = BackupWifiBean()
                currentWifiInfoBean?.hash = result.hash
                currentWifiInfoBean?.ssid = result.ssid
                setCurrentNet(currentWifiInfoBean)
            }

            override fun onError(errorCode: String, errorMessage: String) {}
        }

        runnable = Runnable {
            Toast.makeText(this,getString(R.string.ty_net_pool_upload_standby_fail_tip),Toast.LENGTH_LONG).show()
        }
    }

    private fun initData() {
        getStandbyNetData()
        getCurrentNetData()
    }


    fun removeBackupWifi(position: Int) {
        var position = position
        if (currentWifiInfoBean != null) {
            position--
        }
        if (backupWifiBeans.size > position) {
            backupWifiBeans.removeAt(position)
            hasChange = true
            refreshRecyclerView(backupWifiBeans)
        }
    }

    fun getDeviceNetInfo() {
        val dev = TuyaHomeSdk.getDataInstance().getDeviceBean(devId)
        dev?.let {
            if (!it.getCommunicationOnline(CommunicationEnum.LAN) && !it.getCommunicationOnline(CommunicationEnum.MQTT)) {
                Toast.makeText(this,getString(R.string.ty_net_pool_offine_tip),Toast.LENGTH_LONG).show()
                return
            }
        }

    }


    fun saveBackupList() {
        if (!hasChange) {
            finish()
            return
        }
        saveBackupWifiList()

    }

    private fun saveBackupWifiList() {
        wifiBackup?.setBackupWifiList(backupWifiBeans, callback)
        runnable?.let {
            handler.postDelayed(it, 5000)
        }
    }

    fun addNetAction() {
        if (backupWifiBeans.size >= max) {
            Toast.makeText(this, getString(R.string.ty_net_pool_standby_upper_limit_tip), Toast.LENGTH_SHORT).show()
            return
        }
        if (currentWifiInfoBean == null) {
            return
        }
        val addWifiDialog : AddWifiDialog = AddWifiDialog(this)
        addWifiDialog.setClickAddWifiDialogListener(object : AddWifiDialog.ClickAddWifiDialogListener{
            override fun onClickConnect(ssid: String?, pwd: String?) {
                addNetCheckExist(ssid, pwd)

            }
        })
        addWifiDialog.show()

    }


    private fun addNetCheckExist(ssid: String?, pwd: String?) {
        if (hasSameWIFI(ssid, pwd)) {
            Toast.makeText(this, getString(R.string.ty_net_pool_add_wifi_equal_to_standby_tip) ,Toast.LENGTH_SHORT).show()
            return
        }
        if (isSameCurrentWIFI(ssid, pwd)) {
            Toast.makeText(this,getString(R.string.ty_net_pool_add_wifi_equal_to_standby_tip),Toast.LENGTH_LONG).show()
            return
        }
        pushBackupList(ssid, pwd)
    }

    private fun pushBackupList(ssid: String?, pwd: String?) {
        val backupWifiBean = BackupWifiBean()
        backupWifiBean.ssid = ssid
        backupWifiBean.passwd = pwd
        backupWifiBean.hash = SHA256Util.getBase64Hash(dev?.getLocalKey() + ssid + pwd)
        backupWifiBeans.add(backupWifiBean)
        hasChange = true
        refreshRecyclerView(backupWifiBeans)
    }

    private fun hasSameWIFI(ssid: String?, pwd: String?): Boolean {
        val hash = SHA256Util.getBase64Hash(dev?.getLocalKey() + ssid + pwd)
        for (backupWifiBean in backupWifiBeans) {
            if (TextUtils.equals(hash, backupWifiBean.hash)) {
                return true
            }
        }
        return false
    }

    private fun isSameCurrentWIFI(ssid: String?, pwd: String?): Boolean {
        if (dev != null && currentWifiInfoBean != null) {
            val hash = SHA256Util.getBase64Hash(dev?.getLocalKey() + ssid + pwd)
            return TextUtils.equals(hash, currentWifiInfoBean?.hash)
        }
        return false
    }


    fun getStandbyNetData() {
        wifiBackup?.getBackupWifiList(object : ITuyaDataCallback<BackupWifiListInfo> {
            override fun onSuccess(result: BackupWifiListInfo) {
                backupWifiBeans.clear()
                try {
                    max = Integer.getInteger(result.maxNum)
                } catch (e: Exception) {
                }
                backupWifiBeans.addAll(result.backupList)
                refreshRecyclerView(result.backupList)
            }

            override fun onError(errorCode: String, errorMessage: String) {
            }
        })
    }

    fun getCurrentNetData() {
        wifiBackup?.getCurrentWifiInfo(currentCallback)
    }

    override fun onResume() {
        super.onResume()
        getDeviceNetInfo()
    }

    fun initView(){
        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_save -> saveBackupList()

            }
            false
        }


        recyclerView = findViewById(R.id.net_list)
        standbyNetAdapter = StandbyNetAdapter()
        standbyNetAdapter?.setOnClickAddListener(object : StandbyNetAdapter.onClickAddListener{
            override fun addNet() {
                addNetAction()
            }

            override fun removeNet(index: Int) {
                removeBackupWifi(index)
            }
        })

        recyclerView?.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        recyclerView?.setAdapter(standbyNetAdapter)
    }

    fun setCurrentNet(data: BackupWifiBean?) {
        standbyNetAdapter?.setCurrentNet(data)
    }

    fun refreshRecyclerView(result: List<BackupWifiBean>?) {
        standbyNetAdapter?.setData(result)
    }


    override fun onDestroy() {
        super.onDestroy()
        wifiBackup?.onDestroy()
    }
}