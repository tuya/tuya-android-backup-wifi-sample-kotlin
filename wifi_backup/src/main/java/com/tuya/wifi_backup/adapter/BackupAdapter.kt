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
 * Backup net adapter
 *
 * @author zhantang <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/20 11:50 AM
 */
class BackupAdapter : RecyclerView.Adapter<BackupAdapter.NetViewHolder> {
    private var data: ArrayList<BackupWifiBean>? = null
    private var listener: OnClickItemListener? = null


    constructor():super(){
        data = ArrayList()
    }



    fun setOnClickItemListener(listener: OnClickItemListener?) {
        this.listener = listener
    }


    fun setData(data: List<BackupWifiBean>?) {
        this.data?.clear()
        data?.let {
            this.data?.addAll(data)
        }
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun onBindViewHolder(holder: NetViewHolder, position: Int) {
        data?.let {
            val backupWifiBean = it[position]
            holder.ssid.text = backupWifiBean.ssid
            holder.wrapper.setOnClickListener {
                listener?.clickItem(backupWifiBean)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetViewHolder {
        return NetViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.net_pool_recyclerview_item_net, parent, false)
        )
    }

    class NetViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var ssid: TextView
        var wrapper: View

        init {
            ssid = itemView.findViewById(R.id.tv_content)
            wrapper = itemView
        }
    }

    interface OnClickItemListener {
        fun clickItem(data: BackupWifiBean?)
    }
}