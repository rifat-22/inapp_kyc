// import 'package:flutter/material.dart';
//
// class DynamicForm extends StatelessWidget {
//   final List<FormFieldData> formFields;
//
//   DynamicForm({required this.formFields});
//
//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//       appBar: AppBar(title: Text('Dynamic Form')),
//       body: Padding(
//         padding: const EdgeInsets.all(16.0),
//         child: ListView.builder(
//           itemCount: formFields.length,
//           itemBuilder: (context, index) {
//             FormFieldData fieldData = formFields[index];
//             return TextFormField(
//               initialValue: fieldData.value,
//               decoration: InputDecoration(labelText: fieldData.label),
//               // Implement any validation or other form handling logic here
//               onChanged: (value) {
//                 // Update the value in the list whenever the form field changes
//                 formFields[index].value = value;
//               },
//             );
//           },
//         ),
//       ),
//     );
//   }
// }
