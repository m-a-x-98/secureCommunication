#include "padallocator.hpp"

PadAllocator::PadAllocator(KeyMaterialClient& client) : client(client){}


uint64_t PadAllocator::reservePadRange(const uint64_t length){
    // Reserve a message of length, length 
    uint64_t old_pad_pos = client.getPadPosition();
    client.updatePadPosition(length);
    return old_pad_pos;
}
