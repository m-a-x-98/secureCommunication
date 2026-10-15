#include "encryptionEngine.hpp"
#include "exceptions.hpp"

void perfectEncrypt(Message& msg, ByteBuffer& key){
    if (key.size() < msg.get_len()) throw KeyTooShortException("");
    char* msg_text = msg.get_msg();
    for (size_t i = 0; i < msg.get_len(); i++){
        msg.update_bit(i, msg_text[i] ^ key[i]);
    }
}

void perfectDecrypt(Message& msg, ByteBuffer& key){
    if (key.size() < msg.get_len()) throw KeyTooShortException("");
    char* msg_text = msg.get_msg();
    for (size_t i = 0; i < msg.get_len(); i++){
        msg.update_bit(i, msg_text[i] ^ key[i]);
    } 
}