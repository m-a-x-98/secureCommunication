#pragma once

#include <string>


class TCP_client{
public:
    virtual ~TCP_client() = default;
    virtual int tcp_connect(const std::string& usr_name, const std::string& password) = 0;
    virtual int send_msg(const std::string& send_message) = 0;
    virtual int receive_msg(std::string* msg_buffer) = 0;
};