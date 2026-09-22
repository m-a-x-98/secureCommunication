#pragma once
#include <string>
#include <vector>
#include <cstdint>
#include <optional>
#include <memory>

#include "tcp_client.hpp"

class KeyMaterialClient {
public:
    KeyMaterialClient(std::unique_ptr<TCP_client> client, 
                      const std::string& username, 
                      const std::string& password);
    ~KeyMaterialClient();

    void storeKeyMaterial(const ByteBuffer& keyMaterial);
    ByteBuffer getKeyMaterial();

    void updatePadPosition(uint64_t position);
    uint64_t getPadPosition();

private:
    std::unique_ptr<TCP_client> client;
    std::string username;
    
    ByteBuffer sendAndReceive(const ByteBuffer& request); // throws on status != 0x00
};