import zipfile
import re

apk_path = "/tmp/diktatv.apk"

with zipfile.ZipFile(apk_path, "r") as z:
    for name in z.namelist():
        data = z.read(name)
        # Find anything with workers.dev, github, pastebin, gitlab, bitbucket, sfile, t.me, etc.
        patterns = [
            rb"https?://[a-zA-Z0-9\.\-_/:\?\#\=\&\%\+\@]+",
            rb"[a-zA-Z0-9_\-\.]{4,}\.(?:workers\.dev|github\.io|pastebin\.com|blogspot\.com|pages\.dev|vercel\.app|netlify\.app|herokuapp\.com|sfile\.mobi|t\.me)"
        ]
        for p in patterns:
            for m in re.finditer(p, data):
                print(f"[{name}] {m.group(0).decode('latin1')}")
