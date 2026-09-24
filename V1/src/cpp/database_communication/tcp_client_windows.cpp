#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>
#include <vector>

#include "tcp_client_windows.hpp"
#include "message_builders.hpp"
#include "types.hpp"




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

    ByteBuffer authMsg = buildAuthenticateMessage(usr_name, password);
    if (send_msg(authMsg) != 0) {
        return 1;
    }

    ByteBuffer response;
    if (receive_msg(&response) != 0) {
        return 1;
    }
    if (response.empty() || response[0] != 0x00) {
        return 1; // authentication failed
    }

    return 0;
}
int TCP_client_windows::send_msg(const ByteBuffer& send_message){
    int msg_sent = 0;
    int msg_size = send_message.size();
    while (msg_sent < msg_size){
        int sent = send(connectsocket, (char*)send_message.data() + msg_sent, msg_size - msg_sent, 0);
        if (sent == SOCKET_ERROR){
            wprintf(L"send function failed with error: %ld\n", WSAGetLastError());
            return 1;
        }
        msg_sent += sent;
    }
    return 0;
}
int TCP_client_windows::receive_msg(ByteBuffer* msg_buffer){
    const size_t header_size = 5;
    uint8_t header[header_size];
    int total_received = 0;
    while (total_received < header_size){
        int received = recv(connectsocket, (char*)header + total_received, sizeof(header) - total_received, 0);
        if (received == SOCKET_ERROR){
            wprintf(L"receive function failed with error: %ld\n", WSAGetLastError());
            return 2;
        }
        if (received == 0){
            return 1;
        }
        total_received += received;
    }
    uint32_t payload_len = get_payload_len(header);
    ByteBuffer payload(payload_len);
    total_received = 0;
    while (total_received < payload_len){
        int received = recv(connectsocket, (char*)payload.data() + total_received, payload_len - total_received, 0);
        if (received == SOCKET_ERROR){
            wprintf(L"receive function failed with error: %ld\n", WSAGetLastError());
            return 2;
        }
        if (received == 0){
            return 1;
        }
        total_received += received;
    }
    msg_buffer->assign(header, header + 5);
    msg_buffer->insert(msg_buffer->end(), payload.begin(), payload.end());
    return 0;
}

uint32_t TCP_client_windows::get_payload_len(const uint8_t buffer[]){
    uint32_t payload_len = (static_cast<uint32_t>(buffer[1]) << 24) |
                           (static_cast<uint32_t>(buffer[2]) << 16) |
                           (static_cast<uint32_t>(buffer[3]) << 8)  |
                            static_cast<uint32_t>(buffer[4]);
    return payload_len;
}