#include "doctest.h"
#include "keymaterial_client.hpp"
#include "tcp_client.hpp"
#include "tcp_client_windows.hpp"
#include "types.hpp"

#include <memory>
#include <sstream>

// assumes a user named "testing" with password "pwd123" exists

TEST_CASE("KeyMaterialClient _connection") {
    SUBCASE("Accepts valid user") {
        std::string dest_addr = "127.0.0.1";
        std::string usrname = "testing";
        std::string pwd = "pwd123";

        std::unique_ptr<KeyMaterialClient> kmc;

        CHECK_NOTHROW(kmc = std::make_unique<KeyMaterialClient>(
            std::make_unique<TCP_client_windows>(dest_addr, 5000), usrname, pwd));

        REQUIRE(kmc != nullptr);
    }
    SUBCASE("Reject non-valid password") {
        std::string dest_addr = "127.0.0.1";
        std::string usrname = "testing";
        std::string pwd = "wrongpwd123";

        std::unique_ptr<KeyMaterialClient> kmc;

        CHECK_THROWS_AS(kmc = std::make_unique<KeyMaterialClient>(
            std::make_unique<TCP_client_windows>(dest_addr, 5000), usrname, pwd), std::runtime_error);

        REQUIRE(kmc == nullptr);
    }
    SUBCASE("Reject non-valid username") {
        std::string dest_addr = "127.0.0.1";
        std::string usrname = "no_such_user_ajsdlfk";
        std::string pwd = "pwd123";

        std::unique_ptr<KeyMaterialClient> kmc;

        CHECK_THROWS_AS(kmc = std::make_unique<KeyMaterialClient>(
            std::make_unique<TCP_client_windows>(dest_addr, 5000), usrname, pwd), std::runtime_error);

        REQUIRE(kmc == nullptr);
    }
}

TEST_CASE("KeyMaterialClient _store_get_keymaterial") {
    std::string dest_addr = "127.0.0.1";
    std::string usrname = "testing";
    std::string pwd = "pwd123";

    std::unique_ptr<KeyMaterialClient> kmc = std::make_unique<KeyMaterialClient>(
        std::make_unique<TCP_client_windows>(dest_addr, 5000), usrname, pwd);

    SUBCASE("Store then get returns identical bytes") {
        ByteBuffer key = {1, 2, 3, 4, 5, 'A', 'B', 'C', '!', '?'};
        CHECK_NOTHROW(kmc->storeKeyMaterial(key));

        ByteBuffer out_key;
        CHECK_NOTHROW(out_key = kmc->getKeyMaterial());
        REQUIRE(!out_key.empty());

        CHECK(key == out_key);
    }

    SUBCASE("Storing new key material overwrites the previous value") {
        ByteBuffer first = {1, 2, 3};
        ByteBuffer second = {9, 8, 7, 6};

        CHECK_NOTHROW(kmc->storeKeyMaterial(first));
        CHECK_NOTHROW(kmc->storeKeyMaterial(second));

        ByteBuffer out_key;
        CHECK_NOTHROW(out_key = kmc->getKeyMaterial());
        CHECK(out_key == second);
    }

    SUBCASE("Empty key material round-trips correctly") {
        ByteBuffer empty_key;
        CHECK_NOTHROW(kmc->storeKeyMaterial(empty_key));

        ByteBuffer out_key;
        CHECK_NOTHROW(out_key = kmc->getKeyMaterial());
        CHECK(out_key.empty());
    }
}

TEST_CASE("KeyMaterialClient _store_get_padPos") {
    std::string dest_addr = "127.0.0.1";
    std::string usrname = "testing";
    std::string pwd = "pwd123";

    std::unique_ptr<KeyMaterialClient> kmc = std::make_unique<KeyMaterialClient>(
        std::make_unique<TCP_client_windows>(dest_addr, 5000), usrname, pwd);

    SUBCASE("Update then get returns the same position") {
        uint64_t position = 42;
        CHECK_NOTHROW(kmc->updatePadPosition(position));

        uint64_t out_position = 0;
        CHECK_NOTHROW(out_position = kmc->getPadPosition());
        CHECK(out_position == position);
    }

    SUBCASE("Repeated updates: last write wins") {
        CHECK_NOTHROW(kmc->updatePadPosition(10));
        CHECK_NOTHROW(kmc->updatePadPosition(999));

        uint64_t out_position = 0;
        CHECK_NOTHROW(out_position = kmc->getPadPosition());
        CHECK(out_position == 999);
    }

    SUBCASE("Position value exceeding 32 bits round-trips correctly") {
        // Specifically exercises the 4-byte vs 8-byte wire-width question —
        // this will fail if the protocol's offset field is still 4 bytes
        // while MessageCodec/readLongAt assume 8.
        uint64_t large_position = 5'000'000'000ULL; // > UINT32_MAX
        CHECK_NOTHROW(kmc->updatePadPosition(large_position));

        uint64_t out_position = 0;
        CHECK_NOTHROW(out_position = kmc->getPadPosition());
        CHECK(out_position == large_position);
    }
}

TEST_CASE("KeyMaterialClient _pad_size") {
    std::string dest_addr = "127.0.0.1";
    std::string usrname = "testing";
    std::string pwd = "pwd123";

    std::unique_ptr<KeyMaterialClient> kmc = std::make_unique<KeyMaterialClient>(
        std::make_unique<TCP_client_windows>(dest_addr, 5000), usrname, pwd);

    SUBCASE("Get pad size for a user with no size set returns 0") {
        // NOTE: only valid if this test user genuinely has never had pad_size set;
        // if other tests in this run set it first, this subcase may need its own
        // dedicated test user to stay meaningful.
        uint64_t size = 0;
        CHECK_NOTHROW(size = kmc->getPadSize());
        CHECK(size == 0);
    }
}