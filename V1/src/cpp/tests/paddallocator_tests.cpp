#include "doctest.h"
#include "exceptions.hpp"
#include "fake_tcp_client.hpp"
#include "keymaterial_client.hpp"
#include "padallocator.hpp"

TEST_CASE("PadAllocator reserves before use") {
    auto server = std::make_shared<FakeKeyServer>();
    server->pad_position = 10;
    server->pad_size = 100;
    KeyMaterialClient client(std::make_unique<FakeTCPClient>(server), "alice", "pwd");
    PadAllocator alloc(client);

    SUBCASE("returns the old position and advances the stored one") {
        CHECK(alloc.reservePadRange(5) == 10);
        CHECK(server->pad_position == 15);
    }
    SUBCASE("running out throws and leaves the position alone") {
        server->pad_position = 96;
        CHECK_THROWS_AS(alloc.reservePadRange(5), PadExhaustedException);
        CHECK(server->pad_position == 96);
    }
    SUBCASE("position + length overflow is not allowed to wrap") {
        server->pad_position = UINT64_MAX - 1;
        server->pad_size = UINT64_MAX;
        CHECK_THROWS_AS(alloc.reservePadRange(10), PadExhaustedException);
    }
}

TEST_CASE("KeyMaterialClient round-trips binary key material") {
    auto server = std::make_shared<FakeKeyServer>();
    KeyMaterialClient client(std::make_unique<FakeTCPClient>(server), "alice", "pwd");

    ByteBuffer key = {0x00, 0xFF, 0x10, 0x00};
    client.storeKeyMaterial(key);
    CHECK(server->key == key);
    CHECK(client.getKeyMaterial() == key);
}