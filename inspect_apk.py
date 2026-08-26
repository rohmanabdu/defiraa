import zipfile
import re
import json
import os

apk_path = "/tmp/diktatv.apk"
if not os.path.exists(apk_path):
    apk_path = "diktatv.apk"

with zipfile.ZipFile(apk_path, "r") as z:
    names = z.namelist()
    print("Files in APK:", len(names))
    assets = [n for n in names if n.startswith("assets/")]
    print("Assets:", assets)

    all_urls = set()
    
    for name in names:
        try:
            content = z.read(name)
        except Exception as e:
            continue
            
        if any(k in name.lower() for k in ["m3u", "channel", "playlist", "tv", "stream", "data", "list", "config"]):
            print("Potentially interesting file:", name, len(content))
            try:
                print("Preview:", content.decode("utf-8", errors="ignore")[:300])
            except:
                pass
                
        # Regex search for URLs
        matches = re.findall(b"https?://[a-zA-Z0-9\\.\\-_\\~:\\?#\\[\\]@!$&\'()*+,;=%/]+", content)
        for m in matches:
            try:
                url = m.decode("utf-8", errors="ignore")
                all_urls.add((url, name))
            except:
                pass

print(f"\nFound {len(all_urls)} URLs total.")

# Let us filter interesting ones
print("\n--- Interesting Streams & Endpoints ---")
for url, source in sorted(all_urls):
    u_lower = url.lower()
    if any(k in u_lower for k in [".m3u", ".m3u8", "stream", "live", "tv", "channel", "playlist", "pastebin", "github", "gitlab", "firebase", "blogspot", "google", "sfile", "drive", "api", "json", "raw", "hls", "mpd"]):
        if not any(skip in u_lower for skip in ["schemas.android.com", "android.googlesource", "w3.org", "apache.org", "kotlin", "google.com/policies", "maven.google.com"]):
            print(f"[{source}] {url}")
