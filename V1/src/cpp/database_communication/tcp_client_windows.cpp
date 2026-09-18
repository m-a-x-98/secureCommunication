#include "tcp_client_windows.hpp"


TCP_client_windows::TCP_client_windows(std::string& dest_ip, int port) : dest_ip(dest_ip), port(port){
    return;
}
TCP_client_windows::~TCP_client_windows(){
    // Close the connection 
}


int TCP_client_windows::connect(const std::string& password){

}
int TCP_client_windows::send_msg(const std::string& send_message){

}
int TCP_client_windows::recieve_msg(std::string* msg_buffer){

}