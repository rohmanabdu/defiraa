import zipfile
import struct
import json

apk_path = "/tmp/diktatv.apk"

with zipfile.ZipFile(apk_path, "r") as z:
    for name in z.namelist():
        if name.endswith(".dex"):
            data = z.read(name)
            # Find all strings
            string_ids_size, string_ids_off = struct.unpack_from("<2I", data, 0x38)
            strings = []
            for i in range(string_ids_size):
                off = struct.unpack_from("<I", data, string_ids_off + i * 4)[0]
                p = off
                while (data[p] & 0x80) != 0:
                    p += 1
                p += 1
                end = data.find(b"\x00", p)
                try:
                    s = data[p:end].decode("utf-8")
                except:
                    s = data[p:end].decode("latin1", errors="ignore")
                strings.append(s)

            print(f"Total strings in {name}: {len(strings)}")
            # Filter all strings that could be channel names, categories, URLs, IDs, endpoints
            interesting = []
            for s in strings:
                s_l = s.lower()
                if any(k in s_l for k in [
                    ".m3u", "http", "ftp", "rtsp", "rtmp", "playlist", "channel", 
                    "trans7", "trans tv", "rcti", "sctv", "indosiar", "mnc", "antv", "tvri",
                    "kompas", "metro", "net tv", "gtv", "inews", "tvone", "ch_",
                    "diktatv", "dikta", "iptv", "sports", "beinsports", "spotv", "kodi",
                    "t.me", "pastebin", "github", "raw.githubusercontent"
                ]):
                    interesting.append(s)
            
            print(f"Interesting strings ({len(interesting)}):")
            for item in interesting[:200]:
                print("  *", item)
