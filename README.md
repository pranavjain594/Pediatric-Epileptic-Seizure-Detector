## Product Link
*Apk file download link* :- [*epilepsydetector.apk*](https://drive.google.com/file/d/1F9dxHHHTRm5XUWBFWyqAu1YlHmUfbT7Q/view?usp=sharing)

## Product Perspective

The PEDIATRIC EPILEPTIC SEIZURE DETECTOR helps the health-care professionals to predict the actual time of epileptic seizure in the patient more accurately and in a bias-free manner by computerizing the prediction process.

Predictions or results will be generated from the EEG recordings which will help the user to make appropriate decisions regarding the course of their treatment. It will help medical personnel to determine the presence of seizure in an EEG recording by allowing them to upload specific files generated from multiple electrode channels.
The app will display occurrence of seizure by indicating the exact sample value corresponding to the EEG recording.

## Product Functionality

All the functions of the system will be performed in this order-

1. Get the EEG recordings
2. Upload the CSV file
3. Read the CSV file
4. Evaluate using the saved model
5. Determine and analyze the output
6. Display the real time output

## Product Design

The proposed idea was implemented using Java in the form of an android application having an interface and made using the Model-View-Controller (MVC) design pattern. The application is ”apk” installable. The Model and the View can run independently using the MVC pattern. The Model module can be used separately in other programs.


## The MVC Model

Model-view-controller (MVC) is an architectural pattern widely used to design user interfaces, which divides an application into three interrelated components. This is done to distinguish internal information representations from how information is communicated to and accepted from the user. The design pattern of MVC decouples these major components to allow efficient reuse of code and also allows parallel development of each of the component. This architecture is mostly used for desktop graphical user interfaces (GUIs) and is popular in designing web applications.


## The Model

A Model is the application's principal central component. It receives user inputs and commands via the View component and uses the logic to generate outputs, which is shown again via the View component. This Model comprises of various sub-models which represent each disease. In this project, I have used my own custom CNN model.


Epilepsy model

For designing the epilepsy CNN model, I have taken a dataset comprising of a CSV file of around 2 lac recordings. The dataset is then divided into the train set and test set in 4:1 ratio. Then Convolutional Neural Network (CNN) has been applied, having 10 convolutional layers and one fully-connected layer. ‘relu’ and ‘sigmoid’ activation functions are applied to the hidden layers and the output layer respectively. While compiling ‘binary_crossentropy’ has been used as the loss function and ‘adam’ as an optimization function. This model is then converted into Tensorflow Lite(.tflite) format before using it in Android Studio.

The trained model has achieved an accuracy of 96% on the validation dataset.



## The View

A View is something available to the user. It reflects the user interface with which the user is communicating while using an application. While the View has buttons, it, itself remains unaware of the fundamental interaction that exists with the back-end. It helps UI / UX people to operate in parallel with the people at the back-end of the user interface.

## The Controller

A Controller is a master that synchronizes the Model along with View. It obtains the user's interaction with the View, transmits them on to the Model that then processes the input information for output production. Through the View, the outputs (results) are then shown to the user.

## How 'MVC' fits into this project

The View in this project is represented by the android application interface where the user can upload the EEG recording and generate the output. The results will be displayed to the user through this interface only. When the user will press the detect button available on the screen, a function will be called at the backend which in turn will call other functions each having the unique functionality i.e., open the file, read it, import the model and interpret it, finding the output etc. The function calls will work as controller in my project. The custom CNN model present in the firebase repository and also in the App's assets folder, will be imported by the controller at the time function calls. With the help of these, we can alter and run the GUI component separately without compromising the functionality of the other components. Similarly, by importing separately from the GUI we can modify the Detector component and use it as a module as well.

## Working

On opening the application, the main activity displays a graphView, a upload button, and a detect button. When the user clicks on upload button, it calls upon the epilpesy() function which reads the path of the CSV file that is to be uploaded using Intent.ACTION_OPEN_DOCUMENT.

When the user clicks on the ‘Detect Epilepsy’ button, the button calls upon the detect() function which then retrieves the path of the uploaded file, reads it, and takes it as an input to the saved custom tflite model. The model's tflite file is saved in both Google Firebase console as well as in App's asset folder. This saved CNN model is then used to predict the output. The result is displayed to the user on the screen using the graphView.

## Installation

The application is “apk” installable and can be installed in the following way-

1. Click on the package named “epilpesydetector.apk”, and then press the Install button.

2. You will see the application package installing and after it is done press Close.

3. Open the “Pediatric Epileptic Seizure Detector” to run the application.


(**Note:** The custom CNN models are not uploaded in the repository.)
