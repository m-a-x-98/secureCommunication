#include "message_builders.hpp"

ByteBuffer buildPingMessage(){
    ByteBuffer msg;
    appendByte(msg, 0x01); // message type
    return msg;
}

ByteBuffer buildAuthenticateMessage(const std::string& username, const std::string& password) {
    ByteBuffer payload;
    appendString(payload, username);
    appendString(payload, password);

    ByteBuffer msg;
    appendByte(msg, 0x02);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}

ByteBuffer buildStoreKeyMessage(const std::string& username, const ByteBuffer& key_material){
    ByteBuffer payload;
    appendString(payload, username);
    appendByteBuffer(payload, key_material);

    ByteBuffer msg;
    appendByte(msg, 0x03);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}

ByteBuffer buildStoreKeyMessage(const std::string& username, const std::string& key_material){
    ByteBuffer payload;
    appendString(payload, username);
    appendString(payload, key_material);

    ByteBuffer msg;
    appendByte(msg, 0x03);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}

ByteBuffer buildGetKeyMessage(const std::string& username){
    ByteBuffer payload;
    appendString(payload, username);

    ByteBuffer msg;
    appendByte(msg, 0x04);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}

ByteBuffer buildUpdatePosMessage(const std::string& username, const uint64_t offset){
    ByteBuffer payload;
    appendString(payload, username);
    appendUint64(payload, offset);

    ByteBuffer msg;
    appendByte(msg, 0x05);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}

ByteBuffer buildGetPosMessage(const std::string& username){
    ByteBuffer payload;
    appendString(payload, username);

    ByteBuffer msg;
    appendByte(msg, 0x06);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}

ByteBuffer buildGetPadSizeMessage(const std::string& username){
    ByteBuffer payload;
    appendString(payload, username);

    ByteBuffer msg;
    appendByte(msg, 0x07);
    appendUint32(msg, static_cast<uint32_t>(payload.size()));
    appendBytes(msg, payload);
    return msg;
}