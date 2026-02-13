package com.android.pjsip.plugin.ui

import android.widget.Toast
import com.android.pjsip.plugin.databinding.LayoutIncomingCallBinding
import com.android.pjsip.plugin.helper.PjsipManager
import com.android.pjsip.plugin.ui.base.BaseVMActivity
import net.gotev.sipservice.SipServiceCommand
import org.pjsip.pjsua2.pjsip_inv_state

class IncomingActivity : BaseVMActivity<LayoutIncomingCallBinding>() {

    private val statusListener: PjsipManager.PjsipStatusListener = object : PjsipManager.PjsipStatusListener {
        override fun onPjsipStatus(callStateCode: Int) {
            if (pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED == callStateCode) {
                //连接成功
                finish()
            } else if (pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED == callStateCode) {
                //断开连接
                finish()
            } else if (pjsip_inv_state.PJSIP_INV_STATE_NULL == callStateCode) {
                //未知错误
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PjsipManager.instance.removePjsipStatusListener(statusListener)
    }

    override fun isFullScreen(): Boolean {
        return true
    }

    override fun getBinding(): LayoutIncomingCallBinding {
        return LayoutIncomingCallBinding.inflate(layoutInflater)
    }

    override fun initUiAndData() {
        super.initUiAndData()
        PjsipManager.instance.addPjsipStatusListener(statusListener)

        val accountID = intent?.extras?.getString("accountID") ?: ""
        val callID = intent?.extras?.getInt("callID") ?: 0
        val displayName = intent?.extras?.getString("displayName") ?: ""
        val remoteUri = intent?.extras?.getString("remoteUri") ?: ""
        val isVideo = intent?.extras?.getBoolean("isVideo") == true


        views.btnDecline.setOnClickListener {
            //拒绝
            SipServiceCommand.declineIncomingCall(this, accountID, callID)

            finish()
        }
        views.btnAnswer.setOnClickListener {
            //接听
            SipServiceCommand.acceptIncomingCall(this, accountID, callID, isVideo)
//            CallActivity.startActivityIn(
//                this@IncomingActivity,
//                accountID,
//                callID,
//                displayName,
//                remoteUri,
//                isVideo
//            )
        }
    }
}
