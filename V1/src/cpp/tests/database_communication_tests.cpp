#include "doctest.h"
#include "encryptionEngine.hpp"
#include "encryptionService.hpp"
#include "inputhelpers.hpp"
#include "types.hpp"

#include <sstream>

TEST_CASE("KeyMaterialClient _connection") {
    SUBCASE("Accepts valid user") {
    }
    SUBCASE("Reject non-valid user") {

    }
}

TEST_CASE("KeyMaterialClient _store_get_keymaterial") {
    SUBCASE("Reject non-valid user") {
    }
    SUBCASE("Valid user updated correctly") {
    }
}

TEST_CASE("KeyMaterialClient _store_get_keymaterial") {
    SUBCASE("Reject non-valid user") {
    }
    SUBCASE("Valid user updated correctly") {
    }
}


TEST_CASE("inputhelpers _get_usr"){
    std::istringstream in("fodksodk12321¤¤#\n");
    CHECK(_get_usr(in) == "fodksodk12321¤¤#");
}
