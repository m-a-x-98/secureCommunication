#include "types.hpp"

Message::Message() : data(new char[1]{'\0'}), len(0), encryption_index(0) {}
Message::Message(const char* str) : encryption_index(0){
    len = str ? std::strlen(str) : 0;
    data = new char[len + 1];
    std::memcpy(data, str ? str : "", len);
    data[len] = '\0';
}

Message::Message(const char* str, int len) : len(len), encryption_index(0){
    data = new char[len + 1];
    std::memcpy(data, str ? str : "", len);
    data[len] = '\0';
}

Message::~Message() {delete[] data;}

// Copy constructor
Message::Message(const Message& msg) : len(msg.len), encryption_index(msg.encryption_index){
    data = new char[len + 1];
    std::memcpy(data, msg.data, len);
    data[len] = '\0';
}

// Move constructor
Message::Message(Message&& msg) noexcept : data(msg.data), len(msg.len), encryption_index(msg.encryption_index){
    msg.data = nullptr;
    msg.len = 0;
    msg.encryption_index = 0;
}

Message& Message::operator=(const Message& msg){
    if (this != &msg) {
        char* new_data = new char[msg.len + 1];
        if (msg.len > 0) {
            std::memcpy(new_data, msg.data, msg.len);
        }
        new_data[msg.len] = '\0';

        delete[] data;
        data = new_data;
        len = msg.len;
        encryption_index = msg.encryption_index;
    }
    return *this;
}
Message& Message::operator=(Message&& msg) noexcept{
    if (this != &msg) {
        delete[] data;

        data = msg.data;
        len = msg.len;
        encryption_index = msg.encryption_index;

        msg.data = nullptr;
        msg.len = 0;
    }
    return *this;
}



bool Message::equals_string(const char* msg) const{
    return std::strcmp(msg, data) == 0;
}

char* Message::get_msg() const{
    return data;
}

size_t Message::get_len() const{
    return len;
}

bool Message::equals(Message& msg) const{
    return strcmp(msg.data, data) == 0 && msg.len == len && msg.encryption_index == encryption_index;
}
