#include "keymaterial_client.hpp"

#include <winsock2.h>
#include <ws2tcpip.h>
#include <memory>
#include <stdexcept>

#include "tcp_client.hpp"
#include "message_builders.hpp"


// Need to define a package protocol - and encrypt all data (dont need to do this at once) 


KeyMaterialClient::KeyMaterialClient(std::unique_ptr<TCP_client> client,
                                     const std::string& username, 
                                     const std::string& password) 
                                     : client(std::move(client)), username(username){
    
    client->tcp_connect(username, password);
}
KeyMaterialClient::~KeyMaterialClient(){
    client->~TCP_client();
}


void KeyMaterialClient::storeKeyMaterial(const ByteBuffer& keyMaterial){
    ByteBuffer reply;
    try {
        ByteBuffer msg = buildStoreKeyMessage(username, keyMaterial);
        sendAndReceive(msg);
    }
    catch(const std::exception& e) {
        
    }
}
ByteBuffer KeyMaterialClient::getKeyMaterial(){
    ByteBuffer reply;
    try {
        ByteBuffer msg = buildGetKeyMessage(username);
        reply = sendAndReceive(msg);
    }
    catch(const std::exception& e) {
        
    }
    ByteBuffer key_material;
    key_material.assign(reply.begin() + 5, reply.end());
    return key_material;
}

void KeyMaterialClient::updatePadPosition(uint64_t position){
    ByteBuffer reply;
    try {
        ByteBuffer msg = buildUpdatePosMessage(username, position);
        reply = sendAndReceive(msg);
    }
    catch(const std::exception& e) {
        
    }
}
uint64_t KeyMaterialClient::getPadPosition(){
    ByteBuffer reply;
    try {
        ByteBuffer msg = buildGetPosMessage(username);
        reply = sendAndReceive(msg);
    }
    catch(const std::exception& e) {
        
    }
    uint64_t key_len = reply[5];
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