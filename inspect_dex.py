import zipfile
import re
import json
import os

apk_path = "/tmp/diktatv.apk"
with zipfile.ZipFile(apk_path, "r") as z:
    for name in z.namelist():
        if name.endswith(".dex"):
            print("Processing DEX:", name)
            data = z.read(name)
            # Find all string-like things
            # In DEX format, strings table is present. Let's extract ASCII / UTF-8 strings >= 4 chars
            raw_strings = re.findall(b"[\x20-\x7e]{4,}", data)
            print(f"Found {len(raw_strings)} strings in {name}")
            
            # Let's search for specific indicators
            for s_bytes in raw_strings:
                s = s_bytes.decode("latin1")
                s_low = s.lower()
                if any(x in s_low for x in ["http", "m3u", "channel", "playlist", "dikta", "stream", "live", "rtmp", "hls", "ts", "mp4", "token", "auth", "base64", "github", "pastebin", "raw", "gist", "blogspot", "firebase", "sfile", "drive"]):
                    if not any(skip in s_low for skip in ["schemas.android.com", "android.support", "androidx", "kotlin", "google.com", "jetbrains"]):
                        print("  [STR]", s)
