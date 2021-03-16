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

package com.tuya.wifi_backup.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import com.tuya.wifi_backup.R

/**
 * wifi dialog
 *
 * @author zhantang <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/25 5:42 PM
 */
class AddWifiDialog : Dialog{
    private var listener: ClickAddWifiDialogListener? = null
    private var etName: EditText? = null
    private  var etPassword:EditText? = null
    private  var tvAdd:TextView? = null

    constructor(context:Context):super(context){

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.net_pool_dialog_net_set)
        etName = findViewById(R.id.et_name)
        etPassword = findViewById(R.id.et_pwd)
        tvAdd = findViewById(R.id.tv_add)
        tvAdd?.setOnClickListener {
            listener?.let {
                it.onClickConnect(etName?.text.toString(),etPassword?.text.toString())
                dismiss()
            }
        }

    }

    fun setClickAddWifiDialogListener(listener: ClickAddWifiDialogListener) {
        this.listener = listener
    }

    interface ClickAddWifiDialogListener {
        fun onClickConnect(ssid: String?, pwd: String?)
    }

}