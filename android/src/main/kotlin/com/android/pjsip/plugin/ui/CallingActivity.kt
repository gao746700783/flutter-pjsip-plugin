package com.android.pjsip.plugin.ui

import com.android.pjsip.plugin.databinding.ActivityCallingBinding
import com.android.pjsip.plugin.helper.Constants
import com.android.pjsip.plugin.helper.PjsipManager
import com.android.pjsip.plugin.ui.base.BaseVMActivity
import com.android.pjsip.plugin.ui.widget.TextureViewRenderer
import net.gotev.sipservice.Logger
import net.gotev.sipservice.MediaState
import net.gotev.sipservice.SipServiceCommand
import org.pjsip.pjsua2.pjsip_inv_state

class CallingActivity : BaseVMActivity<ActivityCallingBinding>() {

    private val statusListener: PjsipManager.PjsipStatusListener =
        object : PjsipManager.PjsipStatusListener {
            override fun onPjsipStatus(callStateCode: Int) {
                if (pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED == callStateCode) {
                    //断开连接
                    finish()
                } else if (pjsip_inv_state.PJSIP_INV_STATE_NULL == callStateCode) {
                    //未知错误
                    finish()
                }
            }

            override fun onPjsipMediaState(stateType: MediaState?, stateValue: Boolean) {
                if (stateType == MediaState.LOCAL_VIDEO_MUTE) {
                    // do nothing
                } else if (stateType == MediaState.LOCAL_MUTE) {
                    // do nothing
                }
            }
        }

    private var micMute = false
    private var accountID = ""
    private var callID = 0
    private var displayName = ""
    private var remoteUri = ""
    private var isVideo = false

    override fun onDestroy() {
        super.onDestroy()
        PjsipManager.instance.removePjsipStatusListener(statusListener)
    }

    override fun getBinding(): ActivityCallingBinding {
        return ActivityCallingBinding.inflate(layoutInflater)
    }

    override fun initUiAndData() {
        super.initUiAndData()
        PjsipManager.instance.addPjsipStatusListener(statusListener)

        accountID = intent?.extras?.getString(Constants.ACCOUNT_ID) ?: ""
        callID = intent?.extras?.getInt(Constants.CALL_ID) ?: 0
        displayName = intent?.extras?.getString(Constants.DISPLAY_NAME) ?: ""
        remoteUri = intent?.extras?.getString(Constants.REMOTE_URI) ?: ""
        isVideo = intent?.extras?.getBoolean(Constants.IS_VIDEO) == true

        views.svLocal.setOnSurfaceListener(object : TextureViewRenderer.OnRendererListener {
            override fun onRendererReady() {
                Logger.debug(TAG, "localRendererReady")
                views.svLocal.surfaceRenderer?.let {
                    SipServiceCommand.startVideoPreview(
                        this@CallingActivity, accountID, callID, it
                    )
                }
            }
        })
        views.svLocal.setMirror()

        views.remoteRenderer.setOnSurfaceListener(object : TextureViewRenderer.OnRendererListener {
            override fun onRendererReady() {
                Logger.debug(TAG, "remoteRendererReady")
                views.remoteRenderer.surfaceRenderer?.let {
                    SipServiceCommand.setupIncomingVideoFeed(
                        this@CallingActivity, accountID, callID, it
                    )
                }
            }
        })

        views.btnEndCall.setOnClickListener {
            //挂断
            SipServiceCommand.hangUpCall(this, accountID, callID)
            finish()
        }

        views.btnMute.setOnClickListener {
            //麦克风静音
            micMute = !micMute
            SipServiceCommand.setCallMute(this, accountID, callID, micMute)
        }
        views.btnVideo.setOnClickListener {
            //切换摄像头
            SipServiceCommand.switchVideoCaptureDevice(this, accountID, callID)
        }
    }


}
