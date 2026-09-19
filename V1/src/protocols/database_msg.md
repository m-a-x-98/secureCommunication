#### Key database 
Message: (header - 5 bytes    body)
[1 byte, msg type][4 bytes, msg len (big endian)][payload]

Response: (header - 5 bytes   body)
[1 byte status][4 bytes, msg len(big endian)][payload]

Status codes:
0 = OK
1 = General error 


Message types and payload
Ping (1): empty
Authentication (2): [username (4 bytes)][password]
Store key material (3): [username (4 bytes)][key material]
Get key material (4): [username (4 bytes)]
Update pad position (5): [username (4 bytes)][offset]
Get pad position (6): [username (4 bytes)]
