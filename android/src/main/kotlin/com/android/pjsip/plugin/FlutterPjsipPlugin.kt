package com.android.pjsip.plugin

import com.android.pjsip.plugin.helper.PjsipManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** FlutterPjsipPlugin */
class FlutterPjsipPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var mContext : android.content.Context

  private var mLoginStatusEventChannel: EventChannel? = null
  private var mLoginStatusEventSink: EventSink? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    mContext = flutterPluginBinding.applicationContext;
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_pjsip_plugin")
    channel.setMethodCallHandler(this)

    mLoginStatusEventChannel =
      EventChannel(flutterPluginBinding.binaryMessenger, "login_status_stream")
    mLoginStatusEventChannel!!.setStreamHandler(object : EventChannel.StreamHandler {
      override fun onListen(arguments: Any?, events: EventSink?) {
        mLoginStatusEventSink = events
      }

      override fun onCancel(arguments: Any?) {
      }
    })
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    val args = call.arguments as MutableMap<*, *>?
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "init" -> {
        PjsipManager.instance.init(mContext,mLoginStatusEventSink)
        result.success(null)
      }
      "loginSip" -> {
        val username = args!!["username"] as String
        val password = args["password"] as String
        val ip = args["ip"] as String
        val port = args["port"] as String
        val displayName = args["displayName"] as String
        PjsipManager.instance.registerWithCheck(username, password, ip, port,displayName)
        result.success(null)
      }
      "logoutSip" -> {
        PjsipManager.instance.unRegister()
        result.success(null)
      }
      "makeCall" -> {
        if (args?.containsKey("isVideo") == false || args?.containsKey("phoneNo") == false) {
          result.error("1","2","3")
          return
        }
        val isVideo = args!!["isVideo"] as Boolean
        val phoneNo = args!!["phoneNo"] as String
        if (isVideo) {
          PjsipManager.instance.videoCall(phoneNo)
        } else {
          PjsipManager.instance.audioCall(phoneNo)
        }
        result.success(null)
      }

      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
