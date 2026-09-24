#pragma once
#include <string>
#include <vector>
#include <cstdint>
#include <optional>
#include <memory>

#include "tcp_client.hpp"

class KeyMaterialClient {
public:
    /**
     * @throws std::runtime_error if authentication fails
     */
    KeyMaterialClient(std::unique_ptr<TCP_client> client, 
                      const std::string& username, 
                      const std::string& password);
    ~KeyMaterialClient();

    /**
     * Sends a request to the java client to update the key material in the database. 
     * @throws std::runtime_error if the send or receive fails 
     */
    void storeKeyMaterial(const ByteBuffer& keyMaterial);
    /**
     * Sends a request to the java client to get the key material in the database. 
     * @throws std::runtime_error if the send or receive fails 
     */
    ByteBuffer getKeyMaterial();

    /**
     * Sends a request to the java client to update the pad position in the database. 
     * @throws std::runtime_error if the send or receive fails 
     */
    void updatePadPosition(uint64_t offset);
    /**
     * Sends a request to the java client to get the pad position in the database. 
     * @throws std::runtime_error if the send or receive fails 
     */
    uint64_t getPadPosition();

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