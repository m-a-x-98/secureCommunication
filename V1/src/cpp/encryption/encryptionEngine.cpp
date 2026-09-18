#include "encryptionEngine.hpp"
#include "doctest.h"

const char key[4] = {123, 23, 30, 21};
int key_index = 0;

void perfectEncrypt(Message& msg){
    for (char& c : msg){
        c ^ key[key_index++]; 
    }
}

void perfectDecrypt(Message& msg){
    int i = 0;
    for (char& c : msg){
        c ^ key[msg.encryption_index + i++];
    }    
}


int check_perfect_encrypt_decrypt(const char* test_msg){
    Message msg = Message(test_msg);
    perfectEncrypt(msg);
    perfectDecrypt(msg);
    return msg.equals_string(test_msg);
}

