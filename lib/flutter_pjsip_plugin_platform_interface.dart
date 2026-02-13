import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_pjsip_plugin_method_channel.dart';
import 'model/index.dart';

abstract class FlutterPjsipPluginPlatform extends PlatformInterface {
  /// Constructs a FlutterPjsipPluginPlatform.
  FlutterPjsipPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterPjsipPluginPlatform _instance =
      MethodChannelFlutterPjsipPlugin();

  /// The default instance of [FlutterPjsipPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterPjsipPlugin].
  static FlutterPjsipPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterPjsipPluginPlatform] when
  /// they register themselves.
  static set instance(FlutterPjsipPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future loginSip(String userName, String displayName, String password,
      String sipIp, String port) {
    throw UnimplementedError('sipLogin() has not been implemented.');
  }

  Future init() {
    throw UnimplementedError('init() has not been implemented.');
  }

  Future initWithCb(LoginStatusChangeCallback callback) {
    throw UnimplementedError('init() has not been implemented.');
  }

  Future logoutSip() {
    throw UnimplementedError('logoutSip() has not been implemented.');
  }

  Future makeCall(String phoneNo, bool isVideo) {
    throw UnimplementedError('makeCall() has not been implemented.');
  }

  Future setAutoAnswer(bool autoAnswer) {
    throw UnimplementedError('setAutoAnswer() has not been implemented.');
  }

  Future<String> getMacAddress() {
    throw UnimplementedError('getMacAddress() has not been implemented.');
  }

  // void onLoginStatusChange(LoginStatusChangeCallback callback) {
  //   throw UnimplementedError('onLoginStatusChange() has not been implemented.');
  // }
}
