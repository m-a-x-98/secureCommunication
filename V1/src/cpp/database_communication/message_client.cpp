#include "message_client.hpp"

#include <winsock2.h>
#include <ws2tcpip.h>
#include <memory>
#include <stdexcept>
#include <vector>
#include <ctime>

#include "tcp_client.hpp"
#include "message_builders.hpp"
#include "byte_reader.hpp"


/**
 * @throws std::runtime_error if authentication fails
 */
MessageClient::MessageClient(std::unique_ptr<TCP_client> client, 
                    const std::string& username, 
                    const std::string& password){
    if (this->client->tcp_connect(username, password) != 0) {
        throw std::runtime_error("Failed to connect/authenticate as user: " + username);
    }
}
MessageClient::~MessageClient(){}

void MessageClient::addMessage(const UserMessage& message){
    ByteBuffer msg = buildAddUsrMessage(message);
    sendAndReceive(msg);
}

std::vector<UserMessage> MessageClient::getNMessages(char* usrname, int numb){
    ByteBuffer msg = buildGetNUsrMessage(username, numb);
    ByteBuffer reply = sendAndReceive(msg);

    // parse message and return
    return std::vector<UserMessage>();
}

std::vector<UserMessage> MessageClient::getMessagesTime(char* usrname, std::time_t start_time, std::time_t end_time){
    ByteBuffer msg = buildGetUserMessageTime(username, start_time, end_time);
    ByteBuffer reply = sendAndReceive(msg);

    // parse message and return
    return std::vector<UserMessage>();
}

int MessageClient::pingDB(){
    ByteBuffer msg = buildPingMessage();
    ByteBuffer outmsg = sendAndReceive(msg);
    return outmsg[0];
}
