#include "doctest.h"
#include "byte_reader.hpp"
#include "types.hpp"

TEST_CASE("readUint32BE") {
    SUBCASE("reads a known value correctly") {
        ByteBuffer buf = {0x00, 0x00, 0x01, 0x00}; // 256
        CHECK(readUint32BE(buf, 0) == 256);
    }
    SUBCASE("respects a non-zero offset") {
        ByteBuffer buf = {0xFF, 0x00, 0x00, 0x00, 0x2A}; // 42 at the end
        CHECK(readUint32BE(buf, 1) == 42);
    }
    SUBCASE("respects a non-zero offset") {
        ByteBuffer buf = {0x00, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x2A}; // 42 at the end
        CHECK(readUint64BE(buf, 5) == 42);
    }
    SUBCASE("handles max value correctly, including sign-extension pitfalls") {
        ByteBuffer buf = {0xFF, 0xFF, 0xFF, 0xFF}; // would sign-extend badly if read as signed char
        CHECK(readUint32BE(buf, 0) == 0xFFFFFFFFu);
    }
    SUBCASE("throws when buffer is too short for the offset") {
        ByteBuffer buf = {0x01, 0x02};
        CHECK_THROWS_AS(readUint32BE(buf, 0), std::runtime_error);
    }
}

TEST_CASE("readUint64BE") {
    SUBCASE("reads a known small value placed in the last byte") {
        // exactly reproduces the bug: value 42 encoded as 8 bytes big-endian,
        // with the non-zero byte at the final position
        ByteBuffer buf = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x2A};
        CHECK(readUint64BE(buf, 0) == 42);
    }
    SUBCASE("reads a value exceeding 32 bits correctly") {
        uint64_t original = 5'000'000'000ULL; // > UINT32_MAX
        ByteBuffer buf(8);
        for (int i = 0; i < 8; i++) {
            buf[7 - i] = static_cast<uint8_t>(original >> (i * 8));
        }
        CHECK(readUint64BE(buf, 0) == original);
    }
    SUBCASE("respects a non-zero offset") {
        ByteBuffer buf = {0xAA, 0xAA, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x2A};
        CHECK(readUint64BE(buf, 2) == 42);
    }
    SUBCASE("throws when buffer is too short for the offset") {
        ByteBuffer buf = {0x01, 0x02, 0x03};
        CHECK_THROWS_AS(readUint64BE(buf, 0), std::runtime_error);
    }
}