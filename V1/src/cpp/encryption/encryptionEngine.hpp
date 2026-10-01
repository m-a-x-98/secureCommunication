#pragma once

#include "types.hpp"

void perfectEncrypt(Message& msg, ByteBuffer key);
void perfectDecrypt(Message& msg, ByteBuffer key);
