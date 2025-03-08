from telethon import TelegramClient
import asyncio
import os

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
        # Disconnect and reset the client if it exists
        if client is not None:
            await client.disconnect()
            client = None

        # Create a new client and session file
        client = TelegramClient(SESSION_FILE, API_ID, API_HASH, loop=loop)
        await client.connect()

        # Add a small delay to ensure the connection is fully established
        await asyncio.sleep(1)  # 1-second delay

        # Send OTP without any checks
        await client.send_code_request(phone)
        return "OTP has been sent to your Telegram app. Please check."

    except Exception as e:
        return f"Error: {str(e)}"

async def send_code(code, phone):
    global client
    try:
        if client is None:
            return "Client not initialized. Please send OTP first."

        await client.sign_in(phone=phone, code=code)

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
        client = TelegramClient(SESSION_FILE, API_ID, API_HASH, loop=loop)
        await client.connect()
        async for dialog in client.iter_dialogs():
            chat_name = dialog.name or "unknown chat"
            chat_id = dialog.id
            chat.append(f"chatname: {chat_name}, chat id: {chat_id}")

        if await client.is_user_authorized():
            return "Session restored. Already authorized."
        else:
            return "No valid session found. Please log in again."

    except Exception as e:
        return f"Error: {str(e)}"

async def get_convo(selectedContactId):
    global client, messagess

    try:
        if client is None:
            client = TelegramClient(SESSION_FILE, API_ID, API_HASH, loop=loop)

        await client.connect()

        if not await client.is_user_authorized():
            return {"error": "Client not authorized. Please log in first."}

        target = await client.get_entity(int(selectedContactId))
        messages = await client.get_messages(target, limit=1000)

        messagess.clear()

        for message in messages:
            message_date = message.date
            message_sender_id = message.sender_id
            chatcontent = message.text or '<Media/Non-text message>'
            messagess.append(f"chatdate: {message_date}, chat id: {message_sender_id}, content: {chatcontent} end")

        return {"messages": messagess}

    except Exception as e:
        return {"error": f"Error: {str(e)}"}

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
    result = loop.run_until_complete(get_convo(selectedContactId))
    if "error" in result:
        return result["error"]
    else:
        return result["messages"]

async def disconnect_client_async():
    global client
    if client is not None:
        await client.disconnect()
        client = None

def disconnect_client():
    loop.run_until_complete(disconnect_client_async())