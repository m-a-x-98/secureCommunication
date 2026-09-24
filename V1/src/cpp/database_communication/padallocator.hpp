#pragma once

#include "keymaterial_client.hpp"

class PadAllocator {
public:
    PadAllocator(KeyMaterialClient& client);
    /**
     * Reserves the next 'length' bytes of the pad stored with the username. 
     * @returns The starting offset the caller should read pad bytes from. 
     * @throws std::runtime_error on failed send or receive. 
     */
    uint64_t reservePadRange(const uint64_t length);

private:
    KeyMaterialClient& client;
};