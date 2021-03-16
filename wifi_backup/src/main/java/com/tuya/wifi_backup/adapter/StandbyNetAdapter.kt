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

package com.tuya.wifi_backup.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tuya.smart.sdk.api.wifibackup.api.bean.BackupWifiBean
import com.tuya.wifi_backup.R
import java.util.*

/**
 * set up standby net adapter
 *
 * @author zhantang <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/23 2:20 PM
 */
class StandbyNetAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        val CURRENT_NET = 0
        val STANDBY_NET = 1
        val ADD_NET = 2
    }

    private var data: ArrayList<BackupWifiBean>? = null
    private var listener: onClickAddListener? =
        null
    private var currentWifiBean: BackupWifiBean? = null

    init {
        data = ArrayList()
    }


    fun setOnClickAddListener(listener: onClickAddListener?) {
        this.listener = listener
    }


    fun setData(data: List<BackupWifiBean>?) {
        this.data?.clear()

        currentWifiBean?.let {
            this.data?.add(it)
        }


        if (data != null) {
            this.data?.addAll(data)
        }
        notifyDataSetChanged()
    }

    fun setCurrentNet(backupWifiBean: BackupWifiBean?) {
        currentWifiBean = backupWifiBean
        currentWifiBean?.let {
            data?.add(0, it)
        }
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            StandbyNetAdapter.CURRENT_NET, StandbyNetAdapter.STANDBY_NET -> StandbyNetAdapter.NetViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.net_pool_recyclerview_item_standby_net, parent, false)
            )
            else ->  AddViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.net_pool_recyclerview_item_standby_add, parent, false)
            )
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val type = getItemViewType(position)
        when (type) {
            StandbyNetAdapter.CURRENT_NET -> {
                data?.let {
                    val currentWifiBean = it[position]
                    val ssidCurrent = currentWifiBean.ssid
                    (holder as StandbyNetAdapter.NetViewHolder).ssid.setText(
                        ssidCurrent
                    )
                    (holder as StandbyNetAdapter.NetViewHolder).tip.setVisibility(
                        View.VISIBLE
                    )
                }


            }
            StandbyNetAdapter.STANDBY_NET -> {
                data?.let {
                    val backupWifiBean = it[position]
                    val ssidStandby = backupWifiBean.ssid
                    (holder as StandbyNetAdapter.NetViewHolder).ssid.setText(
                        ssidStandby
                    )
                    (holder as StandbyNetAdapter.NetViewHolder).tip.setVisibility(
                        View.GONE
                    )
                    (holder as NetViewHolder).itemView.setOnClickListener { listener?.removeNet(position) }
                }

            }
            StandbyNetAdapter.ADD_NET -> (holder as AddViewHolder).add.setOnClickListener { listener?.addNet() }
            else -> {
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        if(data == null){
            return ADD_NET
        }

        if(data?.size == 0){
            return ADD_NET;
        }

        if (position == 0 && currentWifiBean != null) {
            return StandbyNetAdapter.CURRENT_NET
        }

        data?.let {
            if (position < it.size) {
               return StandbyNetAdapter.STANDBY_NET
            } else {
               return StandbyNetAdapter.ADD_NET
            }
        }

        return ADD_NET
    }

    override fun getItemCount(): Int {
        data?.let {
            return it.size +1
        }
        return 1
//        return if (data == null) 1 else data.size + 1
    }

    class NetViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var ssid: TextView
        var tip: TextView

        init {
            ssid = itemView.findViewById(R.id.tv_ssid)
            tip = itemView.findViewById(R.id.tv_tip)
        }
    }

    class AddViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var add: TextView

        init {
            add = itemView.findViewById(R.id.tv_add)
        }
    }

    interface onClickAddListener {
        fun addNet()
        fun removeNet(index:Int)
    }
}