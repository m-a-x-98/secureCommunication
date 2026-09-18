#include "doctest.h"
#include "encryptionEngine.hpp"
#include "encryptionService.hpp"
#include "inputhelpers.hpp"
#include "types.hpp"

#include <sodium.h>
#include <sstream>

TEST_CASE("inputhelpers _get_password"){
    std::istringstream in("badPassword\nbadPassword123\nbadPassword123456789\n!!BadPassword123\n!GoodPassword123456!\n");
    std::string pwd = _get_password(in);
    CHECK(pwd == "!GoodPassword123456!");
}
TEST_CASE("inputhelpers _get_key_len") {
    SUBCASE("rejects invalid tokens, then accepts a custom value") {
        std::istringstream in("dsadre2323\n!2324\ner\n12e\n");
        CHECK(_get_key_len(in, true) == 12);
    }
    SUBCASE("preset disabled") {
        std::istringstream in("1\n");
        CHECK(_get_key_len(in, false) == 1);
    }
    SUBCASE("preset 1 maps to 1000") {
        std::istringstream in("1\n");
        CHECK(_get_key_len(in, true) == 1000);
    }
    SUBCASE("preset 2 maps to 10000") {
        std::istringstream in("2\n");
        CHECK(_get_key_len(in, true) == 10000);
    }
    SUBCASE("preset 3 maps to 100000") {
        std::istringstream in("3\n");
        CHECK(_get_key_len(in, true) == 100000);
    }
}

TEST_CASE("inputhelpers _get_usr"){
    std::istringstream in("fodksodk12321¤¤#\n");
    CHECK(_get_usr(in) == "fodksodk12321¤¤#");
}

TEST_CASE("inputhelpers _get_encryptor"){
    SUBCASE("selection 1 returns a valid AES256GCMEncryption") {
        std::istringstream in("1\n");
        auto encryptor = _get_encryptor(in, "okadosjds");
        REQUIRE(encryptor != nullptr);
        CHECK(dynamic_cast<AES256GCMEncryption*>(encryptor.get()) != nullptr);
    }

    SUBCASE("negative and non-numeric input is rejected before a valid selection is accepted") {
        std::istringstream in("-5\nabc\n1\n");
        auto encryptor = _get_encryptor(in, "okadosjds");
        REQUIRE(encryptor != nullptr);
        CHECK(dynamic_cast<AES256GCMEncryption*>(encryptor.get()) != nullptr);
    }
}