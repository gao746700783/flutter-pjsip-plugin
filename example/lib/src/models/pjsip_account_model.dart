import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_pjsip_plugin/flutter_pjsip_plugin.dart';
import 'package:flutter_pjsip_plugin/model/index.dart';
import 'package:logger/logger.dart';

class PjsipAccountModel extends ChangeNotifier {
  final Logger _logger = Logger();

  LoginStatusEnum _loginStatus = LoginStatusEnum.none;
  LoginStatusEnum get loginStatus => _loginStatus;
  SipAccount? _sipAccount;
  SipAccount? get sipAccount => _sipAccount;

  String _platformVersion = 'Unknown';
  final FlutterPjsipPlugin _plugin = FlutterPjsipPlugin();

  String get platformVersion => _platformVersion;

  Future<void> fetchPlatformVersion() async {
    String version;
    try {
      version =
          await _plugin.getPlatformVersion() ?? 'Unknown platform version';
    } on PlatformException {
      version = 'Failed to get platform version.';
    }
    _platformVersion = version;
    notifyListeners();
  }

  Future<void> init() async {
    _logger.d('init here');
    // _plugin.init();
    _plugin.initWithCb((loginStatus) {
      _loginStatus = loginStatus;
      notifyListeners();
    });
  }

  Future<void> register({
    required String sipUri,
    required String port,
    required String displayName,
    required String password,
    required String authUser,
  }) async {
    _logger.d('register sip account: $sipUri, $port, $displayName, $authUser');
    _plugin.loginSip(
        userName: authUser,
        password: password,
        displayName: displayName,
        sipIp: sipUri,
        port: port);
  }

  Future<void> call(String phoneNo, {bool isVideo = false}) async {
    _logger.d("call with: phoneNo:$phoneNo, isVideo:$isVideo");
    _plugin.makeCall(phoneNo: phoneNo, isVideo: isVideo);
  }
}
