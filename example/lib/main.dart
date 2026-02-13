import 'package:flutter/material.dart';
import 'package:flutter_pjsip_plugin_example/src/models/pjsip_account_model.dart';
import 'package:flutter_pjsip_plugin_example/src/models/platform_version_model.dart';
import 'package:flutter_pjsip_plugin_example/src/about.dart';
import 'package:flutter_pjsip_plugin_example/src/dialpad.dart';
import 'package:flutter_pjsip_plugin_example/src/register.dart';
import 'package:provider/provider.dart';

void main() {
  runApp(MultiProvider(
    providers: [
      ChangeNotifierProvider<PlatformVersionModel>(
        create: (context) => PlatformVersionModel()..init(),
      ),
      ChangeNotifierProvider(create: (_) => PjsipAccountModel()),
    ],
    child: const MyApp(),
  ));
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Plugin example app',
      initialRoute: '/',
      routes: {
        '/': (context) => const HomePage(),
        '/about': (context) => const AboutWidget(),
        '/register': (context) => const RegisterWidget(),
        '/dial': (context) => DialPadWidget(),
      },
    );
  }
}

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final model = context.watch<PlatformVersionModel>();
    final accountModel = context.read<PjsipAccountModel>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('Running on: ${model.platformVersion}\n'),
            const SizedBox(height: 12),
            ElevatedButton(
              onPressed: () =>
                  context.read<PlatformVersionModel>().fetchPlatformVersion(),
              child: const Text('Refresh'),
            ),
            const SizedBox(height: 12),
            ElevatedButton(
              onPressed: () => Navigator.pushNamed(context, '/about'),
              child: const Text('Go to About'),
            ),
            const SizedBox(height: 12),
            ElevatedButton(
              onPressed: () {
                accountModel.init();
              },
              child: const Text('Try init'),
            ),
            const SizedBox(height: 12),
            ElevatedButton(
              onPressed: () => Navigator.pushNamed(context, '/register'),
              child: const Text('Go to Register'),
            ),
            const SizedBox(height: 12),
            ElevatedButton(
              onPressed: () => Navigator.pushNamed(context, '/dial'),
              child: const Text('Go to Dialpad'),
            ),
          ],
        ),
      ),
    );
  }
}
