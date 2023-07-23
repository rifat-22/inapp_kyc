import 'dart:io';
import 'package:flutter/services.dart';


class LivenessCam {
  final _methodChannel = const MethodChannel('liveness_cam');

  ///starting camera
  Future<File?> start() async {
    print("object");
    try {
      var result = await _methodChannel.invokeMethod("start");
      if ("$result" != "null"&&"$result" != "") {
        return File("$result".replaceAll("file:/", ""));
      }
    } catch (e) {
      print("catched");
      return null;
    }
    return null;
  }
}
