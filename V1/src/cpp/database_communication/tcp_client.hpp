#pragma once

#include <string>


class TCP_client{
public:
    TCP_client(std::string& dest_addr, int port);
    ~TCP_client();
    virtual int tcp_connect(const std::string& password);
    virtual int send_msg(const std::string& send_message);
    virtual int recieve_msg(std::string* msg_buffer);
};