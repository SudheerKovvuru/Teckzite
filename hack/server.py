from flask import Flask, request, jsonify
import tensorflow as tf
import numpy as np
import librosa
import soundfile as sf
import os

app = Flask(__name__)

# ✅ Load the trained emotion recognition model
try:
    model = tf.keras.models.load_model("emotion_model.h5")
    print("✅ Model loaded successfully!")
    print("✅ Model Input Shape:", model.input_shape)  # Debugging output
except Exception as e:
    print(f"❌ Error loading model: {e}")
    model = None

# ✅ Emotion labels (Must match the labels used during model training)
emotions = ["Neutral", "Happy", "Sad", "Angry", "Fear", "Surprise"]

# ✅ Function to extract features using Mel Spectrogram
def extract_features(audio_path):
    try:
        # Load audio file
        y, sr = librosa.load(audio_path, sr=16000)

        # Compute Mel Spectrogram
        mel_spec = librosa.feature.melspectrogram(y=y, sr=sr, n_mels=40, n_fft=1024, hop_length=512)
        mel_spec_db = librosa.power_to_db(mel_spec, ref=np.max)  # Convert to log scale

        # Ensure the shape is (40, 100)
        if mel_spec_db.shape[1] < 100:
            mel_spec_db = np.pad(mel_spec_db, ((0, 0), (0, 100 - mel_spec_db.shape[1])), mode="constant")
        elif mel_spec_db.shape[1] > 100:
            mel_spec_db = mel_spec_db[:, :100]

        # Reshape to match model input (40, 100, 1)
        features = mel_spec_db.reshape(1, 40, 100, 1)

        print(f"✅ Extracted features shape: {features.shape}")  # Debugging output
        return features
    except Exception as e:
        print(f"❌ Error processing audio: {e}")
        return None

# ✅ Define the prediction route
@app.route("/predict", methods=["POST"])
def predict():
    # Check if an audio file was uploaded
    if "audio" not in request.files:
        return jsonify({"error": "No audio file uploaded"}), 400  # Bad request

    file = request.files["audio"]
    
    # Ensure the file is not empty
    if file.filename == "":
        return jsonify({"error": "Empty file uploaded"}), 400

    file_path = "temp.wav"
    file.save(file_path)

    try:
        # Extract features from the uploaded audio
        features = extract_features(file_path)
        if features is None:
            return jsonify({"error": "Feature extraction failed"}), 500

        # Ensure model is loaded before making predictions
        if model is None:
            return jsonify({"error": "Model not loaded"}), 500

        # Ensure input shape matches model requirements
        if features.shape[1:] != model.input_shape[1:]:
            return jsonify({"error": f"Feature shape mismatch. Expected {model.input_shape[1:]}, got {features.shape[1:]}"}), 500

        # Make prediction
        prediction = model.predict(features)
        emotion_index = np.argmax(prediction)
        detected_emotion = emotions[emotion_index]

        # Cleanup temporary file
        os.remove(file_path)

        return jsonify({"emotion": detected_emotion})

    except Exception as e:
        return jsonify({"error": f"Internal Server Error: {str(e)}"}), 500

# ✅ Run the Flask server
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
