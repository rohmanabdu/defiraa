import zipfile

apk_path = "/tmp/diktatv.apk"

with zipfile.ZipFile(apk_path, "r") as z:
    for fname in z.namelist():
        if fname.endswith(".dex"):
            data = z.read(fname)
            # Find all strings in this dex
            # Let's search for "http", "m3u", "pastebin", "github", "bit.ly", "sfile", "diktatv", "dikta", "channel"
            import re
            for match in re.finditer(b"https?://[^\x00\r\n\"'\<\>\s]+", data):
                print(match.group(0).decode("latin1", errors="ignore"))

            # Also check base64 strings or encrypted strings or arrays
            for b64match in re.finditer(b"[A-Za-z0-9+/=]{20,}", data):
                s = b64match.group(0).decode("latin1", errors="ignore")
                import base64
                try:
                    dec = base64.b64decode(s)
                    if b"http" in dec or b"m3u" in dec or b"channel" in dec or b"EXTINF" in dec:
                        print("FOUND B64:", dec.decode('latin1', errors='ignore'))
                except:
                    pass
