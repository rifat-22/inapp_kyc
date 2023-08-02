# inapp_flutter_kyc
The inapp_flutter_kyc package is a powerful and easy-to-use plugin that brings essential Know Your Customer (KYC) functionalities to your Flutter applications. Designed to streamline user identity verification and enhance security, this package offers a comprehensive set of features for performing KYC checks seamlessly within your app.


## Features

### Liveness Detection:
The package allows you to perform real-time liveness detection using the device's camera. Ensure that the user is physically present during the KYC process, preventing fraudulent attempts.

### ID Scanning:
Utilize advanced computer vision techniques to extract information from official identification documents such as passports, driver's licenses, national IDs, and more. Accelerate the KYC process with reliable and accurate data extraction.

### Face Matching:
Enhance identity verification by comparing the facial features between the scanned ID image and the selfie image provided by the user.

### Customizable UI:
Tailor the user interface to match your app's branding and design seamlessly. Customize colors, fonts, and layouts to create a cohesive user experience.

### Privacy and Security:
We prioritize the privacy and security of your users. Our package implements industry-standard security measures to protect sensitive user data during the KYC process.

## Getting started
To get started with the ekyc_flutter package, follow these simple steps:
```
flutter pub add inapp_flutter_kyc
```
### Android
Add this before `<application></application>` in your android/app/src/main/AndroidManifest.xml
```
<uses-feature
        android:name="android.hardware.camera"
        android:required="false" />
    <uses-permission android:name="android.permission.CAMERA" />
```
in `<activity>` add this 
```
android:requestLegacyExternalStorage="true"
```
in your android/app/build.gradle make minSdkVersion 23
```
defaultConfig {
        minSdkVersion 23
    }
```
in android/build.gradle make sure the kotlin version to 1.8.0
```
buildscript {
    ext.kotlin_version = '1.8.0'
}
```

### ID Scanning
For ID Card Scanning, two cases have considered, where the keyword and value are inline (image 1) and the keyword and the value are in the next line.
<p align="center">
  <img src="./images/inline_id.png" alt="Image 1" width="400"/>
  <img src="./images/nextLine_id.png" alt="Image 2" width="400"/>
</p>

[//]: # (![Image 1]&#40;./images/inline_id.png&#41; ![Image 2]&#40;./images/nextLine_id.png&#41;)

in EkycServices().openImageScanner() function, pass the keyword name and a boolean
-> if the keyword and the value are inline pass true
-> if the keyword and the value are not inline pass false
For example 
```
Map<String, bool> keyWordData = {
    'Name' : false,
    'Date of Birth' : true,
    'NID No' : false
  };
```
Now pass this keyWordData to EkycServices().openImageScanner() 
```
 ExtractedDataFromId? extractedDataFromId;
 extractedDataFromId = await EkycServices().openImageScanner(keyWordData);
```
The `ExtractedDataFromId` also contains `extractedText`. If the ocr text doesn't get parsed from these cases, you can also manipulate the text from the `extractedText`
### Face Matching:
For running face match go to [https://drive.google.com/drive/folders/1Po7VxJsUcH_W0XOenUHzg_IDu1s3_do8?usp=sharing](https://drive.google.com/drive/folders/1Po7VxJsUcH_W0XOenUHzg_IDu1s3_do8?usp=sharing)
Download the folder and in the folder directory run this (for ubuntu)
```
python3 face_match.py
```
for windows
```
python face_match.py
```
you will see something like this in your console 
```
 * Running on all addresses (0.0.0.0)
 * Running on http://127.0.0.1:5000
 * Running on http://10.0.3.50:5000
```
copy the last address and pass it in EkycServices().runFaceMatch function along with the two imagepath (selfie image and id image) like this

```
await EkycServices().runFaceMatch("http://10.0.3.50:5000", selfieImage?.path, imageAndText?.imagePath);
```