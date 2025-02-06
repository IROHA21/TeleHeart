from telethon import TelegramClient
from telethon.errors import PhoneCodeInvalidError, PhoneCodeExpiredError, SessionPasswordNeededError
import asyncio
import os


API_ID = 25016078
API_HASH = "6e818b2e5b0e074c403e3bb120f64736"



async def main_async(phone):
    await asyncio.sleep(1)
    session_file = f"session_{phone}.session"
    if os.path.exists(session_file):
        os.remove(session_file)





     # Simulate async delay
    return f"Received phone: {phone}"

def phoneNumber(phone):
    return asyncio.run(main_async(phone))
