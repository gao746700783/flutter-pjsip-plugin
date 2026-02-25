package com.android.pjsip.plugin.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import com.android.pjsip.plugin.ui.CallingActivity
import com.android.pjsip.plugin.ui.IncomingActivity
import com.android.pjsip.plugin.ui.OutgoingActivity
import io.flutter.plugin.common.EventChannel
import net.gotev.sipservice.BroadcastEventReceiver
import net.gotev.sipservice.Logger
import net.gotev.sipservice.MediaState
import net.gotev.sipservice.SipAccountData
import net.gotev.sipservice.SipAccountTransport
import net.gotev.sipservice.SipServiceCommand
import org.pjsip.pjsua2.pjsip_inv_state
import org.pjsip.pjsua2.pjsip_status_code
import java.lang.ref.WeakReference

class PjsipManager {

    companion object {
        private const val TAG = "PjsipManager"
        val instance: PjsipManager by lazy { PjsipManager() }
    }

    private var isInitialized = false

    private lateinit var mContext: Context
    private lateinit var mActWeakRef: WeakReference<Activity>
    private var mStatusSink: EventChannel.EventSink? = null
    private var accountID: String? = null
    private var mRegCode: Int? = pjsip_status_code.PJSIP_SC_NULL
    private val mPjsipStatusListeners: MutableList<PjsipStatusListener> = mutableListOf()

    @Synchronized
    fun getInstance(): PjsipManager {
        return instance
    }

    fun init(
        appContext: Context,
        actContext: WeakReference<Activity>,
        eventSink: EventChannel.EventSink?
    ) {
        if (this.isInitialized) {
            Logger.debug(TAG, "PjsipManager is already initialized.")
            return
        }
        Logger.error(TAG, "init called...")
        this.isInitialized = true
        this.mContext = appContext
        this.mActWeakRef = actContext
        this.mStatusSink = eventSink

        Logger.setLogLevel(Logger.LogLevel.DEBUG)
        SipServiceCommand.enableSipDebugLogging(true)
        val cameraManager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        SipServiceCommand.setCameraManager(cameraManager)
        SipServiceCommand.start(mContext)
        this.mReceiver.register(mContext)
        //requestPermissions()
    }

    fun unInit() {
        Logger.debug(TAG, "unInit called...")
        this.isInitialized = false
        this.mStatusSink = null
        this.accountID = null
        this.mRegCode = pjsip_status_code.PJSIP_SC_NULL
        this.mReceiver.unregister(mContext)
        SipServiceCommand.stop(mContext)
    }

    fun registerWithCheck(
        userName: String,
        userPwd: String,
        serverHost: String,
        serverPort: String,
        displayName: String,
    ) {
        Logger.debug(
            TAG,
            "login: userName:$userName, userPwd:$userPwd, serverHost:$serverHost, serverPort:$serverPort,displayName:$displayName"
        )
        val mAccount = SipAccountData().apply {
            setUsername(userName)
            setPassword(userPwd)
            setHost(serverHost)
            setPort(serverPort.toInt().toLong())
            setRealm("*")
            setTransport(SipAccountTransport.TCP)
        }
        val checkId = getAccountIdByIdUri(mAccount)
        if (checkId == this.accountID) {
            Logger.debug(TAG, "login: accountID is same, just check current status")
            val isReg = checkRegStatus()
            if (isReg) {
                Logger.debug(TAG, "login: already registered")
                return
            }
        }
        val mAccountId = SipServiceCommand.setAccount(mContext, mAccount)
        Logger.debug(TAG, "login: mAccount:$mAccount, mAccountId $mAccountId")
    }

    private fun getAccountIdByIdUri(account: SipAccountData): String {
        if ("*" == account.realm)  //return "sip:" + username;
            return "sip:" + account.username + "@" + account.host + ":" + account.port

        return "sip:" + account.username + "@" + account.realm
    }

    private fun checkRegStatus(): Boolean {
        Logger.debug(TAG, "checkRegStatus: accountID: ${this.accountID}")
        if (mRegCode == pjsip_status_code.PJSIP_SC_OK) {
            return true
        }
        return false
    }

    fun unRegister() {
        if (accountID.isNullOrEmpty().not()) {
            SipServiceCommand.removeAccount(mContext, accountID)
            accountID = null
        }
    }


    //语音呼叫
    fun audioCall(callNumber: String?) {
        try {
            SipServiceCommand.makeCall(mContext, accountID, callNumber)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //视频呼叫
    fun videoCall(callNumber: String?) {
        try {
            SipServiceCommand.makeCall(mContext, accountID, callNumber, true, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addPjsipStatusListener(listener: PjsipStatusListener) {
        if (mPjsipStatusListeners.contains(listener)) {
            return
        }
        mPjsipStatusListeners.add(listener)
    }

    fun removePjsipStatusListener(listener: PjsipStatusListener) {
        mPjsipStatusListeners.remove(listener)
    }

    private var mReceiver: BroadcastEventReceiver = object : BroadcastEventReceiver() {
        override fun onRegistration(accountID: String, registrationStateCode: Int) {
            super.onRegistration(accountID, registrationStateCode)

            this@PjsipManager.mRegCode = registrationStateCode
            this@PjsipManager.mStatusSink?.success(registrationStateCode)
            if (registrationStateCode == pjsip_status_code.PJSIP_SC_OK) {
                this@PjsipManager.accountID = accountID
            }
        }

        override fun onIncomingCall(
            accountID: String,
            callID: Int,
            displayName: String,
            remoteUri: String,
            isVideo: Boolean
        ) {
            super.onIncomingCall(accountID, callID, displayName, remoteUri, isVideo)
            startActivityIn(
                IncomingActivity::class.java,
                accountID,
                callID,
                displayName,
                remoteUri,
                isVideo
            )
        }

        override fun onOutgoingCall(
            accountID: String,
            callID: Int,
            number: String,
            isVideo: Boolean,
            isVideoConference: Boolean,
            isTransfer: Boolean
        ) {
            super.onOutgoingCall(accountID, callID, number, isVideo, isVideoConference, isTransfer)
            startActivityIn(OutgoingActivity::class.java, accountID, callID, "", number, isVideo)
        }

        override fun onCallState(
            accountID: String?,
            callID: Int,
            callStateCode: Int,
            callStatusCode: Int,
            connectTimestamp: Long
        ) {
            super.onCallState(accountID, callID, callStateCode, callStatusCode, connectTimestamp)
            this@PjsipManager.mPjsipStatusListeners.forEach {
                it.onPjsipStatus(callStateCode)
            }

            if (pjsip_inv_state.PJSIP_INV_STATE_CALLING == callStateCode) {
                // 去电
                // startActivityIn(mContext, OutgoingActivity::class.java, accountID, callID, "", "", false)
            } else if (pjsip_inv_state.PJSIP_INV_STATE_INCOMING == callStateCode) {
                //来电
                // startActivityIn(mContext, IncomingActivity::class.java, accountID, callID, "", "", false)
            } else if (pjsip_inv_state.PJSIP_INV_STATE_EARLY == callStateCode) {
                //响铃
//                mTextViewCallState.setText("early")
            } else if (pjsip_inv_state.PJSIP_INV_STATE_CONNECTING == callStateCode) {
                //连接中
//                mTextViewCallState.setText("connecting")
            } else if (pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED == callStateCode) {
                //连接成功
//                mTextViewCallState.setText("confirmed")
//                showLayout(CallActivity.TYPE_CALL_CONNECTED)
                startActivityIn(CallingActivity::class.java, accountID, callID, "", "", false)
            } else if (pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED == callStateCode) {
                //断开连接
//                finish()
            } else if (pjsip_inv_state.PJSIP_INV_STATE_NULL == callStateCode) {
                //未知错误
//                Toast.makeText(this@CallActivity, "未知错误", Toast.LENGTH_SHORT).show()
//                finish()
            }
        }

        override fun onVideoSize(width: Int, height: Int) {
            super.onVideoSize(width, height)
            // 根据width,height 重新设置 mRemoteSurface宽高
//            val layoutParams = mLayoutCallInfo.getLayoutParams() as ConstraintLayout.LayoutParams
//            ConstraintSet constraintSet = new ConstraintSet();
//            constraintSet.clone(mLayoutCallInfo);
//            constraintSet.setDimensionRatio(mLayoutCallInfo,"");
//            constraintSet.applyTo(mLayoutCallInfo);
//            layoutParams.width = dp2px(CallActivity.this,width);
//            layoutParams.height = dp2px(CallActivity.this,height);
//            mLayoutCallInfo.setLayoutParams(layoutParams);
        }

        override fun onCallMediaState(
            accountID: String?,
            callID: Int,
            stateType: MediaState?,
            stateValue: Boolean
        ) {
            super.onCallMediaState(accountID, callID, stateType, stateValue)
            this@PjsipManager.mPjsipStatusListeners.forEach {
                it.onPjsipMediaState(stateType, stateValue)
            }
        }
    }

    fun startActivityIn(
//        context: Context,
        clazz: Class<out Activity>,
        accountID: String?,
        callID: Int,
        displayName: String?,
        remoteUri: String?,
        isVideo: Boolean
    ) {
        if (mActWeakRef.get() == null) {
            return
        }
        val intent = Intent(mActWeakRef.get(), clazz)
        intent.putExtra(Constants.ACCOUNT_ID, accountID)
        intent.putExtra(Constants.CALL_ID, callID)
        intent.putExtra(Constants.DISPLAY_NAME, displayName)
        intent.putExtra(Constants.REMOTE_URI, remoteUri)
        intent.putExtra(Constants.IS_VIDEO, isVideo)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        mActWeakRef.get()?.startActivity(intent)
    }

    interface PjsipStatusListener {
        fun onPjsipStatus(code: Int)
        fun onPjsipMediaState(stateType: MediaState?, stateValue: Boolean) {}
    }

}