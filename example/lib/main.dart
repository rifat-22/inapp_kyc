import 'dart:io';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:inapp_flutter_kyc/inapp_flutter_kyc.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'EKYC Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  File? selfieImage;
  ExtractedDataFromId? extractedDataFromId;
  bool? isMatchFace;
  bool isloading = false;
  bool faceMatchButtonPressed = false;
  Map<String, bool> keyWordData = {
    'Name': false,
    'Date of Birth': true,
    'NID No': false
  };

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            TextButton(
              onPressed: () {
                EkycServices().livenessDetct().then((result) {
                  if (result != null) {
                    print("File path: $result");
                    setState(() {
                      selfieImage = result;
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                            builder: (context) =>
                                ShowImage(selfieImage: selfieImage)),
                      );
                    });
                  } else {
                    print("Liveness detection failed.");
                  }
                }).catchError((error) {
                  print("Error occurred during liveness detection: $error");
                });
              },
              style: TextButton.styleFrom(
                foregroundColor: Colors.white,
                padding: const EdgeInsets.all(16.0),
                backgroundColor: Colors.blue,
                elevation: 9.0,
                textStyle: const TextStyle(
                  fontSize: 20,
                ),
              ),
              child: Text("Liveness Detection"),
            ),
            SizedBox(
              height: 10,
            ),
            TextButton(
              onPressed: () async {
                extractedDataFromId =
                    await EkycServices().openImageScanner(keyWordData);
                if (extractedDataFromId?.extractedText != null) {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (context) => ShowScannedText(
                              scannedText: extractedDataFromId!.extractedText!,
                              keyNvalue: extractedDataFromId?.keywordNvalue,
                            )),
                  );
                }
              },
              style: TextButton.styleFrom(
                foregroundColor: Colors.white,
                padding: const EdgeInsets.all(16.0),
                backgroundColor: Colors.blue,
                elevation: 9.0,
                textStyle: const TextStyle(
                  fontSize: 20,
                ),
              ),
              child: Text("Scan your Id"),
            ),
            SizedBox(
              height: 10,
            ),
            TextButton(
                onPressed: () async {
                  if (selfieImage == null) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text(
                            'Capture a selfie first using liveness detection'),
                        duration: Duration(seconds: 3),
                      ),
                    );
                  } else if (extractedDataFromId?.imagePath == null) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text('There is no face detected in Id card'),
                        duration: Duration(seconds: 3),
                      ),
                    );
                  } else {
                    isloading = true;
                    setState(() {
                      faceMatchButtonPressed = true;
                    });

                    isMatchFace = await EkycServices().runFaceMatch(
                        "http://10.255.187.82:5000",
                        selfieImage?.path,
                        extractedDataFromId?.imagePath);
                    setState(() {
                      isloading = false;
                    });
                  }
                },
                style: TextButton.styleFrom(
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.all(16.0),
                  backgroundColor: Colors.blue,
                  elevation: 9.0,
                  textStyle: const TextStyle(
                    fontSize: 20,
                  ),
                ),
                child: Text("Face match with Id")),
            Visibility(
              visible: faceMatchButtonPressed,
              child: Container(
                width: double.infinity,
                // padding: EdgeInsets.fromLTRB(8, 8, 8, 0),
                child: Card(
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.all(Radius.circular(15))),
                  color: Color(0xFFe3e6f5),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      SizedBox(
                        height: 15,
                      ),
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.center,
                        children: [
                          SizedBox(
                            width: 10,
                          ),
                          (isloading == true)
                              ? SizedBox(
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                    valueColor:
                                        AlwaysStoppedAnimation(Colors.white),
                                  ),
                                  height: 50.0,
                                  width: 50.0,
                                )
                              : (isMatchFace == true)
                                  ? Icon(
                                      Icons.check_circle_sharp,
                                      size: 40,
                                      color: Color(0xFF9677eca),
                                    )
                                  : Transform.rotate(
                                      angle: 45 * pi / 180,
                                      child: Icon(
                                        Icons.add_circle,
                                        size: 40,
                                        color: Colors.red,
                                      ),
                                    ),
                          SizedBox(
                            width: 5,
                          ),
                          Expanded(
                            child: Text(
                                (isloading == true)
                                    ? '  Running face match...'
                                    : (isMatchFace == true)
                                        ? "Successful!!! ID Face matches with Selfie"
                                        : (isMatchFace == false)
                                            ? "Something is wrong! Please try again! "
                                            : 'NID Face does not match with Selfie',
                                maxLines: 3,
                                textAlign: TextAlign.left,
                                style: TextStyle(
                                    fontSize: 17,
                                    height: 1.5,
                                    fontWeight: FontWeight.w400)),
                          ),
                          const SizedBox(
                            width: 10,
                          ),
                        ],
                      ),
                      SizedBox(
                        height: 20,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}

class ShowImage extends StatelessWidget {
  File? selfieImage;
  ShowImage({this.selfieImage});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Liveness Detect succesful!"),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Image.file(selfieImage!),
            SizedBox(
              height: 20,
            ),
            TextButton(
              onPressed: () {
                Navigator.pop(context);
              },
              style: TextButton.styleFrom(
                foregroundColor: Colors.white,
                padding: const EdgeInsets.all(16.0),
                backgroundColor: Colors.blue,
                elevation: 9.0,
                textStyle: const TextStyle(
                  fontSize: 20,
                ),
              ),
              child: Text("Retake"),
            ),
          ],
        ),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}

class FormFieldData {
  final String label;
  String value;

  FormFieldData({required this.label, required this.value});
}

class ShowScannedText extends StatefulWidget {
  String scannedText;
  Map<String, dynamic>? keyNvalue;

  ShowScannedText({required this.scannedText, this.keyNvalue});

  @override
  State<ShowScannedText> createState() => _ShowScannedTextState();
}

class _ShowScannedTextState extends State<ShowScannedText> {
  List<FormFieldData> formFields = [];
  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    if (widget.keyNvalue != null) {
      widget.keyNvalue?.forEach((key, value) {
        formFields.add(FormFieldData(label: key, value: value.toString()));
      });
    }
    print(widget.keyNvalue);
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return Scaffold(
      appBar: AppBar(
        title: Text("Sccaned Text"),
      ),
      body: Column(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Visibility(
            visible: widget.keyNvalue != null,
            child: Expanded(
              child: ListView.builder(
                shrinkWrap: true,
                itemCount: formFields.length,
                itemBuilder: (context, index) {
                  FormFieldData fieldData = formFields[index];
                  return Padding(
                    padding: EdgeInsets.all(16),
                    child: TextFormField(
                      initialValue: fieldData.value,
                      decoration: InputDecoration(labelText: fieldData.label),
                      onChanged: (value) {
                        formFields[index].value = value;
                      },
                    ),
                  );
                },
              ),
            ),
          ),
          Text(
            "Full Extracted Text",
            style: TextStyle(fontSize: 20),
          ),
          SizedBox(
            height: 20,
          ),
          Padding(
            padding: const EdgeInsets.only(bottom: 20.0),
            child: Text(widget.scannedText),
          ),
        ],
      ),
    );
  }
}
