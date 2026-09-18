#include "tcp_client_windows.hpp"

#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>

#pragma comment(lib, "ws2_32.lib")


TCP_client_windows::TCP_client_windows(std::string& dest_ip, int port) : dest_ip(dest_ip), port(port){
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


int TCP_client_windows::tcp_connect(const std::string& password){
    clientService.sin_family = AF_INET;
    clientService.sin_addr.s_addr = inet_addr("127.0.0.1");
    clientService.sin_port = htons(27015);

    // Connect to java server 
    int iResult = connect(connectsocket, (SOCKADDR *) & clientService, sizeof (clientService));
    if (iResult == SOCKET_ERROR) {
        wprintf(L"connect function failed with error: %ld\n", WSAGetLastError());
        iResult = closesocket(connectsocket);
        if (iResult == SOCKET_ERROR)
            wprintf(L"closesocket function failed with error: %ld\n", WSAGetLastError());
        WSACleanup();
        return 1;
    }

    // Server is now connected 
}
int TCP_client_windows::send_msg(const std::string& send_message){

}
int TCP_client_windows::recieve_msg(std::string* msg_buffer){

}