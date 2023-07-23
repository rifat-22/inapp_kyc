from facematch.face import match
import cv2
from PIL import Image
from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/face-match', methods=['POST'])
def face_match():
    image1 = request.files['image1']
    image2 = request.files['image2']


    image1.save('input_1.jpeg')
    image2.save('input_2.jpeg')

    def crop_face_n_save(image_path_to_crop):
        face_to_crop = cv2.imread(image_path_to_crop)
        gray = cv2.cvtColor(face_to_crop, cv2.COLOR_BGR2GRAY)

        # read the haarcascade to detect the faces in an image
        face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_alt.xml')
        faces = face_cascade.detectMultiScale(gray, 1.1, 4)
        print('Number of detected faces:', len(faces))

        largest_face = None
        largest_area = 0
        # loop over all detected faces
        if len(faces) > 0:
            for i, (x,y,w,h) in enumerate(faces):
      
                # To draw a rectangle in a face
                cv2.rectangle(face_to_crop,(x,y),(x+w,y+h),(0,255,255),2)
                # Check if current face has a larger area than the previous largest face
                if w * h > largest_area:
                    largest_face = face_to_crop[y:y+h, x:x+w]
                    largest_area = w * h
                
            if largest_face is not None:
                cv2.imwrite(f'{image_path_to_crop}', largest_face)
                print(f"{image_path_to_crop} is saved")



    def get_image_size(image_path):
        with Image.open(image_path) as img:
            width, height = img.size
        return width, height

    def resize_image(image_path, new_size):
        with Image.open(image_path) as img:
            resized_img = img.resize(new_size)
            resized_img.save("%s"%(image_path))  # Save the resized image to a file


    # Example usage
    image1_path = "input_1.jpeg"
    image2_path = "input_2.jpeg"

    crop_face_n_save(image1_path)
    crop_face_n_save(image2_path)


    image1_width, image1_height = get_image_size(image1_path)
    image2_width, image2_height = get_image_size(image2_path)

    if image1_width * image1_height > image2_width * image2_height:
        resize_image(image2_path, (image1_width, image1_width))
    elif image1_width * image1_height < image2_width * image2_height:
        resize_image(image1_path, (image2_width, image2_width))


    f = open(image1_path, 'rb')
    data1 = f.read()
    f.close()


    f = open(image2_path, 'rb')
    data2 = f.read()
    f.close()

    result, distance, data = match(data1, data2)

    f = open('out.png', 'wb')
    f.write(data)
    f.close()

    print(distance)
    print(result)

    result_bool = bool(result)

    if result is None:
        return jsonify({'error': 'Face matching failed'}), 500

    return jsonify({'result': result_bool, 'distance': distance})


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)