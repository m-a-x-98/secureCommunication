#### Key database 
Message: (header - 5 bytes    body)
[1 byte, msg type][4 bytes, msg len (big endian)][payload]

Response: (header - 5 bytes   body)
[1 byte status][4 bytes, msg len(big endian)][payload]

Status codes:
0x00 = OK
0x01 = General error 


Message types and payload
Ping (0x01): empty
Authentication (0x02): [2 byte, username size in bytes][username][password]
Store key material (0x03): [2 byte, username size in bytes][username][key material]
Get key material (0x04): [2 byte, username size in bytes][username]
Update pad position (0x05): [2 byte, username size in bytes][username][offset]
Get pad position (0x06): [2 byte, username size in bytes][username]
