#pragma once
#include <string>
#include <vector>
#include <cstdint>
#include <optional>
#include <memory>
#include <vector>
#include <ctime>

#include "tcp_client.hpp"

class MessageClient {
public:
    /**
     * @throws std::runtime_error if authentication fails
     */
    MessageClient(std::unique_ptr<TCP_client> client, 
                      const std::string& username, 
                      const std::string& password);
    ~MessageClient();

    /**
     * Sends request to the java message client to add a new message. 
     * @throws std::runtime_error if the send or receive fails 
     */
    void addMessage(const UserMessage& message);
    /**
     * Sends request to the java message client to get the numb previous messages from user
     * @throws std::runtime_error if the send or receive fails 
     */
    std::vector<UserMessage> getNMessages(char* usrname, int numb);
    /**
     * Sends a request to the java client to get the key material in the database. 
     * @throws std::runtime_error if the send or receive fails 
     */
    std::vector<UserMessage> getMessagesTime(char* usrname, std::time_t start_time, std::time_t end_time);

    int pingDB();

private:
    std::unique_ptr<TCP_client> client;
    std::string username;
    
    /**
     * Sends a request and waits for a response.
     * @throws std::runtime_error if the send or receive fails, or if the
     *         server returns an error status.
     */
    ByteBuffer sendAndReceive(const ByteBuffer& request); // throws on status != 0x00
};