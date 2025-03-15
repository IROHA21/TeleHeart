from telethon import TelegramClient
import asyncio
import os
from telethon.tl.functions.account import GetAuthorizationsRequest, ResetAuthorizationRequest
# Replace with your own API credentials
API_ID = 25016078
API_HASH = "6e818b2e5b0e074c403e3bb120f64736"
SESSION_FILE = None  # Will be set dynamically
user_id = None
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


        # Send OTP without any checks
        await client.send_code_request(phone)
        return "Code sent check your telegram"

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

async def get_convo(selectedContactId, quantity):
    global client, messagess, user_id

    try:
        if client is None:
            client = TelegramClient(SESSION_FILE, API_ID, API_HASH, loop=loop)

        # Ensure the client is connected
        if not client.is_connected():
            await client.connect()

        if not await client.is_user_authorized():
            return {"error": "Client not authorized. Please log in first."}

        target = await client.get_entity(int(selectedContactId))
        intquna = int(quantity)
        messages = await client.get_messages(target, limit=intquna)

        me = await client.get_me()
        user_id = me.id

        messagess.clear()

        for message in messages:
            message_date = message.date
            message_sender_id = message.sender_id
            chatcontent = message.text or '<Media/Non-text message>'
            messagess.append(f"chatdate: {message_date}, chat id: {message_sender_id}, content: {chatcontent} end")

        return {"messages": messagess}

    except Exception as e:
        return {"error": f"Error: {str(e)}"}

# Function to disconnect all sessions



async def terminate_and_disconnect_async():
    global client
    try:
        if client is None:
            client = TelegramClient(SESSION_FILE, API_ID, API_HASH, loop=loop)

        await client.connect()

        # Log out of the current session
        await client.log_out()

        # Disconnect the client
        await client.disconnect()

        print("Disconnected from Telegram.")

    except Exception as e:
        print(f"Error: {str(e)}")



def get_user_id_sync():
    global user_id, client

    try:
        if client is None:
            client = TelegramClient(SESSION_FILE, API_ID, API_HASH, loop=loop)

        # Ensure the client is connected
        if not client.is_connected():
            loop.run_until_complete(client.connect())

        if not loop.run_until_complete(client.is_user_authorized()):
            return None

        me = loop.run_until_complete(client.get_me())
        user_id = me.id
        print(f"get_user_id_sync: user_id = {user_id}")
        return user_id

    except Exception as e:
        print(f"Error in get_user_id_sync: {str(e)}")
        return None

def phoneNumber(phone):
    return loop.run_until_complete(send_otp_async(phone))

def otpCode(code, phone):
    return loop.run_until_complete(send_code(code, phone))

def restoreSession():
    return loop.run_until_complete(restore_session())

def get_chats():
    global chat
    return chat

def getconvo(selectedContactId, quantity):
    result = loop.run_until_complete(get_convo(selectedContactId, quantity))
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



def terminate_and_disconnect():
    return loop.run_until_complete(terminate_and_disconnect_async())