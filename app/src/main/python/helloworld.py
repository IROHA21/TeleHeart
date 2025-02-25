from telethon import TelegramClient
import asyncio

# Replace with your own API credentials
API_ID = 25016078
API_HASH = "6e818b2e5b0e074c403e3bb120f64736"

# Global event loop and client
loop = asyncio.new_event_loop()
client = None

async def send_otp_async(phone):
    global client
    try:
        # Initialize the client if it doesn't exist
        if client is None:
            client = TelegramClient(None, API_ID, API_HASH, loop=loop)

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
        await client.sign_in(phone, code)

        chats = {}
        chat =[]
        async for dialog in client.iter_dialogs():
            chat_name = dialog.name or "unknown chat"
            chat_id = dialog.idq
            chats[chats_id]= chat_name
            chat.append (f"chatname: {chat_name}, chat id: {chat_id}")



        return "Logged in successfully."

    except Exception as e:
        return f"Error: {str(e)}"

# Helper functions to run coroutines in the existing event loop
def phoneNumber(phone):
    return loop.run_until_complete(send_otp_async(phone))

def otpCode(code, phone):
    return loop.run_until_complete(send_code(code, phone))