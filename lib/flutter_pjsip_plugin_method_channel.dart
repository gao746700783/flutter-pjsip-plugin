import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_pjsip_plugin/model/index.dart';

import 'flutter_pjsip_plugin_platform_interface.dart';

/// An implementation of [FlutterPjsipPluginPlatform] that uses method channels.
class MethodChannelFlutterPjsipPlugin extends FlutterPjsipPluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_pjsip_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  final loginStatusEventChannel = const EventChannel("login_status_stream");

  Stream get loginStatusStream {
    return loginStatusEventChannel.receiveBroadcastStream();
  }

  @override
  Future init() async {
    await methodChannel.invokeMethod('init');
  }

  @override
  Future initWithCb(LoginStatusChangeCallback callback) async {
    loginStatusStream.listen((data) {
      callback(LoginStatusEnum.fromInt(data));
    });
    await methodChannel.invokeMethod('init');
  }

  @override
  Future loginSip(String userName, String displayName, String password,
      String sipIp, String port) async {
    Map option = {};
    option["username"] = userName;
    option["password"] = password;
    option["ip"] = sipIp;
    option["port"] = port;
    option["displayName"] = displayName;
    return await methodChannel.invokeMethod('loginSip', option);
  }

  @override
  Future logoutSip() async {
    return await methodChannel.invokeMethod('logoutSip');
  }

  @override
  Future makeCall(String phoneNo, bool isVideo) async {
    Map option = {};
    option["phoneNo"] = phoneNo;
    option["isVideo"] = isVideo;
    return await methodChannel.invokeMethod('makeCall', option);
  }

  @override
  Future setAutoAnswer(bool autoAnswer) async {
    Map option = {};
    option["autoAnswer"] = autoAnswer;
    return await methodChannel.invokeMethod('setAutoAnswer', option);
  }

  @override
  Future<String> getMacAddress() async {
    return await methodChannel.invokeMethod('getMacAddress');
  }

  // @override
  // void onLoginStatusChange(LoginStatusChangeCallback callback) {
  //   loginStatusStream.listen((data) {
  //     callback(LoginStatusEnum.fromInt(data));
  //   });
  // }
}
