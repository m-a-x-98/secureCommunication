#include "byte_reader.hpp"

#include "types.hpp"
#include <cstdint>
#include <stdexcept>


uint32_t readUint32BE(const ByteBuffer& buf, size_t offset) {
    if (offset + 4 > buf.size()) {
        throw std::runtime_error("readUint32BE: buffer too short for offset");
    }
    return (static_cast<uint32_t>(buf[offset])     << 24) |
           (static_cast<uint32_t>(buf[offset + 1]) << 16) |
           (static_cast<uint32_t>(buf[offset + 2]) << 8)  |
            static_cast<uint32_t>(buf[offset + 3]);
}

uint64_t readUint64BE(const ByteBuffer& buf, size_t offset) {
    if (offset + 8 > buf.size()) {
        throw std::runtime_error("readUint64BE: buffer too short for offset");
    }
    uint64_t value = 0;
    for (int i = 0; i < 8; i++) {
        value = (value << 8) | buf[offset + i];
    }
    return value;
}