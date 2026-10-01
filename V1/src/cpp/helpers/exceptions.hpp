#pragma once

#include <stdexcept>
#include <string>

class PadExhaustedException : public std::runtime_error {
public:
    explicit PadExhaustedException(const std::string& message)
        : std::runtime_error(message) {}
};