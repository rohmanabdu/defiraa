import urllib.request
import urllib.error
import ssl
import json
import base64

urls = [
    ("DiktaTV", "https://diktatv.randhyrandhy421.workers.dev"),
    ("Dikta Xtream", "https://tv3.randhyrandhy421.workers.dev"),
    ("DIKTA PL LAIN", "https://tv2.randhyrandhy421.workers.dev/"),
]

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

user_agents = [
    "okhttp/4.12.0",
    "DiktaTV/1.0 (Android)",
    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36",
    ""
]

for name, url in urls:
    print(f"\n================ Fetching {name}: {url} ================")
    success = False
    for ua in user_agents:
        try:
            req = urllib.request.Request(
                url,
                headers={"User-Agent": ua} if ua else {}
            )
            with urllib.request.urlopen(req, context=ctx, timeout=8) as resp:
                data = resp.read()
                print(f"Success with UA '{ua}'! Status={resp.status}, size={len(data)}")
                text = data.decode("utf-8", errors="ignore")
                print("First 300 chars:\n", text[:300])
                safe_name = name.lower().replace(" ", "_")
                with open(f"{safe_name}.m3u", "w", encoding="utf-8") as f:
                    f.write(text)
                success = True
                break
        except Exception as e:
            print(f"Failed with UA '{ua}': {e}")
