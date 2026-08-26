import urllib.request
import re

req = urllib.request.Request("https://t.me/s/diktatv", headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
with urllib.request.urlopen(req, timeout=10) as r:
    html = r.read().decode("utf-8", errors="ignore")

print("Length of HTML:", len(html))
print(html[:2000])
