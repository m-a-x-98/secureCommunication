#pragma once

#include "types.hpp"
#include "sodium.h"
#include <memory>
#include <string>

class IencryptionService{
public:
    virtual void encrypt(Message& msg) = 0;
    virtual void decrypt(Message& msg) = 0;
    virtual ~IencryptionService() = default;
};

class perfectEncryption : public IencryptionService{
private:
    char* key;
    int key_index;
    int key_len;

    std::string usr_name;
    std::string password;
    std::unique_ptr<IencryptionService> encryptor;

public:
    ~perfectEncryption() override;

    void createKey();
    void login();
    void encrypt(Message& msg) override;
    void decrypt(Message& msg) override;

    std::string get_usr_name() const;
};

class AES256GCMEncryption : public IencryptionService{
private:
    std::string password;
    unsigned long key_len;


public:
    AES256GCMEncryption(std::string& password);
    ~AES256GCMEncryption() override;

    void encrypt(Message& msg);
    void decrypt(Message& msg);
};

