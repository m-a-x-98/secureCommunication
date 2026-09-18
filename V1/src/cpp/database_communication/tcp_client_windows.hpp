#pragma once

#include <winsock2.h>
#include <string>

#include "tcp_client.hpp"


class TCP_client_windows : TCP_client{
private:
    const int port;
    const std::string dest_ip;

    WSADATA wsa_data{};
    SOCKET server_socket{};
    SOCKET client_socket{};
    sockaddr_in server {};
public:
    TCP_client_windows(std::string& dest_addr, int port);
    ~TCP_client_windows();
    int connect(const std::string& password);
    int send_msg(const std::string& send_message);
    int recieve_msg(std::string* msg_buffer);
};