#include "keymaterial_client.hpp"

#include <winsock2.h>
#include <ws2tcpip.h>


// Need to define a package protocol - and encrypt all data (dont need to do this at once) 


KeyMaterialClient::KeyMaterialClient(const std::string& host, uint16_t port, const std::string& username, const std::string& password) : username(username){
    
    
    // See this: https://medium.com/@v21it039/tcp-server-in-c-window-fdb40ecd585c


    authenticated = false;
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd == INVALID_SOCKET) {
        return;
    }
}
KeyMaterialClient::~KeyMaterialClient(){

}

bool authenticate(const std::string& username, const std::string& password){
    // Send password to the java client which will authenticate it - should probably be the first message sent to the client
}

void KeyMaterialClient::storeKeyMaterial(const std::vector<uint8_t>& keyMaterial){

}
std::vector<uint8_t> KeyMaterialClient::getKeyMaterial(){

}

void KeyMaterialClient::updatePadPosition(uint64_t position){

}
uint64_t KeyMaterialClient::getPadPosition(){

}

void KeyMaterialClient::updatePadSize(uint64_t size){

}
uint64_t KeyMaterialClient::getPadSize(){

}

void KeyMaterialClient::sendRequest(uint8_t messageType, const std::vector<uint8_t>& payload){

}
std::vector<uint8_t> KeyMaterialClient::readResponse(){

}