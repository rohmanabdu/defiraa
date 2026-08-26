import urllib.request
import re

req = urllib.request.Request("https://t.me/s/diktatv", headers={"User-Agent": "Mozilla/5.0"})
with urllib.request.urlopen(req, timeout=10) as r:
    html = r.read().decode("utf-8", errors="ignore")

# Find all text messages in telegram channel
msgs = re.findall(r'<div class="tgme_widget_message_text[^>]*>(.*?)</div>', html, re.DOTALL)
for i, m in enumerate(msgs):
    # clean HTML tags
    clean = re.sub(r'<[^>]+>', ' ', m)
    print(f"--- MSG {i+1} ---")
    print(clean.strip())
