import 'package:flutter/material.dart';
import 'package:flutter_pjsip_plugin/model/index.dart';
import 'package:flutter_pjsip_plugin_example/src/models/pjsip_account_model.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

class RegisterWidget extends StatefulWidget {
  // final SIPUAHelper? _helper;

  // RegisterWidget(this._helper, {Key? key}) : super(key: key);
  const RegisterWidget({super.key});

  @override
  State<RegisterWidget> createState() => _MyRegisterWidget();
}

class _MyRegisterWidget extends State<RegisterWidget> {
  final TextEditingController _sipUriController = TextEditingController();
  final TextEditingController _portController = TextEditingController();
  final TextEditingController _displayNameController = TextEditingController();
  final TextEditingController _userController = TextEditingController();
  final TextEditingController _pwdController = TextEditingController();
  late SharedPreferences _preferences;

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  @override
  void dispose() {
    _pwdController.dispose();
    _userController.dispose();
    _sipUriController.dispose();
    _displayNameController.dispose();
    super.dispose();
  }

  @override
  void deactivate() {
    super.deactivate();
    _saveSettings();
  }

  void _loadSettings() async {
    _preferences = await SharedPreferences.getInstance();
    setState(() {
      _portController.text = '5060'; //8070
      _sipUriController.text = _preferences.getString('sip_uri') ??
          'sip.linphone.org'; // '10.2.2.73';
      _displayNameController.text =
          _preferences.getString('display_name') ?? 'Flutter SIP UA';
      _pwdController.text = _preferences.getString('password') ?? 'XXX';
      _userController.text = _preferences.getString('auth_user') ?? 'XXX';
    });
  }

  void _saveSettings() {
    _preferences.setString('port', _portController.text);
    _preferences.setString('sip_uri', _sipUriController.text);
    _preferences.setString('display_name', _displayNameController.text);
    _preferences.setString('password', _pwdController.text);
    _preferences.setString('auth_user', _userController.text);
  }

  void _alert(BuildContext context, String alertFieldName) {
    showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext context) {
        return AlertDialog(
            title: Text('$alertFieldName is empty'),
            content: Text('Please enter $alertFieldName!'),
            actions: <Widget>[
              TextButton(
                child: Text('Ok'),
                onPressed: () {
                  Navigator.of(context).pop();
                },
              ),
            ]);
      },
    );
  }

  void _register(BuildContext context, PjsipAccountModel accountModel) {
    if (_sipUriController.text == '') {
      _alert(context, "SIP URI");
    }

    _saveSettings();
    accountModel.register(
        sipUri: _sipUriController.text,
        port: _portController.text,
        displayName: _displayNameController.text,
        password: _pwdController.text,
        authUser: _userController.text);
  }

  @override
  Widget build(BuildContext context) {
    final accountModel = context.watch<PjsipAccountModel>();

    Color? textColor = Theme.of(context).textTheme.bodyMedium?.color;
    Color? textFieldFill =
        Theme.of(context).buttonTheme.colorScheme?.surfaceContainerLowest;

    OutlineInputBorder border = OutlineInputBorder(
      borderSide: BorderSide.none,
      borderRadius: BorderRadius.circular(5),
    );
    Color? textLabelColor =
        Theme.of(context).textTheme.bodyMedium?.color?.withValues(alpha: 0.5);
    return Scaffold(
      appBar: AppBar(
        title: Text("SIP Account Register"),
      ),
      bottomNavigationBar: Padding(
        padding: const EdgeInsets.symmetric(vertical: 18, horizontal: 20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Row(
              children: [
                Expanded(
                  child: SizedBox(
                    height: 40,
                    child: ElevatedButton(
                      child: Text('Register'),
                      onPressed: () => _register(context, accountModel),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.symmetric(vertical: 18, horizontal: 20),
        children: <Widget>[
          Center(
              child: Text(
            'Register Status: ${accountModel.loginStatus.getLoginStatusString()}',
            style: TextStyle(fontSize: 18, color: textColor),
          )),
          SizedBox(height: 15),
          Text('Port', style: TextStyle(color: textLabelColor)),
          SizedBox(height: 5),
          TextFormField(
            controller: _portController,
            keyboardType: TextInputType.text,
            textAlign: TextAlign.center,
            decoration: InputDecoration(
              filled: true,
              fillColor: textFieldFill,
              border: border,
              enabledBorder: border,
              focusedBorder: border,
            ),
          ),
          SizedBox(height: 15),
          Text('SIP URI', style: TextStyle(color: textLabelColor)),
          SizedBox(height: 5),
          TextFormField(
            controller: _sipUriController,
            keyboardType: TextInputType.text,
            autocorrect: false,
            textAlign: TextAlign.center,
            decoration: InputDecoration(
              filled: true,
              fillColor: textFieldFill,
              border: border,
              enabledBorder: border,
              focusedBorder: border,
            ),
          ),
          SizedBox(height: 15),
          Text('Authorization User', style: TextStyle(color: textLabelColor)),
          SizedBox(height: 5),
          TextFormField(
            controller: _userController,
            keyboardType: TextInputType.text,
            autocorrect: false,
            textAlign: TextAlign.center,
            decoration: InputDecoration(
              filled: true,
              fillColor: textFieldFill,
              border: border,
              enabledBorder: border,
              focusedBorder: border,
              hintText: _userController.text.isEmpty ? '[Empty]' : null,
            ),
          ),
          SizedBox(height: 15),
          Text('Password', style: TextStyle(color: textLabelColor)),
          SizedBox(height: 5),
          TextFormField(
            controller: _pwdController,
            keyboardType: TextInputType.text,
            autocorrect: false,
            textAlign: TextAlign.center,
            decoration: InputDecoration(
              filled: true,
              fillColor: textFieldFill,
              border: border,
              enabledBorder: border,
              focusedBorder: border,
              hintText: _pwdController.text.isEmpty ? '[Empty]' : null,
            ),
          ),
          SizedBox(height: 15),
          Text('Display Name', style: TextStyle(color: textLabelColor)),
          SizedBox(height: 5),
          TextFormField(
            controller: _displayNameController,
            keyboardType: TextInputType.text,
            textAlign: TextAlign.center,
            decoration: InputDecoration(
              filled: true,
              fillColor: textFieldFill,
              border: border,
              enabledBorder: border,
              focusedBorder: border,
              hintText: _displayNameController.text.isEmpty ? '[Empty]' : null,
            ),
          ),
        ],
      ),
    );
  }
}
