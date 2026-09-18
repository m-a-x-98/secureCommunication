#include "doctest.h"
#include "encryptionEngine.hpp"
#include "encryptionService.hpp"
#include "inputhelpers.hpp"
#include "types.hpp"

#include <sodium.h>
#include <sstream>

TEST_CASE("perfectEncrypt/perfectDecrypt check"){
    const char* tests[5] = {"dfl", "AOIJdksijdskdaub", ")#)EJdj29jdj2",
                             "!JHNOKDFBIJD)W", "=)(U%do290dk29jHFIW(URHJNOMD))"};
    for (const char* test : tests) {
        Message msg = Message(test);
        perfectEncrypt(msg);
        perfectDecrypt(msg);
        CHECK(msg.equals_string(test));
    }
}

TEST_CASE("AES256GCMEncryption encrypt/decrypt") {
    const char* messages[] = {
        "Hello",
        "Hello friend123 what is your name",
        "okjduoahdfpuihfeasdmfndoubfoqidnwoceocn"
    };
    std::string password = "HelloThisISMYPASSWORD123!!!";

    SUBCASE("round-trips correctly for varied message lengths") {
        AES256GCMEncryption encryptor(password);
        for (const char* msg : messages) {
            CAPTURE(msg);   // shows which message failed, if any does

            Message m(msg);
            encryptor.encrypt(m);
            CHECK(!m.equals_string(msg));   // ciphertext must differ from plaintext

            encryptor.decrypt(m);
            CHECK(m.equals_string(msg));    // must recover the exact original
        }
    }

    SUBCASE("each encryption produces different ciphertext for the same message") {
        AES256GCMEncryption encryptor(password);
        Message a("same plaintext every time");
        Message b("same plaintext every time");

        encryptor.encrypt(a);
        encryptor.encrypt(b);

        CHECK(!a.equals(b));   // fresh salt+nonce per call must make outputs differ
    }

    SUBCASE("decrypting with the wrong password fails") {
        AES256GCMEncryption encryptor(password);
        Message m("a secret message");
        encryptor.encrypt(m);

        std::string msg = "TotallyDifferentPassword456";
        AES256GCMEncryption wrongEncryptor(msg);
        CHECK_THROWS_AS(wrongEncryptor.decrypt(m), std::runtime_error);
    }

    SUBCASE("decrypting tampered ciphertext fails, doesn't silently return garbage") {
        AES256GCMEncryption encryptor(password);
        Message m("don't tamper with me");
        encryptor.encrypt(m);

        // Flip a byte inside the actual ciphertext region, not the nonce/salt prefix
        size_t offset = crypto_secretbox_NONCEBYTES + crypto_pwhash_SALTBYTES;
        *(m.begin() + offset) ^= 0xFF;

        CHECK_THROWS_AS(encryptor.decrypt(m), std::runtime_error);
    }

    SUBCASE("empty message round-trips correctly") {
        AES256GCMEncryption encryptor(password);
        Message m("");
        encryptor.encrypt(m);
        encryptor.decrypt(m);
        CHECK(m.equals_string(""));
    }
}