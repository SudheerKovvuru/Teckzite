# Women Safety App

## Overview
The Women Safety App is designed to enhance personal security by leveraging machine learning to analyze emotions from voice recordings. When a user speaks, the app detects distress or fear using a trained emotion recognition model. Based on the detected emotion, the app automatically sends an emergency alert message to a predefined emergency contact number.

## Features
- **Real-time Voice Analysis**: Listens to the user's voice and determines their emotional state.
- **Machine Learning Integration**: Uses a trained emotion detection model to recognize distress signals.
- **Emergency Alert System**: Automatically sends alerts when a distressing emotion is detected.
- **Mobile Application**: Provides a user-friendly interface for seamless interaction.
- **Backend API**: Flask-based API to handle emotion detection requests.

## Repository Structure
```
📂 Teckzite
├── 📂 hack             # Backend Flask API for ML model inference
├── 📂 main             # Android application source code
└── emotion_model.h5    # Pre-trained emotion detection ML model
```
## Usage
1. Launch the app and allow necessary permissions (Microphone, SMS, Location if applicable).
2. Click the "Start Listening" button.
3. Speak normally; the app will analyze emotions in real-time.
4. If distress is detected, an emergency alert will be sent.

## Technologies Used
- **Android (Jetpack Compose, Kotlin)** - Mobile app development.
- **Flask** - Backend API for emotion recognition.
- **TensorFlow/Keras** - Emotion detection model.
- **Librosa** - Audio processing.

## Contact
For any inquiries or suggestions, please reach out via the GitHub repository.

