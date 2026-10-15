#pragma once

#include <stdexcept>
#include <string>

class PadExhaustedException : public std::runtime_error {
public:
    explicit PadExhaustedException(const std::string& message)
        : std::runtime_error(message) {}
};

class KeyTooShortException : public std::runtime_error {
public:
    explicit KeyTooShortException(const std::string& message)
        : std::runtime_error(message) {}
};

class TCPClientException : public std::runtime_error {
public:
    explicit TCPClientException(const std::string& message)
        : std::runtime_error(message) {}
};