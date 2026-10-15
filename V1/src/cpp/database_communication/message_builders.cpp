#include "message_builders.hpp"

ByteBuffer buildPingMessage(){
    ByteBuffer msg;
    appendByte(msg, 0x01); // message type
    appendUint32(msg, 0);
    return msg;
}

ByteBuffer buildAuthenticateMessage(const std::string& username, const std::string& password) {
    ByteBuffer payload;
    appendString16(payload, username);
    appendString16(payload, password);

    ByteBuffer msg;
    appendByte(msg, 0x02);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}

ByteBuffer buildStoreKeyMessage(const std::string& username, const ByteBuffer& key_material){
    ByteBuffer payload;
    appendString16(payload, username);
    appendByteBuffer64(payload, key_material);

    ByteBuffer msg;
    appendByte(msg, 0x03);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}


ByteBuffer buildGetKeyMessage(const std::string& username){
    ByteBuffer payload;
    appendString16(payload, username);

    ByteBuffer msg;
    appendByte(msg, 0x04);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}

ByteBuffer buildUpdatePosMessage(const std::string& username, const uint64_t offset){
    ByteBuffer payload;
    appendString16(payload, username);
    appendUint64(payload, offset);

    ByteBuffer msg;
    appendByte(msg, 0x05);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}

ByteBuffer buildGetPosMessage(const std::string& username){
    ByteBuffer payload;
    appendString16(payload, username);

    ByteBuffer msg;
    appendByte(msg, 0x06);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}

ByteBuffer buildGetPadSizeMessage(const std::string& username){
    ByteBuffer payload;
    appendString16(payload, username);

    ByteBuffer msg;
    appendByte(msg, 0x07);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}


ByteBuffer buildAddUsrMessage(const UserMessage message){
    return ByteBuffer();
}

ByteBuffer buildGetNUsrMessage(const std::string& username, const int numb){
    return ByteBuffer();
}

ByteBuffer buildGetUserMessageTime(const std::string& username, std::time_t start_time, std::time_t end_time){
    return ByteBuffer();
}