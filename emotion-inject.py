import requests
import json

# URL to send the POST request to
url = 'http://43.201.28.34:8080/api/v1/emotion-labels'

# Headers for the request
headers = {
    'accept': '*/*',
    'Content-Type': 'application/json'
}

# List of emotion names to send
emotion_names = [
    "기쁨",     # Joy
    "슬픔",     # Sadness
    "분노",     # Anger
    "무기력",   # Lethargy
    "평온",     # Calmness
    "두려움",   # Fear
    "놀람",     # Surprise
    "기대",     # Expectation
    "혐오"      # Disgust
]

# Function to send a POST request for each emotion
def post_emotion(name):
    # Create the payload
    payload = {
        "name": name
    }

    # Convert the payload to JSON
    json_payload = json.dumps(payload)

    # Send the POST request
    response = requests.post(url, headers=headers, data=json_payload)

    # Print the result
    print(f"Posting emotion '{name}':")
    print(f"Status Code: {response.status_code}")
    print(f"Response: {response.text}")
    print("-" * 50)

# Send a POST request for each emotion
for emotion in emotion_names:
    post_emotion(emotion)
