#pragma once

#include <cstring>
#include <vector>
#include <cstdint>
#include <string>

using ByteBuffer = std::vector<uint8_t>;


class Message{
    char* data;
    size_t len;

public:
    int encryption_index;

    // Constructors
    Message();
    Message(const char* str);
    Message(const char* str, size_t len);
    // Copy
    Message(const Message& msg);
    // Move
    Message(Message&& msg) noexcept;
    ~Message();

    // Copy
    Message& operator=(const Message& other);
    // Move 
    Message& operator=(Message&& other) noexcept;

    // Iterator 
    using iterator = char*;
    using const_iterator = const char*;

    iterator begin() { return data; }
    iterator end()   { return data + len; }

    const_iterator begin() const { return data; }
    const_iterator end()   const { return data + len; }

    const_iterator cbegin() const { return data; }
    const_iterator cend()   const { return data + len; }

    void update_bit(size_t index, uint8_t val);

    // Methods 
    bool equals_string(const char* msg) const;
    bool equals(Message& msg) const;
    char* get_msg() const;
    size_t get_len() const;

    char* toString() const;
};

class UserMessage{
public:
    ByteBuffer msg;
    std::string usrname;
    std::string recipient;
    long pad_start_offset;

    UserMessage(ByteBuffer msg, std::string usrname, std::string recipient, long pad_start_offset);
};