#pragma once

#include <string>

#include "types.hpp"


inline void appendByte(ByteBuffer& buf, uint8_t value) {
    buf.push_back(value);
}

inline void appendUint16(ByteBuffer& buf, uint16_t value) {
    buf.push_back(static_cast<uint8_t>((value >> 8) & 0xFF));
    buf.push_back(static_cast<uint8_t>(value & 0xFF));
}

inline void appendUint32(ByteBuffer& buf, uint32_t value) {
    for (int i = 3; i >= 0; --i) {
        buf.push_back(static_cast<uint8_t>((value >> (i * 8)) & 0xFF));
    }
}

inline void appendUint64(ByteBuffer& buf, uint64_t value) {
    for (int i = 7; i >= 0; --i) {
        buf.push_back(static_cast<uint8_t>((value >> (i * 8)) & 0xFF));
    }
}

inline void appendBytes(ByteBuffer& buf, const ByteBuffer& data) {
    buf.insert(buf.end(), data.begin(), data.end());
}

inline void appendString(ByteBuffer& buf, const std::string& s) {
    appendUint16(buf, s.size()); // add the string size
    buf.insert(buf.end(), s.begin(), s.end());
}

inline void appendByteBuffer(ByteBuffer& buf, const ByteBuffer& s){
    appendUint16(buf, s.size()); // add the ByteBuffer size
    buf.insert( buf.end(), s.begin(), s.end() );
}

ByteBuffer buildPingMessage();
ByteBuffer buildAuthenticateMessage(const std::string& username, const std::string& password);
ByteBuffer buildStoreKeyMessage(const std::string& username, const ByteBuffer& key_material);
ByteBuffer buildStoreKeyMessage(const std::string& username, const std::string& key_material);
ByteBuffer buildGetKeyMessage(const std::string& username);
ByteBuffer buildUpdatePosMessage(const std::string& username, const uint64_t offset);
ByteBuffer buildGetPosMessage(const std::string& username);