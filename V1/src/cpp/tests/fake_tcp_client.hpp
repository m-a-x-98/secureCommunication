#pragma once
#include <memory>
#include <string>
#include "tcp_client.hpp"
#include "types.hpp"

// Plays the part of KeyMaterialServer, following database_msg.md
struct FakeKeyServer {
    ByteBuffer key;
    uint64_t pad_position = 0;
    uint64_t pad_size = 0;
    uint8_t forced_status = 0x00;   // set to 0x01 to make every request fail
    bool accept_login = true;
};

class FakeTCPClient : public TCP_client {
public:
    explicit FakeTCPClient(std::shared_ptr<FakeKeyServer> s) : server(std::move(s)) {}

    int tcp_connect(const std::string&, const std::string&) override {
        return server->accept_login ? 0 : 1;
    }
    int send_msg(const ByteBuffer& msg) override { reply = handle(msg); return 0; }
    int receive_msg(ByteBuffer* out) override { *out = reply; return 0; }

private:
    std::shared_ptr<FakeKeyServer> server;
    ByteBuffer reply;

    static uint64_t be(const ByteBuffer& b, size_t pos, size_t n) {
        uint64_t v = 0;
        for (size_t i = 0; i < n; i++) v = (v << 8) | b[pos + i];
        return v;
    }
    static ByteBuffer u64(uint64_t v) {
        ByteBuffer b;
        for (int i = 7; i >= 0; --i) b.push_back(static_cast<uint8_t>(v >> (8 * i)));
        return b;
    }
    static ByteBuffer respond(uint8_t status, const ByteBuffer& payload = {}) {
        ByteBuffer r{status};
        uint32_t len = static_cast<uint32_t>(payload.size());
        for (int i = 3; i >= 0; --i) r.push_back(static_cast<uint8_t>(len >> (8 * i)));
        r.insert(r.end(), payload.begin(), payload.end());
        return r;
    }

    ByteBuffer handle(const ByteBuffer& msg) {
        if (server->forced_status != 0x00) return respond(server->forced_status);
        if (msg.size() < 7 || be(msg, 1, 4) != msg.size() - 5) return respond(0x01);

        size_t pos = 5 + 2 + be(msg, 5, 2);   // skip [2 username len][username]
        switch (msg[0]) {
            case 0x03: {                       // [8 key len][key]
                if (msg.size() < pos + 8 || be(msg, pos, 8) != msg.size() - pos - 8) return respond(0x01);
                server->key.assign(msg.begin() + static_cast<std::ptrdiff_t>(pos + 8), msg.end());
                return respond(0x00, {0x00});
            }
            case 0x04: return respond(0x00, server->key);
            case 0x05: server->pad_position = be(msg, pos, 8); return respond(0x00, {0x00});
            case 0x06: return respond(0x00, u64(server->pad_position));
            case 0x07: return respond(0x00, u64(server->pad_size));
            default:   return respond(0x01);
        }
    }
};