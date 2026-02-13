import 'package:flutter_pjsip_plugin/model/login_status_cb.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_pjsip_plugin/flutter_pjsip_plugin.dart';
import 'package:flutter_pjsip_plugin/flutter_pjsip_plugin_platform_interface.dart';
import 'package:flutter_pjsip_plugin/flutter_pjsip_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterPjsipPluginPlatform
    with MockPlatformInterfaceMixin
    implements FlutterPjsipPluginPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<String> getMacAddress() {
    return Future.value("");
  }

  @override
  Future init() {
    return Future.value();
  }

  @override
  Future loginSip(String userName, String displayName, String password,
      String sipIp, String port) {
    throw UnimplementedError();
  }

  @override
  Future logoutSip() {
    throw UnimplementedError();
  }

  @override
  Future makeCall(String phoneNo, bool isVideo) {
    // TODO: implement makeCall
    throw UnimplementedError();
  }

  @override
  void onLoginStatusChange(LoginStatusChangeCallback callback) {
    // TODO: implement onLoginStatusChange
  }

  @override
  Future setAutoAnswer(bool autoAnswer) {
    throw UnimplementedError();
  }

  @override
  Future initWithCb(LoginStatusChangeCallback callback) {
    throw UnimplementedError();
  }
}

void main() {
  final FlutterPjsipPluginPlatform initialPlatform =
      FlutterPjsipPluginPlatform.instance;

  test('$MethodChannelFlutterPjsipPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterPjsipPlugin>());
  });

  test('getPlatformVersion', () async {
    FlutterPjsipPlugin flutterPjsipPlugin = FlutterPjsipPlugin();
    MockFlutterPjsipPluginPlatform fakePlatform =
        MockFlutterPjsipPluginPlatform();
    FlutterPjsipPluginPlatform.instance = fakePlatform;

    expect(await flutterPjsipPlugin.getPlatformVersion(), '42');
  });
}
