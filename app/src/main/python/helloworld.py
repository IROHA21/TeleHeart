from telethon import TelegramClient
import asyncio

# Replace with your own API credentials
API_ID = 25016078
API_HASH = "6e818b2e5b0e074c403e3bb120f64736"

async def send_otp_async(phone):
    try:
        # Use RAM-based session (No file)
        client = TelegramClient(None, API_ID, API_HASH)

        await client.connect()

        # Check if user is already authorized (logged in)
        if not await client.is_user_authorized():
            await client.send_code_request(phone)  # Send OTP to the phone number
            return "OTP has been sent to your Telegram app. Please check."

        return "Already authorized. No need for OTP."

    except Exception as e:
        return f"Error: {str(e)}"




async def send_code(code, phone):
    try:



        # Retrieve phone_code_hash from session_data


        # Sign in with phone, code, and phone_code_hash
        await client.sign_in(phone, code)

        return "Logged in successfully."
    except Exception as e:
        return f"Error: {str(e)}"


def otpCode(code, phone):
    return asyncio.run(send_code(code, phone))
def phoneNumber(phone):
    return asyncio.run(send_otp_async(phone))


