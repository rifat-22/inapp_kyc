library inapp_flutter_kyc;
import 'dart:convert';

import 'package:http/http.dart' as http;
import 'dart:io';
import 'package:edge_detection/edge_detection.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';

import 'liveness_cam.dart';

class ImageAndText{
  String? imagePath;
  String? extractedText;
  ImageAndText({this.imagePath, this.extractedText});
}


class EkycServices {
  final _livenessCam = LivenessCam();
  File? result;

  Future<File?> livenessDetct() async {
    var value = await _livenessCam.start();
    if (value != null) {
      result = value;
    }
    return result;
  }

  Future<ImageAndText?> openImageScanner() async {
    ImageAndText? imageText = new ImageAndText();
    bool isCameraGranted = await Permission.camera
        .request()
        .isGranted;
    if (!isCameraGranted) {
      isCameraGranted =
          await Permission.camera.request() == PermissionStatus.granted;
    }

    if (!isCameraGranted) {
      // Have not permission to camera
      return imageText;
    }

    String imagePath = join((await getApplicationSupportDirectory()).path,
        "${(DateTime
            .now()
            .millisecondsSinceEpoch / 1000).round()}.jpeg");

    try {
      //Make sure to await the call to detectEdge.
      bool success = await EdgeDetection.detectEdge(imagePath,
        canUseGallery: true,
        androidScanTitle: 'Scanning',
        // use custom localizations for android
        androidCropTitle: 'Crop',
        androidCropBlackWhiteTitle: 'Black White',
        androidCropReset: 'Reset',
      );
    } catch (e) {
      print(e);
    }

    final textRecognizer = TextRecognizer();
    final inputImage = InputImage.fromFilePath(imagePath);
    final visionText = await textRecognizer.processImage(inputImage);


    imageText =
        ImageAndText(imagePath: imagePath, extractedText: visionText.text);


    return imageText;
  }

  Future<bool?> runFaceMatch(String url, String? selfieImagePath,
      String? scannedImagePath) async {
    print(selfieImagePath);
    print(scannedImagePath);

    bool _ismatchedWithSelfie = false;

    var uri = Uri.parse(url + "/face-match");
    var request = http.MultipartRequest('POST', uri);

    request.files.add(
        await http.MultipartFile.fromPath('image1', selfieImagePath!));
    request.files
        .add(await http.MultipartFile.fromPath('image2', scannedImagePath!));

    var response = await request.send();
    print(uri);
    if (response.statusCode == 200) {
      var jsonResponse = await response.stream.bytesToString();
      var data = jsonDecode(jsonResponse);

      // Process the face match result and distance
      bool result = data['result'];
      double distance = data['distance'];
      print('Face Match Result: $result');
      print('Distance: $distance');
      print(result.toString());
      if (result == true) {
        _ismatchedWithSelfie = true;
      } else {
        _ismatchedWithSelfie = false;
      }
    } else {
      // Handle API error
      print('API Error: ${response.reasonPhrase}');
    }

    return _ismatchedWithSelfie;
  }


}



