/*
 *  Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.huawei.loveandshare

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.common.ApiException

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var receiver: MyReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_get_token).setOnClickListener(this)
        findViewById<Button>(R.id.btn_delete_token).setOnClickListener(this)
        receiver = MyReceiver()
        val filter = IntentFilter()
        filter.addAction(CODELABS_ACTION)
        registerReceiver(receiver, filter)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_get_token -> getToken()
            R.id.btn_delete_token -> deleteToken()
            else -> {
            }
        }
    }

    /**
     * getToken(String appId, String scope), This method is used to obtain a token required for accessing HUAWEI Push Kit.
     * If there is no local AAID, this method will automatically generate an AAID when it is called because the Huawei Push server needs to generate a token based on the AAID.
     * This method is a synchronous method, and you cannot call it in the main thread. Otherwise, the main thread may be blocked.
     */
    private fun getToken() {
        showLog("getToken:begin")
        object : Thread() {
            override fun run() {
                try {
                    // read from agconnect-services.json
                    val appId = "Please enter your App_Id from agconnect-services.json "
                    val token = HmsInstanceId.getInstance(this@MainActivity).getToken(appId, "HCM")
                    Log.i(TAG, "get token:$token")
                    if (!TextUtils.isEmpty(token)) {
                        sendRegTokenToServer(token)
                    }
                    showLog("get token:$token")
                } catch (e: ApiException) {
                    Log.e(TAG, "get token failed, $e")
                    showLog("get token failed, $e")
                }
            }
        }.start()
    }

    /**
     * void deleteToken(String appId, String scope) throws ApiException
     * This method is used to obtain a token. After a token is deleted, the corresponding AAID will not be deleted.
     * This method is a synchronous method. Do not call it in the main thread. Otherwise, the main thread may be blocked.
     */
    private fun deleteToken() {
        showLog("deleteToken:begin")
        object : Thread() {
            override fun run() {
                try {
                    // read from agconnect-services.json
                    val appId = "Please enter your App_Id from agconnect-services.json "
                    HmsInstanceId.getInstance(this@MainActivity).deleteToken(appId, "HCM")
                    Log.i(TAG, "deleteToken success.")
                    showLog("deleteToken success")
                } catch (e: ApiException) {
                    Log.e(TAG, "deleteToken failed.$e")
                    showLog("deleteToken failed.$e")
                }
            }
        }.start()
    }

    /**
     * MyReceiver
     */
    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle = intent?.extras
            if (bundle?.getString("msg") != null) {
                val content = bundle.getString("msg")
                showLog(content)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    fun showLog(log: String?) {
        runOnUiThread {
            val tvView = findViewById<View?>(R.id.tv_log)
            val svView = findViewById<View?>(R.id.sv_log)
            if (tvView is TextView) {
                tvView.text = log
            }
            if (svView is ScrollView) {
                svView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun sendRegTokenToServer(token: String?) {
        Log.i(TAG, "sending token to server. token:$token")
    }

    companion object {
        private const val TAG: String = "PushDemoLog"
        private const val CODELABS_ACTION: String = "com.huawei.codelabpush.action"
    }
}
