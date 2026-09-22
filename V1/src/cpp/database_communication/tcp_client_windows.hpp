#pragma once

#include <winsock2.h>
#include <string>

#include "tcp_client.hpp"
#include "types.hpp"



class TCP_client_windows : public TCP_client{
private:
    const int port;
    const std::string dest_ip;

    WSADATA wsa_data;
    SOCKET connectsocket;
    sockaddr_in clientService;
public:
    TCP_client_windows(std::string& dest_addr, int port);
    ~TCP_client_windows();
    int tcp_connect(const std::string& usr_name, const std::string& password);
    int send_msg(const ByteBuffer& send_message);
    int receive_msg(ByteBuffer* msg_buffer);
};