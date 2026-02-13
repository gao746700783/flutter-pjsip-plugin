package com.android.pjsip.plugin.ui

import android.view.SurfaceHolder
import com.android.pjsip.plugin.databinding.ActivityCallingBinding
import com.android.pjsip.plugin.helper.PjsipManager
import com.android.pjsip.plugin.ui.base.BaseVMActivity
import net.gotev.sipservice.Logger
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
        }

    private var micMute = false

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

        val accountID = intent?.extras?.getString("accountID") ?: ""
        val callID = intent?.extras?.getInt("callID") ?: 0
        val displayName = intent?.extras?.getString("displayName") ?: ""
        val remoteUri = intent?.extras?.getString("remoteUri") ?: ""
        val isVideo = intent?.extras?.getBoolean("isVideo") == true

        views.svLocal.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Logger.debug(TAG, "surfaceHolder" + holder.surface + "surfaceCreated")
                SipServiceCommand.startVideoPreview(
                    this@CallingActivity,
                    accountID,
                    callID,
                    holder.surface
                )
            }

            override fun surfaceChanged(holder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
                Logger.debug(TAG, "surfaceHolder" + holder.surface + "surfaceChanged")
                SipServiceCommand.startVideoPreview(
                    this@CallingActivity,
                    accountID,
                    callID,
                    holder.surface
                )
            }

            override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
                Logger.debug(TAG, "surfaceHolder" + surfaceHolder.surface + "surfaceDestroyed")
            }
        })

        views.svRemote.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Logger.debug(TAG, "surfaceHolder" + holder.surface + "surfaceCreated")
                SipServiceCommand.setupIncomingVideoFeed(
                    this@CallingActivity,
                    accountID,
                    callID,
                    holder.surface
                )
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Logger.debug(TAG, "surfaceChanged")
                SipServiceCommand.setupIncomingVideoFeed(
                    this@CallingActivity,
                    accountID,
                    callID,
                    holder.surface
                )
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Logger.debug(TAG, "surfaceDestroyed")
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
