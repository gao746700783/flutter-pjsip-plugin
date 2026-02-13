import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_pjsip_plugin/flutter_pjsip_plugin.dart';

class PlatformVersionModel extends ChangeNotifier {
  String _platformVersion = 'Unknown';
  final FlutterPjsipPlugin _plugin = FlutterPjsipPlugin();

  String get platformVersion => _platformVersion;

  Future<void> init() async {
    await fetchPlatformVersion();
  }

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
}
