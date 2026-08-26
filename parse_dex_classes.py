import zipfile
import re
import struct

apk_path = "/tmp/diktatv.apk"

def parse_dex(dex_bytes):
    # DEX header
    magic = dex_bytes[:8]
    string_ids_size = struct.unpack_from("<I", dex_bytes, 0x38)[0]
    string_ids_off = struct.unpack_from("<I", dex_bytes, 0x3c)[0]
    
    strings = []
    for i in range(string_ids_size):
        str_data_off = struct.unpack_from("<I", dex_bytes, string_ids_off + i * 4)[0]
        # parse uleb128 length then string
        p = str_data_off
        # skip uleb128
        while (dex_bytes[p] & 0x80) != 0:
            p += 1
        p += 1
        # find null terminator
        end = dex_bytes.find(b"\x00", p)
        if end != -1:
            try:
                s = dex_bytes[p:end].decode("utf-8")
                strings.append(s)
            except:
                strings.append(dex_bytes[p:end].decode("latin1", errors="ignore"))
    return strings

with zipfile.ZipFile(apk_path, "r") as z:
    for name in z.namelist():
        if name.endswith(".dex"):
            data = z.read(name)
            strings = parse_dex(data)
            print(f"Parsed {len(strings)} strings from {name}")
            
            # Find class definitions or package names
            classes = [s for s in strings if s.startswith("L") and s.endswith(";")]
            user_classes = [c for c in classes if not any(c.startswith(p) for p in ["Ljava/", "Ljavax/", "Landroid/", "Landroidx/", "Lkotlin/", "Lkotlinx/", "Lokhttp3/", "Lokio/", "Lcom/google/", "Lorg/"])]
            print("User classes:", user_classes[:50])
            
            # Print non-framework strings
            print("\nNon-framework strings matching keywords:")
            for s in strings:
                if any(x in s.lower() for x in ["http", "m3u", "pastebin", "dikta", "raw.github", "api", "json", "token", "playlist", "firebase", "tv", "channel", "stream", "sfile", "drive"]):
                    if not any(skip in s for skip in ["android", "androidx", "kotlin", "google", "okhttp", "schema"]):
                        print("  ->", s)
