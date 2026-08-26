import zipfile
import re
import struct

apk_path = "/tmp/diktatv.apk"

with zipfile.ZipFile(apk_path, "r") as z:
    dex_data = z.read("classes.dex")
    
# Let's search for "DiktaTV_Secure_Key_2026_x89" offset in DEX
key = b"DiktaTV_Secure_Key_2026_x89"
pos = dex_data.find(key)
print(f"Key pos in DEX: {pos}")

# Let's look around this string and other strings around it
start = max(0, pos - 1000)
end = min(len(dex_data), pos + 2000)
snippet = dex_data[start:end]
print("Strings near key:")
for s in re.findall(b"[\x20-\x7e]{3,}", snippet):
    print("  ", s.decode("latin1"))

# Also search for base64-like or hex-like or encrypted strings in all dex files
print("\n--- Searching for all strings in DEX containing playlist/dikta/http ---")
for match in re.finditer(b'([A-Za-z0-9+/=]{16,})', dex_data):
    s = match.group(1).decode('latin1')
    # try decrypting with AES / XOR / Base64
    import base64
    try:
        raw = base64.b64decode(s)
        if len(raw) > 16:
            # Check if plaintext or cipher
            # Let's try AES ECB/CBC with key 'DiktaTV_Secure_Key_2026_x89' (padded/trimmed to 16 or 32 bytes)
            pass
    except:
        pass
