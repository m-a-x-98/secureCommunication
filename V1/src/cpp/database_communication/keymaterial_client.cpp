#include "keymaterial_client.hpp"

#include <winsock2.h>
#include <ws2tcpip.h>
#include <memory>
#include <stdexcept>

#include "tcp_client.hpp"
#include "message_builders.hpp"
#include "byte_reader.hpp"


// Need to define a package protocol - and encrypt all data (dont need to do this at once) 


KeyMaterialClient::KeyMaterialClient(std::unique_ptr<TCP_client> client,
                                     const std::string& username, 
                                     const std::string& password) 
                                     : client(std::move(client)), username(username){
    if (this->client->tcp_connect(username, password) != 0) {
        throw std::runtime_error("Failed to connect/authenticate as user: " + username);
    }
}
KeyMaterialClient::~KeyMaterialClient(){}


void KeyMaterialClient::storeKeyMaterial(const ByteBuffer& keyMaterial){
    ByteBuffer msg = buildStoreKeyMessage(username, keyMaterial);
    sendAndReceive(msg);
}
ByteBuffer KeyMaterialClient::getKeyMaterial(){
    ByteBuffer reply;
    
    ByteBuffer msg = buildGetKeyMessage(username);
    reply = sendAndReceive(msg);

    ByteBuffer key_material;
    key_material.assign(reply.begin() + 5, reply.end());
    return key_material;
}

void KeyMaterialClient::updatePadPosition(uint64_t offset){
    ByteBuffer msg = buildUpdatePosMessage(username, offset);
    sendAndReceive(msg);
}
uint64_t KeyMaterialClient::getPadPosition(){    
    ByteBuffer msg = buildGetPosMessage(username);
    ByteBuffer reply = sendAndReceive(msg);

    // Start at the header size
    uint64_t key_len = readUint64BE(reply, 5);
    return key_len;
}
uint64_t KeyMaterialClient::getPadSize(){
    ByteBuffer msg = buildGetPadSizeMessage(username);
    ByteBuffer reply = sendAndReceive(msg);

    // Start at the header size
    uint64_t key_len = readUint64BE(reply, 5);
    return key_len;
}


ByteBuffer KeyMaterialClient::sendAndReceive(const ByteBuffer& request){
    if (client->send_msg(request) != 0) {
        throw std::runtime_error("Failed to send message");
    }
    ByteBuffer response;
    if (client->receive_msg(&response) != 0) {
        throw std::runtime_error("Failed to receive response");
    }
    if (response.empty()) {
        throw std::runtime_error("Empty response");
    }

    // Handle errors
    if (response[0] == 0x01) {
        throw std::runtime_error("Got an error back");
    }

    return response; // caller strips header/parses payload as needed
}