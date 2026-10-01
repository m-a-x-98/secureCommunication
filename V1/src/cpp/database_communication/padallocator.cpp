#include "padallocator.hpp"

#include "exceptions.hpp"

PadAllocator::PadAllocator(KeyMaterialClient& client) : client(client){}


uint64_t PadAllocator::reservePadRange(const uint64_t length){
    // If want to add concurrency, need to make this method thread safe 
    uint64_t position = client.getPadPosition();
    uint64_t size = client.getPadSize();

    if (position + length > size) throw new PadExhaustedException("");

    // Reserve bits of the pad and return the old starting position 
    client.updatePadPosition(position + length);
    return position;
}
