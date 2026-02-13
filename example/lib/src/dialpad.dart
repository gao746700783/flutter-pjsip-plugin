import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_pjsip_plugin/model/index.dart';
import 'package:flutter_pjsip_plugin_example/src/action_button.dart';
import 'package:flutter_pjsip_plugin_example/src/models/pjsip_account_model.dart';
import 'package:logger/logger.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

class DialPadWidget extends StatefulWidget {
  const DialPadWidget({super.key});

  @override
  State<DialPadWidget> createState() => _MyDialPadWidget();
}

class _MyDialPadWidget extends State<DialPadWidget> {
  String? _dest;
  TextEditingController? _textController;
  late SharedPreferences _preferences;

  final Logger _logger = Logger();

  String? receivedMsg;

  @override
  initState() {
    super.initState();
    receivedMsg = "";
    _loadSettings();
  }

  void _loadSettings() async {
    _logger.d("loadSettings");
    _preferences = await SharedPreferences.getInstance();
    // _dest = _preferences.getString('dest') ?? 'sip:gxh@sip.linphone.org';
    _dest = _preferences.getString('dest') ?? 'sip:gaoxiaohui@sip.linphone.org';
    _textController = TextEditingController(text: _dest);
    _textController!.text = _dest!;

    setState(() {});
  }

  Future<Widget?> _handleCall(BuildContext context,
      [bool voiceOnly = false]) async {
    final accountModel = context.read<PjsipAccountModel>();

    final dest = _textController?.text;
    if (defaultTargetPlatform == TargetPlatform.android ||
        defaultTargetPlatform == TargetPlatform.iOS) {
      await Permission.microphone.request();
      await Permission.camera.request();
    }
    if (dest == null || dest.isEmpty) {
      showDialog<void>(
        context: context,
        barrierDismissible: false,
        builder: (BuildContext context) {
          return AlertDialog(
            title: Text('Target is empty.'),
            content: Text('Please enter a SIP URI or username!'),
            actions: <Widget>[
              TextButton(
                child: Text('Ok'),
                onPressed: () {
                  Navigator.of(context).pop();
                },
              ),
            ],
          );
        },
      );
      return null;
    }

    accountModel.call(dest, isVideo: !voiceOnly);
    // helper!.call(dest, voiceOnly: voiceOnly, mediaStream: mediaStream);
    _preferences.setString('dest', dest);
    return null;
  }

  void _handleBackSpace([bool deleteAll = false]) {
    var text = _textController!.text;
    if (text.isNotEmpty) {
      setState(() {
        text = deleteAll ? '' : text.substring(0, text.length - 1);
        _textController!.text = text;
      });
    }
  }

  void _handleNum(String number) {
    setState(() {
      _textController!.text += number;
    });
  }

  List<Widget> _buildNumPad() {
    final labels = [
      [
        {'1': ''},
        {'2': 'abc'},
        {'3': 'def'}
      ],
      [
        {'4': 'ghi'},
        {'5': 'jkl'},
        {'6': 'mno'}
      ],
      [
        {'7': 'pqrs'},
        {'8': 'tuv'},
        {'9': 'wxyz'}
      ],
      [
        {'*': ''},
        {'0': '+'},
        {'#': ''}
      ],
    ];

    return labels
        .map((row) => Padding(
            padding: const EdgeInsets.all(10),
            child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: row
                    .map((label) => ActionButton(
                          title: label.keys.first,
                          subTitle: label.values.first,
                          onPressed: () => _handleNum(label.keys.first),
                          number: true,
                        ))
                    .toList())))
        .toList();
  }

  List<Widget> _buildDialPad() {
    Color? textFieldColor =
        Theme.of(context).textTheme.bodyMedium?.color?.withValues(alpha: 0.5);
    Color? textFieldFill =
        Theme.of(context).buttonTheme.colorScheme?.surfaceContainerLowest;
    return [
      Align(
        alignment: AlignmentDirectional.centerStart,
        child: Text('Destination URL'),
      ),
      const SizedBox(height: 8),
      TextField(
        keyboardType: TextInputType.text,
        textAlign: TextAlign.center,
        style: TextStyle(fontSize: 18, color: textFieldColor),
        maxLines: 2,
        decoration: InputDecoration(
          filled: true,
          fillColor: textFieldFill,
          border: OutlineInputBorder(
            borderSide: BorderSide(color: Colors.blue.withValues(alpha: 0.5)),
            borderRadius: BorderRadius.circular(5),
          ),
          enabledBorder: OutlineInputBorder(
            borderSide: BorderSide(color: Colors.blue.withValues(alpha: 0.5)),
            borderRadius: BorderRadius.circular(5),
          ),
          focusedBorder: OutlineInputBorder(
            borderSide: BorderSide(color: Colors.blue.withValues(alpha: 0.5)),
            borderRadius: BorderRadius.circular(5),
          ),
        ),
        controller: _textController,
      ),
      SizedBox(height: 20),
      Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisAlignment: MainAxisAlignment.center,
        children: _buildNumPad(),
      ),
      Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: <Widget>[
            ActionButton(
              icon: Icons.videocam,
              onPressed: () => _handleCall(context),
            ),
            ActionButton(
              icon: Icons.dialer_sip,
              fillColor: Colors.green,
              onPressed: () => _handleCall(context, true),
            ),
            ActionButton(
              icon: Icons.keyboard_arrow_left,
              onPressed: () => _handleBackSpace(),
              onLongPress: () => _handleBackSpace(true),
            ),
          ],
        ),
      ),
    ];
  }

  @override
  Widget build(BuildContext context) {
    final accountModel = context.watch<PjsipAccountModel>();
    Color? textColor = Theme.of(context).textTheme.bodyMedium?.color;

    return Scaffold(
      appBar: AppBar(
        title: Text("PJSip Dial Pad"),
      ),
      body: ListView(
        padding: EdgeInsets.symmetric(horizontal: 12),
        children: <Widget>[
          SizedBox(height: 8),
          Center(
              child: Text(
            'Register Status: ${accountModel.loginStatus.getLoginStatusString()}',
            style: TextStyle(fontSize: 18, color: textColor),
          )),
          SizedBox(height: 8),
          Center(
            child: Text(
              'Received Message: $receivedMsg',
              style: TextStyle(fontSize: 16, color: textColor),
            ),
          ),
          SizedBox(height: 8),
          Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            mainAxisAlignment: MainAxisAlignment.center,
            children: _buildDialPad(),
          ),
        ],
      ),
    );
  }
}
