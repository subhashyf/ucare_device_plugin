import 'dart:async';

import 'package:flutter/services.dart';

class UcareDevicePlugin {
  static const MethodChannel _channel =
      const MethodChannel('ucare_device_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<dynamic> get getPairedDevices async {
    final List<dynamic> pairedDevices = await _channel.invokeMethod('getPairedDevices');
    return pairedDevices;
  }

  static Future<dynamic> get scanForDevice async {
    final List<dynamic> scannedDevices = await _channel.invokeMethod('scanForDevice');
    return scannedDevices;
  }
}
