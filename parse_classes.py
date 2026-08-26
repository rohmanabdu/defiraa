import zipfile
import struct

apk_path = "/tmp/diktatv.apk"

with zipfile.ZipFile(apk_path, "r") as z:
    for name in z.namelist():
        if name.endswith(".dex"):
            data = z.read(name)
            # Find class names
            # Parse DEX header
            string_ids_size, string_ids_off, type_ids_size, type_ids_off, proto_ids_size, proto_ids_off, field_ids_size, field_ids_off, method_ids_size, method_ids_off, class_defs_size, class_defs_off = struct.unpack_from("<12I", data, 0x38)
            
            def get_string(idx):
                off = struct.unpack_from("<I", data, string_ids_off + idx * 4)[0]
                p = off
                while (data[p] & 0x80) != 0:
                    p += 1
                p += 1
                end = data.find(b"\x00", p)
                return data[p:end].decode("utf-8", errors="ignore")
            
            def get_type(idx):
                str_idx = struct.unpack_from("<I", data, type_ids_off + idx * 4)[0]
                return get_string(str_idx)
            
            print(f"--- {name} Class Defs ({class_defs_size}) ---")
            for i in range(class_defs_size):
                class_idx = struct.unpack_from("<I", data, class_defs_off + i * 32)[0]
                cname = get_type(class_idx)
                if not any(cname.startswith(p) for p in ["Ljava/", "Ljavax/", "Landroid/", "Landroidx/", "Lkotlin/", "Lkotlinx/", "Lokhttp3/", "Lokio/", "Lcom/google/"]):
                    print("Class:", cname)
