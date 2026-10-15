#pragma once

#include <string>
#include <ctime>

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

inline void appendString16(ByteBuffer& buf, const std::string& s) {
    appendUint16(buf, s.size()); // add the string size
    buf.insert(buf.end(), s.begin(), s.end());
}

inline void appendString32(ByteBuffer& buf, const std::string& s) {
    appendUint32(buf, s.size()); // add the string size
    buf.insert(buf.end(), s.begin(), s.end());
}

inline void appendString64(ByteBuffer& buf, const std::string& s) {
    appendUint64(buf, s.size()); // add the string size
    buf.insert(buf.end(), s.begin(), s.end());
}

inline void appendByteBuffer16(ByteBuffer& buf, const ByteBuffer& s){
    appendUint16(buf, s.size()); // add the ByteBuffer size
    buf.insert( buf.end(), s.begin(), s.end() );
}

inline void appendByteBuffer32(ByteBuffer& buf, const ByteBuffer& s){
    appendUint32(buf, s.size()); // add the ByteBuffer size
    buf.insert( buf.end(), s.begin(), s.end() );
}

inline void appendByteBuffer64(ByteBuffer& buf, const ByteBuffer& s){
    appendUint64(buf, s.size()); // add the ByteBuffer size
    buf.insert( buf.end(), s.begin(), s.end() );
}

ByteBuffer buildPingMessage();
ByteBuffer buildAuthenticateMessage(const std::string& username, const std::string& password);
ByteBuffer buildStoreKeyMessage(const std::string& username, const ByteBuffer& key_material);
ByteBuffer buildGetKeyMessage(const std::string& username);
ByteBuffer buildUpdatePosMessage(const std::string& username, const uint64_t offset);
ByteBuffer buildGetPosMessage(const std::string& username);
ByteBuffer buildGetPadSizeMessage(const std::string& username);

ByteBuffer buildAddUsrMessage(const UserMessage message);
ByteBuffer buildGetNUsrMessage(const std::string& username, const int numb);
ByteBuffer buildGetUserMessageTime(const std::string& username, std::time_t start_time, std::time_t end_time);