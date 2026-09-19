#include "tcp_client_windows.hpp"

#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>
#include <vector>

using ByteBuffer = std::vector<uint8_t>;


// At some point needs to encrypt the TCP packets and add integrity - can be done using AES-256-GCM
// For an encryption key for this, just generate some crazy key with the pad generator and save it to a admin read only file shared between cpp and java 




TCP_client_windows::TCP_client_windows(std::string& dest_ip, int port) : port(port), dest_ip(dest_ip){
    // Initialize winsock 
    int iResult = WSAStartup(MAKEWORD(2, 2), &wsa_data);
    if (iResult != NO_ERROR) {
        wprintf(L"WSAStartup function failed with error: %d\n", iResult);
        // return 1;
    }
    // Connect to a socket
    connectsocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (connectsocket == INVALID_SOCKET) {
        wprintf(L"socket function failed with error: %ld\n", WSAGetLastError());
        WSACleanup();
        // return 1;
    }
}
TCP_client_windows::~TCP_client_windows(){
    // Clean up 
    int iResult = closesocket(connectsocket);
    if (iResult == SOCKET_ERROR) {
        wprintf(L"closesocket function failed with error: %ld\n", WSAGetLastError());
        WSACleanup();
        // return 1;
    }
    WSACleanup();
}


int TCP_client_windows::tcp_connect(const std::string& usr_name, const std::string& password){
    clientService.sin_family = AF_INET;
    clientService.sin_addr.s_addr = inet_addr(dest_ip.c_str());
    clientService.sin_port = htons(port);

    // Connect to java server 
    int iResult = connect(connectsocket, (SOCKADDR*) & clientService, sizeof (clientService));
    if (iResult == SOCKET_ERROR) {
        wprintf(L"connect function failed with error: %ld\n", WSAGetLastError());
        iResult = closesocket(connectsocket);
        if (iResult == SOCKET_ERROR)
            wprintf(L"closesocket function failed with error: %ld\n", WSAGetLastError());
        WSACleanup();
        return 1;
    }
    // Server is now connected, time to authenticate the user

    std::string frame;

    return 0;
}
int TCP_client_windows::send_msg(const std::string& send_message){
    int msg_sent = 0;
    int msg_size = send_message.size();
    while (msg_sent < msg_size){
        int sent = send(connectsocket, send_message.c_str(), msg_size - msg_sent, 0);
        if (sent = SOCKET_ERROR){
            wprintf(L"send function failed with error: %ld\n", WSAGetLastError());
            return 1;
        }
        msg_sent += sent;
    }
    return 0;
}
int TCP_client_windows::receive_msg(std::string* msg_buffer){
    char buffer[4096];
    int received = recv(connectsocket, buffer, sizeof(buffer), 0);
    if (received == SOCKET_ERROR){
        wprintf(L"receive function failed with error: %ld\n", WSAGetLastError());
        return 2;
    }
    if (received == 0){
        return 1;
    }
    msg_buffer->assign(buffer);
    return 0;
}




// Constructing messages, can be moved to a separate file
// Append helpers — each knows how to serialize one field type
inline void appendByte(ByteBuffer& buf, uint8_t value) {
    buf.push_back(value);
}

inline void appendUint16(ByteBuffer& buf, uint16_t value) {
    buf.push_back(static_cast<uint8_t>((value >> 8) & 0xFF));
    buf.push_back(static_cast<uint8_t>(value & 0xFF));
}

inline void appendUint32(ByteBuffer& buf, uint32_t value) {
    for (int i = 3; i >= 0; --i) {
        buf.push_back(static_cast<uint8_t>((value >> (i * 8)) & 0xFF));
    }
}

inline void appendUint64(ByteBuffer& buf, uint64_t value) {
    for (int i = 7; i >= 0; --i) {
        buf.push_back(static_cast<uint8_t>((value >> (i * 8)) & 0xFF));
    }
}

inline void appendBytes(ByteBuffer& buf, const ByteBuffer& data) {
    buf.insert(buf.end(), data.begin(), data.end());
}

inline void appendString(ByteBuffer& buf, const std::string& s) {
    buf.insert(buf.end(), s.begin(), s.end());
}


ByteBuffer buildAuthenticateMessage(const std::string& username, const std::string& password) {
    ByteBuffer msg;
    appendByte(msg, 0x02); // message type
    appendUint16(msg, username.size()); // add the username size
    appendString(msg, username);
    appendString(msg, password);
    return msg;
}