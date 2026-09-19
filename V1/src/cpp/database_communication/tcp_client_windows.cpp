#include "tcp_client_windows.hpp"

#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>



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