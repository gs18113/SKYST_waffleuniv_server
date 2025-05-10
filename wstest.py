import websocket
import stomp
import json
import time
import sys
import ssl
import logging
from threading import Thread

# Set up logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# WebSocket connection details
HOST = "localhost"  # Change to your CloudFront domain or server address
PORT = 8080         # Your server port (8080 as requested)
ENDPOINT = f"ws://{HOST}:{PORT}/ws"  # WebSocket endpoint from your Spring config

# For secure connections (wss://)
# ENDPOINT = f"wss://{HOST}/ws"

# STOMP subscription details
TOPIC_DESTINATION = "/topic/notifications"  # For broadcast messages
QUEUE_DESTINATION = "/user/queue/notifications"  # For user-specific messages
USER_ID = "1"  # Test user ID

class WebSocketClient:
    def __init__(self):
        self.conn = None
        self.connected = False
        self.received_messages = []
        self.connection_error = None

    def connect(self):
        try:
            # Create WebSocket connection
            ws = websocket.create_connection(ENDPOINT)

            # Create STOMP connection over WebSocket
            self.conn = stomp.Connection(host_and_ports=[(HOST, PORT)]) #, websocket=True)

            # Set up listeners
            self.conn.set_listener('test_listener', self.StompListener(self))

            # Connect to STOMP server
            self.conn.connect(wait=True)
            logger.info("Connected to STOMP server")

            # Subscribe to topics
            self.conn.subscribe(destination=TOPIC_DESTINATION, id=1, ack='auto')
            logger.info(f"Subscribed to broadcast topic: {TOPIC_DESTINATION}")

            self.conn.subscribe(destination=QUEUE_DESTINATION, id=2, ack='auto')
            logger.info(f"Subscribed to user queue: {QUEUE_DESTINATION}")

            self.connected = True
            return True

        except Exception as e:
            self.connection_error = str(e)
            logger.error(f"Connection error: {e}")
            return False

    def disconnect(self):
        if self.conn and self.connected:
            self.conn.disconnect()
            logger.info("Disconnected from STOMP server")
            self.connected = False

    def send_message(self, destination, message):
        if self.conn and self.connected:
            self.conn.send(
                destination=destination,
                body=json.dumps(message),
                headers={'content-type': 'application/json'}
            )
            logger.info(f"Sent message to {destination}: {message}")
            return True
        else:
            logger.error("Not connected to STOMP server")
            return False

    class StompListener(stomp.ConnectionListener):
        def __init__(self, client):
            self.client = client

        def on_message(self, frame):
            message = frame.body
            logger.info(f"Received message: {message}")
            try:
                json_message = json.loads(message)
                self.client.received_messages.append(json_message)
            except json.JSONDecodeError:
                logger.warning(f"Received non-JSON message: {message}")
                self.client.received_messages.append(message)

        def on_error(self, frame):
            logger.error(f"Error: {frame.body}")

        def on_disconnected(self):
            logger.info("Disconnected")
            self.client.connected = False

def run_tests():
    client = WebSocketClient()

    # Test 1: Connection
    logger.info("Test 1: Testing connection...")
    if not client.connect():
        logger.error(f"Failed to connect: {client.connection_error}")
        return False

    # Test 2: Send a notification
    logger.info("Test 2: Sending a test notification...")
    test_message = {
        "userId": USER_ID,
        "message": "Test notification from Python client",
        "timestamp": int(time.time() * 1000)
    }

    success = client.send_message("/app/notification", test_message)
    if not success:
        logger.error("Failed to send message")
        client.disconnect()
        return False

    # Wait for response
    logger.info("Waiting for response...")
    timeout = 10  # seconds
    start_time = time.time()

    while time.time() - start_time < timeout:
        if client.received_messages:
            logger.info("Test passed! Received response.")
            logger.info(f"Messages received: {client.received_messages}")
            client.disconnect()
            return True
        time.sleep(0.5)

    logger.error("Test failed: No response received within timeout period")
    client.disconnect()
    return False

if __name__ == "__main__":
    try:
        # Uncomment for debugging WebSocket
        # websocket.enableTrace(True)

        success = run_tests()
        if success:
            logger.info("All tests passed!")
            sys.exit(0)
        else:
            logger.error("Tests failed")
            sys.exit(1)
    except KeyboardInterrupt:
        logger.info("Tests interrupted")
        sys.exit(1)
    except Exception as e:
        logger.error(f"Unexpected error: {e}")
        sys.exit(1)
