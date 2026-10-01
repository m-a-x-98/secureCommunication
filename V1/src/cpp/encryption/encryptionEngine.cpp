#include "encryptionEngine.hpp"
#include "doctest.h"

void perfectEncrypt(Message& msg, ByteBuffer key){
    char* msg_text = msg.get_msg();
    for (size_t i = 0; i < msg.get_len(); i++){
        msg.update_bit(i, msg_text[i] ^ key[i]);
    }
}

void perfectDecrypt(Message& msg, ByteBuffer key){
    char* msg_text = msg.get_msg();
    for (size_t i = 0; i < msg.get_len(); i++){
        msg.update_bit(i, msg_text[i] ^ key[i]);
    } 
}
