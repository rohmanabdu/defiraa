import zipfile
import struct

apk_path = "/tmp/diktatv.apk"

with zipfile.ZipFile(apk_path, "r") as z:
    dex_data = z.read("classes.dex")

# Parse DEX
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

# Find string index for DiktaTV_Secure_Key_2026_x89
target_idx = None
target_indices = {}
for i in range(string_ids_size):
    s = get_string(i)
    if "DiktaTV_Secure_Key" in s or "Dikta TV" in s or "Dikta Xtream" in s:
        target_indices[i] = s
        print(f"String idx {i}: {s}")

# Let's parse all code items to find const-string referencing target_idx
# const-string opcode: 0x1a (reg, string_idx_16)
# const-string/jumbo opcode: 0x1b (reg, string_idx_32)

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

for c_i in range(class_defs_size):
    c_off = class_defs_off + c_i * 32
    class_idx, access_flags, super_idx, interfaces_off, source_idx, annotations_off, class_data_off, static_values_off = struct.unpack_from("<8I", dex_data, c_off)
    cname = get_type(class_idx)
    
    if class_data_off == 0:
        continue
        
    p = class_data_off
    static_fields_size, p = parse_uleb128(dex_data, p)
    instance_fields_size, p = parse_uleb128(dex_data, p)
    direct_methods_size, p = parse_uleb128(dex_data, p)
    virtual_methods_size, p = parse_uleb128(dex_data, p)
    
    # skip static & instance fields
    for _ in range(static_fields_size):
        _, p = parse_uleb128(dex_data, p)
        _, p = parse_uleb128(dex_data, p)
    for _ in range(instance_fields_size):
        _, p = parse_uleb128(dex_data, p)
        _, p = parse_uleb128(dex_data, p)
        
    method_idx = 0
    for m_i in range(direct_methods_size + virtual_methods_size):
        m_diff, p = parse_uleb128(dex_data, p)
        m_flags, p = parse_uleb128(dex_data, p)
        code_off, p = parse_uleb128(dex_data, p)
        method_idx += m_diff
        
        if code_off > 0:
            registers_size, ins_size, outs_size, tries_size, debug_info_off, insns_size = struct.unpack_from("<6H", dex_data, code_off)
            insns_bytes = dex_data[code_off + 16 : code_off + 16 + insns_size * 2]
            
            # Check for const-string instructions
            for ip in range(0, len(insns_bytes) - 2, 2):
                op = insns_bytes[ip]
                if op == 0x1a and ip + 4 <= len(insns_bytes): # const-string
                    s_idx = struct.unpack_from("<H", insns_bytes, ip + 2)[0]
                    if s_idx in target_indices:
                        print(f"Found match in {cname} (method_idx {method_idx}): string {target_indices[s_idx]}")
                        # print all strings used in this method!
                        for ip2 in range(0, len(insns_bytes) - 2, 2):
                            op2 = insns_bytes[ip2]
                            if op2 == 0x1a and ip2 + 4 <= len(insns_bytes):
                                s2 = struct.unpack_from("<H", insns_bytes, ip2 + 2)[0]
                                print("   uses string:", get_string(s2))
                            elif op2 == 0x1b and ip2 + 6 <= len(insns_bytes):
                                s2 = struct.unpack_from("<I", insns_bytes, ip2 + 2)[0]
                                print("   uses string/jumbo:", get_string(s2))
                elif op == 0x1b and ip + 6 <= len(insns_bytes):
                    s_idx = struct.unpack_from("<I", insns_bytes, ip + 2)[0]
                    if s_idx in target_indices:
                        print(f"Found match in {cname} (method_idx {method_idx}): string {target_indices[s_idx]}")
