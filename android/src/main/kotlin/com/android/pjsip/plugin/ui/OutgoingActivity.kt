package com.android.pjsip.plugin.ui

import com.android.pjsip.plugin.databinding.LayoutOutgoingCallBinding
import com.android.pjsip.plugin.helper.Constants
import com.android.pjsip.plugin.helper.PjsipManager
import com.android.pjsip.plugin.ui.base.BaseVMActivity
import net.gotev.sipservice.SipServiceCommand
import org.pjsip.pjsua2.pjsip_inv_state

class OutgoingActivity : BaseVMActivity<LayoutOutgoingCallBinding>() {
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

    private var accountID = ""
    private var callID = 0
    private var displayName = ""
    private var remoteUri = ""
    private var isVideo = false

    override fun onDestroy() {
        super.onDestroy()
        PjsipManager.instance.removePjsipStatusListener(statusListener)
    }

    override fun isFullScreen(): Boolean {
        return true
    }

    override fun getBinding(): LayoutOutgoingCallBinding {
        return LayoutOutgoingCallBinding.inflate(layoutInflater)
    }

    override fun initUiAndData() {
        super.initUiAndData()
        PjsipManager.instance.addPjsipStatusListener(statusListener)

        accountID = intent?.extras?.getString(Constants.ACCOUNT_ID) ?: ""
        callID = intent?.extras?.getInt(Constants.CALL_ID) ?: 0
        displayName = intent?.extras?.getString(Constants.DISPLAY_NAME) ?: ""
        remoteUri = intent?.extras?.getString(Constants.REMOTE_URI) ?: ""
        isVideo = intent?.extras?.getBoolean(Constants.IS_VIDEO) == true

        var micMute = false

        views.btnCancelCall.setOnClickListener {
            //取消呼叫
            SipServiceCommand.hangUpActiveCalls(this, accountID)
            finish()
        }
        views.btnCameraOutgoing.setOnClickListener {
            //切换摄像头
            SipServiceCommand.switchVideoCaptureDevice(this, accountID, callID)
        }
        views.btnSpeakerOutgoing.setOnClickListener {
            //麦克风静音
            micMute = !micMute
            SipServiceCommand.setCallMute(this, accountID, callID, micMute)
        }
    }
}
