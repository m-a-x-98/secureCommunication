#include "padallocator.hpp"

PadAllocator::PadAllocator(KeyMaterialClient& client) : client(client){}


uint64_t PadAllocator::reservePadRange(const std::string& username, uint64_t length){
    // Reserve a message of length, length 
}
