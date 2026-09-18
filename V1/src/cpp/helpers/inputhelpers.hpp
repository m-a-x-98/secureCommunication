#pragma once

#include "encryptionService.hpp"
#include <istream>
#include <string>
#include <iostream>
#include <limits>
#include <cctype>

// User input helper methods
int _get_key_len(std::istream& in, bool use_presets);
int _resolve_key_len_preset(int selection);
std::string _get_usr(std::istream& in);
std::string _get_password(std::istream& in);
bool _is_password_valid(const std::string& pw);
std::unique_ptr<IencryptionService> _resolve_encryption_service_selection(int selection, std::string password);
std::unique_ptr<IencryptionService> _get_encryptor(std::istream& in, std::string password);