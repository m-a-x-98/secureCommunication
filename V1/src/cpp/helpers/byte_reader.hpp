#pragma once
#include "types.hpp"
#include <cstdint>
#include <stdexcept>

uint32_t readUint32BE(const ByteBuffer& buf, size_t offset);
uint64_t readUint64BE(const ByteBuffer& buf, size_t offset);