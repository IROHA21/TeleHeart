from telethon import TelegramClient
import asyncio

# Replace with your own API credentials
API_ID = 25016078
API_HASH = "6e818b2e5b0e074c403e3bb120f64736"
SESSION_FILE = None  # Will be set dynamically

chat = []
messagess = []
loop = asyncio.new_event_loop()
client = None

def set_session_path(path):
    global SESSION_FILE
    SESSION_FILE = path

async def send_otp_async(phone):
    global client
    try:
        # Initialize the client with the session file
        if client is None:
            client = TelegramClient(SESSION_FILE, API_ID, API_HASH, loop=loop)

        await client.connect()

        # Check if user is already authorized (logged in)
        if not await client.is_user_authorized():
            await client.send_code_request(phone)  # Send OTP to the phone number
            return "OTP has been sent to your Telegram app. Please check."

        return "Already authorized. No need for OTP."

    except Exception as e:
        return f"Error: {str(e)}"

async def send_code(code, phone):
    global client
    try:
        if client is None:
            return "Client not initialized. Please send OTP first."

        # Sign in with phone, code, and phone_code_hash
        await client.sign_in(phone=phone, code=code)

        # Save the session after successful login


        async for dialog in client.iter_dialogs():
            chat_name = dialog.name or "unknown chat"
            chat_id = dialog.id
            chat.append(f"chatname: {chat_name}, chat id: {chat_id}")

        return "Logged in successfully."

    except Exception as e:
        return f"Error: {str(e)}"

async def restore_session():
    global client
    try:
        # Initialize the client with the session file
        client = TelegramClient(SESSION_FILE, API_ID, API_HASH, loop=loop)
        await client.connect()
        async for dialog in client.iter_dialogs():
                    chat_name = dialog.name or "unknown chat"
                    chat_id = dialog.id
                    chat.append(f"chatname: {chat_name}, chat id: {chat_id}")

        # Check if the session is valid
        if await client.is_user_authorized():
            return "Session restored. Already authorized."
        else:
            return "No valid session found. Please log in again."

    except Exception as e:
        return f"Error: {str(e)}"


async def get_convo(selectedContactId):
    global client, messagess
    try:
        # Initialize the client if not already initialized
        if client is None:
            client = TelegramClient(SESSION_FILE, API_ID, API_HASH, loop=loop)

        # Connect to the client
        await client.connect()

        # Ensure the client is authorized
        if not await client.is_user_authorized():
            return {"error": "Client not authorized. Please log in first."}

        # Fetch the entity (chat) using the selectedContactId
        target = await client.get_entity(int(selectedContactId))

        # Fetch the last 100 messages from the chat
        messages = await client.get_messages(target, limit=100)

        # Clear the previous messages
        messagess.clear()

        # Process and store the messages
        for message in messages:
            message_date = message.date  # Message date
            message_sender_id = message.sender_id  # Sender ID
            chatcontent = message.text or '<Media/Non-text message>'  # Message content
            messagess.append(f"chatdate: {message_date}, chat id: {message_sender_id}, content: {chatcontent} end")

        # Debug: Print the messages
        print("DEBUG: Messages fetched =", messagess)

        # Return the messages
        return {"messages": messagess}

    except Exception as e:
        return {"error": f"Error: {str(e)}"}

# Helper functions to run coroutines in the existing event loop
def phoneNumber(phone):
    return loop.run_until_complete(send_otp_async(phone))

def otpCode(code, phone):
    return loop.run_until_complete(send_code(code, phone))

def restoreSession():
    return loop.run_until_complete(restore_session())

def get_chats():
    global chat

    return chat


def getconvo(selectedContactId):
    global messagess
    print("DEBUG: Before calling get_convo, messages =", messagess)
    result = loop.run_until_complete(get_convo(selectedContactId))
    print("DEBUG: After calling get_convo, messages =", messagess)
    if "error" in result:
        return result["error"]  # Return the error message
    else:
        return result["messages"]  # Return the list of messagest of messages