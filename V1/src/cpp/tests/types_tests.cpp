#include "doctest.h"
#include "types.hpp"
#include <memory>
#include <string>

TEST_CASE("Message move-assignment replaces contents correctly") {
    Message msg("original", 8);
    Message replacement("replaced!", 9);

    msg = std::move(replacement);

    CHECK(msg.equals_string("replaced!"));
}

TEST_CASE("Message copy-assignment deep-copies, doesn't alias") {
    Message a("hello", 5);
    Message b("world", 5);

    b = a;   // copy assignment
    CHECK(b.equals_string("hello"));

    // mutate through a's iterator, confirm b is unaffected (deep copy, not shared buffer)
    *a.begin() = 'X';
    CHECK(b.equals_string("hello"));   // b untouched
}