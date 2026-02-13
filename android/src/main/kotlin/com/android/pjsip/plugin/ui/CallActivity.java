package com.android.pjsip.plugin.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.pjsip.plugin.R;

import net.gotev.sipservice.BroadcastEventReceiver;
import net.gotev.sipservice.CodecPriority;
import net.gotev.sipservice.Logger;
import net.gotev.sipservice.RtpStreamStats;
import net.gotev.sipservice.SipServiceCommand;

import org.pjsip.pjsua2.pjsip_inv_state;

import java.util.ArrayList;

public class CallActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {
    private static final String TAG = "CallActivity";

    TextView mTextViewPeer;
    TextView mTextViewCallState;
    Button mButtonAccept;
    Button mButtonHangup;
    LinearLayout mLayoutIncomingCall;
    TextView mTvOutCallInfo;
    Button mBtnCancel;
    LinearLayout mLayoutOutCall;
    SurfaceView mSvRemote;
    SurfaceView mSvLocal;
    ImageButton mBtnMuteMic;
    ImageButton mBtnHangUp;
    ImageButton mBtnSpeaker;
    ConstraintLayout mLayoutConnected;
    LinearLayout mParent;
    ConstraintLayout mLayoutCallInfo;

    private String mAccountID;
    private String mDisplayName;
    private String mRemoteUri;
    private int mCallID;
    private boolean mIsVideo;
    private int mType;
    private String mNumber;
    private boolean mIsVideoConference;
    private boolean micMute;
    private boolean localVideoMute;

    public static final int TYPE_INCOMING_CALL = 646;
    public static final int TYPE_OUT_CALL = 647;
    public static final int TYPE_CALL_CONNECTED = 648;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        registReceiver();


        mTextViewPeer = findViewById( R.id.textViewPeer);
        mTextViewCallState = findViewById( R.id.textViewCallState);
        mButtonAccept = findViewById( R.id.buttonAccept);
        mButtonAccept.setOnClickListener(this);
        mButtonHangup = findViewById( R.id.buttonHangup);
        mButtonHangup.setOnClickListener(this);
        mLayoutIncomingCall = findViewById( R.id.layoutIncomingCall);
        mTvOutCallInfo = findViewById( R.id.tvOutCallInfo);
        mBtnCancel = findViewById( R.id.btnCancel);
        mBtnCancel.setOnClickListener(this);
        mLayoutOutCall = findViewById( R.id.layoutOutCall);
        mSvRemote = findViewById( R.id.svRemote);
        mSvLocal = findViewById( R.id.svLocal);
        mBtnMuteMic = findViewById( R.id.btnMuteMic);
        mBtnMuteMic.setOnClickListener(this);
        mBtnHangUp = findViewById( R.id.btnHangUp);
        mBtnHangUp.setOnClickListener(this);
        mBtnSpeaker = findViewById( R.id.btnSwitchCamera);
        mBtnSpeaker.setOnClickListener(this);
        mLayoutConnected = findViewById( R.id.layoutConnected);
        mLayoutCallInfo = findViewById( R.id.layoutCallInfo);
        mParent = findViewById( R.id.parent);

        initData();
    }

    private void registReceiver() {
        mReceiver.register(this);
    }

    private void initData() {
        mAccountID = getIntent().getStringExtra("accountID");
        mCallID = getIntent().getIntExtra("callID", -1);
        mType = getIntent().getIntExtra("type", -1);
        mDisplayName = getIntent().getStringExtra("displayName");
        mRemoteUri = getIntent().getStringExtra("remoteUri");
        mNumber = getIntent().getStringExtra("number");
        mIsVideo = getIntent().getBooleanExtra("isVideo", false);
        mIsVideoConference = getIntent().getBooleanExtra("isVideoConference", false);

        showLayout(mType);
        mTextViewPeer.setText(String.format("%s\n%s", mRemoteUri, mDisplayName));
        mTvOutCallInfo.setText(String.format("您正在呼叫 %s", mNumber));

        SurfaceHolder holder = mSvLocal.getHolder();
        holder.addCallback(this);

        SipServiceCommand.changeVideoOrientation(CallActivity.this, mAccountID, mCallID, Surface.ROTATION_90);
        mSvRemote.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Logger.debug(TAG, "surfaceHolder" + surfaceHolder.getSurface()+ "surfaceCreated");

            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Logger.debug(TAG, "surfaceChanged");
                SipServiceCommand.setupIncomingVideoFeed(CallActivity.this, mAccountID, mCallID, holder.getSurface());
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Logger.debug(TAG, "surfaceDestroyed");
                SipServiceCommand.setupIncomingVideoFeed(CallActivity.this, mAccountID, mCallID, null);
            }
        });
    }

    public static void startActivityIn(Context context, String accountID, int callID, String displayName, String remoteUri, boolean isVideo) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra("accountID", accountID);
        intent.putExtra("callID", callID);
        intent.putExtra("displayName", displayName);
        intent.putExtra("remoteUri", remoteUri);
        intent.putExtra("isVideo", isVideo);
        intent.putExtra("type", TYPE_INCOMING_CALL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startActivityOut(Context context, String accountID, int callID, String number, boolean isVideo, boolean isVideoConference) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra("accountID", accountID);
        intent.putExtra("callID", callID);
        intent.putExtra("number", number);
        intent.putExtra("isVideo", isVideo);
        intent.putExtra("isVideoConference", isVideoConference);
        intent.putExtra("type", TYPE_OUT_CALL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showLayout(int type) {
        if (type == TYPE_INCOMING_CALL) {
            mLayoutIncomingCall.setVisibility(View.VISIBLE);
            mLayoutOutCall.setVisibility(View.GONE);
            mLayoutConnected.setVisibility(View.GONE);
        } else if (type == TYPE_OUT_CALL) {
            mLayoutIncomingCall.setVisibility(View.GONE);
            mLayoutOutCall.setVisibility(View.VISIBLE);
            mLayoutConnected.setVisibility(View.GONE);
        } else if (type == TYPE_CALL_CONNECTED) {
            mLayoutIncomingCall.setVisibility(View.GONE);
            mLayoutOutCall.setVisibility(View.GONE);
            mLayoutConnected.setVisibility(View.VISIBLE);
        } else {
            TextView textView = new TextView(this);
            textView.setText("ERROR~~~~~~~~~~~~~");
            mParent.addView(textView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReceiver.unregister(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Logger.debug(TAG, "surfaceHolder" + surfaceHolder.getSurface()+ "surfaceCreated");
        SipServiceCommand.startVideoPreview(CallActivity.this, mAccountID, mCallID, mSvLocal.getHolder().getSurface());

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Logger.debug(TAG, "surfaceHolder" + surfaceHolder.getSurface()+ "surfaceChanged");
        SipServiceCommand.startVideoPreview(CallActivity.this, mAccountID, mCallID, mSvLocal.getHolder().getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Logger.debug(TAG, "surfaceHolder" + surfaceHolder.getSurface()+ "surfaceDestroyed");
    }

    public BroadcastEventReceiver mReceiver = new BroadcastEventReceiver() {

        @Override
        public void onIncomingCall(String accountID, int callID, String displayName, String remoteUri, boolean isVideo) {
            super.onIncomingCall(accountID, callID, displayName, remoteUri, isVideo);
            Toast.makeText(CallActivity.this, String.format("收到 [%s] 的来电", remoteUri), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCallState(String accountID, int callID, int callStateCode, int callStatusCode, long connectTimestamp) {
            super.onCallState(accountID, callID, callStateCode, callStatusCode, connectTimestamp);
            Log.d(TAG,"onCallState "+callStateCode+" "+callStatusCode);
            if (pjsip_inv_state.PJSIP_INV_STATE_CALLING == callStateCode) {
                //呼出
                mTextViewCallState.setText("calling");
            } else if (pjsip_inv_state.PJSIP_INV_STATE_INCOMING == callStateCode) {
                //来电
                mTextViewCallState.setText("incoming");
            } else if (pjsip_inv_state.PJSIP_INV_STATE_EARLY == callStateCode) {
                //响铃
                mTextViewCallState.setText("early");
            } else if (pjsip_inv_state.PJSIP_INV_STATE_CONNECTING == callStateCode) {
                //连接中
                mTextViewCallState.setText("connecting");
            } else if (pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED == callStateCode) {
                //连接成功
                mTextViewCallState.setText("confirmed");
                showLayout(TYPE_CALL_CONNECTED);
            } else if (pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED == callStateCode) {
                //断开连接
                finish();
            } else if (pjsip_inv_state.PJSIP_INV_STATE_NULL == callStateCode) {
                //未知错误
                Toast.makeText(CallActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        @Override
        public void onOutgoingCall(String accountID, int callID, String number, boolean isVideo, boolean isVideoConference, boolean isTransfer) {
            super.onOutgoingCall(accountID, callID, number, isVideo, isVideoConference, isTransfer);
        }

        @Override
        public void onStackStatus(boolean started) {
            super.onStackStatus(started);
        }

        @Override
        public void onReceivedCodecPriorities(ArrayList<CodecPriority> codecPriorities) {
            super.onReceivedCodecPriorities(codecPriorities);
        }

        @Override
        public void onCodecPrioritiesSetStatus(boolean success) {
            super.onCodecPrioritiesSetStatus(success);
        }

        @Override
        public void onMissedCall(String displayName, String uri) {
            super.onMissedCall(displayName, uri);
        }

        @Override
        protected void onVideoSize(int width, int height) {
            super.onVideoSize(width, height);
            // 根据width,height 重新设置 mRemoteSurface宽高
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mLayoutCallInfo.getLayoutParams();
//            ConstraintSet constraintSet = new ConstraintSet();
//            constraintSet.clone(mLayoutCallInfo);
//            constraintSet.setDimensionRatio(mLayoutCallInfo,"");
//            constraintSet.applyTo(mLayoutCallInfo);
//            layoutParams.width = dp2px(CallActivity.this,width);
//            layoutParams.height = dp2px(CallActivity.this,height);
//            mLayoutCallInfo.setLayoutParams(layoutParams);
        }

        @Override
        protected void onCallStats(int callID, int duration, String audioCodec, int callStatusCode, RtpStreamStats rx, RtpStreamStats tx) {
            super.onCallStats(callID, duration, audioCodec, callStatusCode, rx, tx);
        }

//        @Override
//        public void onCallMediaState(String accountID, int callID, MediaState stateType, boolean stateValue) {
//            super.onCallMediaState(accountID, callID, stateType, stateValue);
//            if (stateType == MediaState.LOCAL_MUTE) {
//                micMute = stateValue;
//                mBtnMuteMic.setSelected(stateValue);
//            } else if (stateType == MediaState.LOCAL_VIDEO_MUTE) {
//                localVideoMute = stateValue;
//            }
//        }
    };

    @Override
    public void onClick(View view) {
        if (view.getId() ==  R.id.buttonAccept) {
            //接听
            SipServiceCommand.acceptIncomingCall(this, mAccountID, mCallID, mIsVideo);
        } else if (view.getId() ==  R.id.buttonHangup) {
            //拒绝
            SipServiceCommand.declineIncomingCall(this, mAccountID, mCallID);
            finish();
        } else if (view.getId() ==  R.id.btnCancel) {
            //取消呼叫
            SipServiceCommand.hangUpActiveCalls(this, mAccountID);
            finish();
        } else if (view.getId() ==  R.id.btnMuteMic) {
            //麦克风静音
            micMute = !micMute;
            SipServiceCommand.setCallMute(this, mAccountID, mCallID, micMute);
        } else if (view.getId() ==  R.id.btnHangUp) {
            //挂断
            SipServiceCommand.hangUpCall(this, mAccountID, mCallID);
            finish();
        } else if (view.getId() == R.id.btnSwitchCamera) {
            //切换摄像头
            SipServiceCommand.switchVideoCaptureDevice(this, mAccountID, mCallID);
        }
    }

    private int dp2px(Context context,float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
