#include "encryptionService.hpp"
#include "encryptionEngine.hpp"
#include "inputhelpers.hpp"
#include "doctest.h"
#include "sodium.h"
#include "types.hpp"
#include <iostream>
#include <memory>
#include <limits>
#include <string>
#include <vector>
#include <stdexcept>

perfectEncryption::~perfectEncryption() = default;

static unsigned char* gen_random_num(const unsigned long len){
    unsigned char* buffer = new unsigned char[len];
    randombytes_buf(buffer, len);   // uses the right CSPRNG per platform automatically
    return buffer;
}

void perfectEncryption::createKey(){
    // First ask for the length of the key (give three options)
    // Second provide username for key
    // Third provide preferde encryption method and beefyness (create the correct object for the encryption service)
    // Fourth ask for a password (minimum 20 characters with capital and lower case letter, digits and special characters)
    // Sixth generate random key

    // Seventh encrypt the key (this step can be skipped for now, just call correct encrypt service)
    // Eight store the key and the current index in a database - database should be encrypted/password protected

    key_len = _get_key_len(std::cin, true);
    usr_name = _get_usr(std::cin);
    password = _get_password(std::cin);
    encryptor = _get_encryptor(std::cin, password);

    // Create the key
    unsigned char* buffer = gen_random_num(key_len);

    // Encrypt the key
    Message key_msg = Message(reinterpret_cast<char*>(buffer), key_len);
    key = key_msg.get_msg();
    delete[] buffer;
    encryptor->encrypt(key_msg);
    key_msg.get_msg(); // Place this in the database 

    // Save the encrypted key, key_index, key_len and encryptor_type in a database 
}
void perfectEncryption::login(){
    usr_name = _get_usr(std::cin);

    // Some method to load the key, key_index, key_len and encryptor_type from the database 

    password = _get_password(std::cin);

    // Ask for password 
    // Decrypt the key and store it in the private variable key (check if this is secure, since this memory could technically be discted)
}
void perfectEncryption::encrypt(Message& msg){
    perfectEncrypt(msg);
}
void perfectEncryption::decrypt(Message& msg){
    perfectDecrypt(msg);
}

std::string perfectEncryption::get_usr_name() const{
    return usr_name;
}


AES256GCMEncryption::AES256GCMEncryption(std::string& password) : password(password){}
AES256GCMEncryption::~AES256GCMEncryption() = default;

void AES256GCMEncryption::encrypt(Message& msg){
    unsigned char salt[crypto_pwhash_SALTBYTES];
    unsigned char key[crypto_secretbox_KEYBYTES];

    randombytes_buf(salt, sizeof salt);

    if (crypto_pwhash
        (key, sizeof key, password.data(), password.size(), salt,
        crypto_pwhash_OPSLIMIT_INTERACTIVE, crypto_pwhash_MEMLIMIT_INTERACTIVE,
        crypto_pwhash_ALG_DEFAULT) != 0) 
    {   
        throw std::runtime_error("Key derivation failed (likely out of memory)");
    }
    unsigned char nonce[crypto_secretbox_NONCEBYTES];
    randombytes_buf(nonce, sizeof nonce);

    size_t ciphertext_len = msg.get_len() + crypto_secretbox_MACBYTES;
    unsigned char* ciphertext = new unsigned char[ciphertext_len];
    crypto_secretbox_easy(ciphertext, reinterpret_cast<const unsigned char*>(msg.get_msg()), msg.get_len(), nonce, key);

    size_t output_size = sizeof nonce + sizeof salt + ciphertext_len;
    unsigned char* output = new unsigned char[output_size];
    std::memcpy(output, nonce, sizeof nonce);
    std::memcpy(output + sizeof nonce, salt, sizeof salt);
    std::memcpy(output + sizeof nonce + sizeof salt, ciphertext, ciphertext_len);
    msg = Message(reinterpret_cast<char*>(output), output_size);

    delete[] ciphertext;
    delete[] output;

    // Salt and nonce must be stored together with the message 
}
void AES256GCMEncryption::decrypt(Message& msg){
    const char* raw = msg.get_msg();
    size_t total_len = msg.get_len();

    // Assumes msg contains nonce and salt 
    if (total_len < crypto_secretbox_NONCEBYTES + crypto_pwhash_SALTBYTES + crypto_secretbox_MACBYTES) {
        throw std::runtime_error("Message too short to be valid ciphertext");
    }

    unsigned char nonce[crypto_secretbox_NONCEBYTES];
    unsigned char salt[crypto_pwhash_SALTBYTES];
    std::memcpy(nonce, raw, crypto_secretbox_NONCEBYTES);
    std::memcpy(salt, raw + crypto_secretbox_NONCEBYTES, crypto_pwhash_SALTBYTES);
    
    size_t ciphertext_len = total_len - crypto_secretbox_NONCEBYTES - crypto_pwhash_SALTBYTES;
    const unsigned char* ciphertext = reinterpret_cast<const unsigned char*>(
        raw + crypto_secretbox_NONCEBYTES + crypto_pwhash_SALTBYTES
    );

    unsigned char key[crypto_secretbox_KEYBYTES];
    if (crypto_pwhash
        (key, sizeof key, password.data(), password.size(), salt,
        crypto_pwhash_OPSLIMIT_INTERACTIVE, crypto_pwhash_MEMLIMIT_INTERACTIVE,
        crypto_pwhash_ALG_DEFAULT) != 0) 
    {   
        throw std::runtime_error("Key derivation failed (likely out of memory)");
    }
    size_t plaintext_len = ciphertext_len - crypto_secretbox_MACBYTES;
    unsigned char* plaintext = new unsigned char[plaintext_len];

    if (crypto_secretbox_open_easy(plaintext, ciphertext, ciphertext_len, nonce, key) != 0) {
        throw std::runtime_error("Decryption failed: wrong password or corrupted/tampered data");
    }

    msg = Message(reinterpret_cast<char*>(plaintext), plaintext_len);

    delete[] plaintext;
}


