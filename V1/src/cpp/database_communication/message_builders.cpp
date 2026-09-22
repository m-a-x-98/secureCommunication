#include "message_builders.hpp"

ByteBuffer buildPingMessage(){
    ByteBuffer msg;
    appendByte(msg, 0x01); // message type
    return msg;
}

ByteBuffer buildAuthenticateMessage(const std::string& username, const std::string& password) {
    ByteBuffer msg;
    appendByte(msg, 0x02); // message type
    appendString(msg, username);
    appendString(msg, password);
    return msg;
}

ByteBuffer buildStoreKeyMessage(const std::string& username, const ByteBuffer& key_material){
    ByteBuffer msg;
    appendByte(msg, 0x03); // message type
    appendString(msg, username);
    appendByteBuffer(msg, key_material);
    return msg;
}

ByteBuffer buildStoreKeyMessage(const std::string& username, const std::string& key_material){
    ByteBuffer msg;
    appendByte(msg, 0x03); // message type
    appendString(msg, username);
    appendString(msg, key_material);
    return msg;
}

ByteBuffer buildGetKeyMessage(const std::string& username){
    ByteBuffer msg;
    appendByte(msg, 0x04); // message type
    appendString(msg, username);
    return msg;
}

ByteBuffer buildUpdatePosMessage(const std::string& username, const uint64_t offset){
    ByteBuffer msg;
    appendByte(msg, 0x05); // message type
    appendString(msg, username);
    appendUint64(msg, offset);
    return msg;
}

ByteBuffer buildGetPosMessage(const std::string& username){
    ByteBuffer msg;
    appendByte(msg, 0x06); // message type
    appendString(msg, username);
    return msg;
}