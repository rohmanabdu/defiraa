import zipfile
import struct
import base64

apk_path = "/tmp/diktatv.apk"

with zipfile.ZipFile(apk_path, "r") as z:
    dex_data = z.read("classes.dex")

# Let's inspect class Le71 and its methods
string_ids_size, string_ids_off, type_ids_size, type_ids_off, proto_ids_size, proto_ids_off, field_ids_size, field_ids_off, method_ids_size, method_ids_off, class_defs_size, class_defs_off = struct.unpack_from("<12I", dex_data, 0x38)

def get_string(idx):
    off = struct.unpack_from("<I", dex_data, string_ids_off + idx * 4)[0]
    p = off
    while (dex_data[p] & 0x80) != 0:
        p += 1
    p += 1
    end = dex_data.find(b"\x00", p)
    try:
        return dex_data[p:end].decode("utf-8")
    except:
        return dex_data[p:end].decode("latin1", errors="ignore")

def get_type(idx):
    str_idx = struct.unpack_from("<I", dex_data, type_ids_off + idx * 4)[0]
    return get_string(str_idx)

def parse_uleb128(data, p):
    res = 0
    shift = 0
    while True:
        b = data[p]
        p += 1
        res |= (b & 0x7f) << shift
        shift += 7
        if (b & 0x80) == 0:
            break
    return res, p

# Find class Le71;
for c_i in range(class_defs_size):
    c_off = class_defs_off + c_i * 32
    class_idx, access_flags, super_idx, interfaces_off, source_idx, annotations_off, class_data_off, static_values_off = struct.unpack_from("<8I", dex_data, c_off)
    cname = get_type(class_idx)
    if cname in ["Le71;", "Laq1;"]:
        print(f"--- Disassembling {cname} ---")
        if class_data_off == 0:
            continue
        p = class_data_off
        static_fields_size, p = parse_uleb128(dex_data, p)
        instance_fields_size, p = parse_uleb128(dex_data, p)
        direct_methods_size, p = parse_uleb128(dex_data, p)
        virtual_methods_size, p = parse_uleb128(dex_data, p)
        
        for _ in range(static_fields_size + instance_fields_size):
            _, p = parse_uleb128(dex_data, p)
            _, p = parse_uleb128(dex_data, p)
            
        method_idx = 0
        for m_i in range(direct_methods_size + virtual_methods_size):
            m_diff, p = parse_uleb128(dex_data, p)
            m_flags, p = parse_uleb128(dex_data, p)
            code_off, p = parse_uleb128(dex_data, p)
            method_idx += m_diff
            
            # get method name
            # method_id = method_idx
            # method_ids format: class_idx (ushort), proto_idx (ushort), name_idx (uint)
            m_c_idx, m_p_idx, m_n_idx = struct.unpack_from("<HHI", dex_data, method_ids_off + method_idx * 8)
            m_name = get_string(m_n_idx)
            
            print(f"Method {m_name} (flags={m_flags:#x}, code_off={code_off})")
            if code_off > 0:
                registers_size, ins_size, outs_size, tries_size, debug_info_off, insns_size = struct.unpack_from("<6H", dex_data, code_off)
                insns_bytes = dex_data[code_off + 16 : code_off + 16 + insns_size * 2]
                print(f"  Registers: {registers_size}, Insns length: {len(insns_bytes)} bytes")
                # print instructions hex & strings
                for ip in range(0, len(insns_bytes) - 1, 2):
                    op = insns_bytes[ip]
                    if op == 0x1a and ip + 4 <= len(insns_bytes):
                        s_idx = struct.unpack_from("<H", insns_bytes, ip + 2)[0]
                        print(f"    [{ip:04x}] const-string v{insns_bytes[ip+1]}, \"{get_string(s_idx)}\"")
                    elif op in [0x90, 0xd8, 0xd0, 0x7b, 0x8b]: # arithmetic / xor / etc
                        print(f"    [{ip:04x}] op {op:#x}")

# Let's also test XOR decryption on the base64 strings with "DiktaTV_Secure_Key_2026_x89"
encrypted_strings = [
    "LB0fBBJueXA3DAgBExEpZRcYMVZYS0Q+FlxRPV1ZRU8jOS04ABEGXAE6PQ==",
    "LB0fBBJueXAnE1BbAAQxLw0ALVNeVl4mTAoIah4EBgoxJCx9AQYD",
    "LB0fBBJueXAnE1FbAAQxLw0ALVNeVl4mTAoIah4EBgoxJCx9AQYDXQ=="
]

key = "DiktaTV_Secure_Key_2026_x89"

print("\n--- Trying XOR Decryption ---")
for s in encrypted_strings:
    raw = base64.b64decode(s)
    dec = bytes([b ^ ord(key[i % len(key)]) for i, b in enumerate(raw)])
    print("XOR:", dec.decode('latin1', errors='ignore'))
