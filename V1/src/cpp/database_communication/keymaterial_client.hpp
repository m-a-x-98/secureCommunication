#pragma once
#include <string>
#include <vector>
#include <cstdint>
#include <optional>

class KeyMaterialClient {
public:
    KeyMaterialClient(const std::string& host, uint16_t port, const std::string& username, const std::string& password);
    ~KeyMaterialClient();


    void storeKeyMaterial(const std::vector<uint8_t>& keyMaterial);
    std::vector<uint8_t> getKeyMaterial();

    void updatePadPosition(uint64_t position);
    uint64_t getPadPosition();

    void updatePadSize(uint64_t size);
    uint64_t getPadSize();

private:
    int authenticated;
    int sockfd;
    const std::string& username;
    void sendRequest(uint8_t messageType, const std::vector<uint8_t>& payload);
    std::vector<uint8_t> readResponse();
    bool authenticate(const std::string& username, const std::string& password);
};