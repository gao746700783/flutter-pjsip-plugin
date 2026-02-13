import 'package:flutter_pjsip_plugin/model/index.dart';

import 'flutter_pjsip_plugin_platform_interface.dart';

class FlutterPjsipPlugin {
  Future<String?> getPlatformVersion() {
    return FlutterPjsipPluginPlatform.instance.getPlatformVersion();
  }

  Future<void> init() {
    return FlutterPjsipPluginPlatform.instance.init();
  }

  Future<void> initWithCb(LoginStatusChangeCallback callback) {
    return FlutterPjsipPluginPlatform.instance.initWithCb(callback);
  }

  Future loginSip({
    required String userName,
    required String password,
    required String displayName,
    required String sipIp,
    required String port,
  }) async {
    return await FlutterPjsipPluginPlatform.instance
        .loginSip(userName, displayName, password, sipIp, port);
  }

  Future logoutSip() async {
    return await FlutterPjsipPluginPlatform.instance.logoutSip();
  }

  Future makeCall({required phoneNo, required bool isVideo}) async {
    return await FlutterPjsipPluginPlatform.instance.makeCall(phoneNo, isVideo);
  }

  Future setAutoAnswer({required bool autoAnswer}) async {
    return await FlutterPjsipPluginPlatform.instance.setAutoAnswer(autoAnswer);
  }

  Future<String> getMacAddress() async {
    return await FlutterPjsipPluginPlatform.instance.getMacAddress();
  }

  // void onLoginStatusChange(LoginStatusChangeCallback callback) {
  //   FlutterPjsipPluginPlatform.instance.onLoginStatusChange(callback);
  // }
}
