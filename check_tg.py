import urllib.request
import re

urls = [
    "https://t.me/s/diktatv",
    "https://diktatv.randhyrandhy421.workers.dev",
    "https://tv2.randhyrandhy421.workers.dev",
    "https://tv3.randhyrandhy421.workers.dev",
]

for u in urls:
    try:
        req = urllib.request.Request(u, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
        with urllib.request.urlopen(req, timeout=10) as r:
            data = r.read().decode("utf-8", errors="ignore")
            print(f"[OK] {u} -> length {len(data)}")
            if "t.me" in u:
                links = re.findall(r"https?://[^\s\"\'<>]+", data)
                print("Telegram links found:")
                for l in sorted(set(links)):
                    print("  *", l)
            else:
                print("Data sample:", data[:300])
    except Exception as e:
        print(f"[ERR] {u} -> {e}")
