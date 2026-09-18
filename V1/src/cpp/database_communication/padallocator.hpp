#pragma once

#include "keymaterial_client.hpp"

class PadAllocator {
public:
    PadAllocator(KeyMaterialClient& client);

    // Reserves the next `length` bytes of pad for `username`, advancing
    // the stored position immediately as part of this call. Returns the
    // starting offset the caller should read pad bytes from.
    uint64_t reservePadRange(const std::string& username, uint64_t length);

private:
    KeyMaterialClient& client;
};