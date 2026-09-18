#include "inputhelpers.hpp"
#include "encryptionService.hpp"
#include <istream>
#include <string>
#include <iostream>

int _get_key_len(std::istream& in, bool use_presets) {
    std::cout << "How long should the key be:\n";
    if (use_presets)
        std::cout <<"(1) 1000 characters\n (2) 10 000 characters\n"
                    "(3) 100 000 characters\n or type your own amount\n";
    int selection;
    while (true) {
        if (in >> selection && selection > 0) break;
        std::cout << "Invalid input (must be a whole number larger than 0)\n";
        in.clear();
        in.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
    }
    if (use_presets)
        return _resolve_key_len_preset(selection);
    return selection;
}

int _resolve_key_len_preset(int selection) {
    switch (selection) {
        case 1: return 1000;
        case 2: return 10000;
        case 3: return 100000;
        default: return selection;   // treat as a custom literal length
    }
}

std::string _get_usr(std::istream& in){
    // Need to check that user name doesn't exist 
    std::string usr_name;
    std::cout<<"Provide a user name";
    std::getline(in, usr_name);
    return usr_name;
}

std::string _get_password(std::istream& in) {
    std::string pwd;
    while (true) {
        std::cout << "Provide a password (min 20 chars, upper/lower/digit/special): ";
        std::getline(in, pwd);
        if (_is_password_valid(pwd)) break;
        std::cout << "Invalid password.\n";
    }
    return pwd;
}

bool _is_password_valid(const std::string& pw) {
    if (pw.size() < 20) return false;
    bool hasUpper=false, hasLower=false, hasDigit=false, hasSpecial=false;
    for (char c : pw) {
        if (std::isupper((unsigned char)c)) hasUpper = true;
        else if (std::islower((unsigned char)c)) hasLower = true;
        else if (std::isdigit((unsigned char)c)) hasDigit = true;
        else hasSpecial = true;
    }
    return hasUpper && hasLower && hasDigit && hasSpecial;
}

std::unique_ptr<IencryptionService> _resolve_encryption_service_selection(int selection, std::string password){
    switch (selection)
    {
    case 1:
        return std::make_unique<AES256GCMEncryption>(password);
    
    default:
        throw std::invalid_argument("Encryption method not yet implemented!");
    }
}

std::unique_ptr<IencryptionService> _get_encryptor(std::istream& in, std::string password){
    int selection;
    std::cout<<"Select preferred encryption method:\n (1) AES-256-GCM\n";
    while (true){
        if (in >> selection && selection >=1 && selection < 2) break;
        std::cout<<"Invalid input\n";
        in.clear();
        in.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
    }
    return _resolve_encryption_service_selection(selection, password);
}


