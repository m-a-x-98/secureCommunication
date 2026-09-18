User keys will be stored as 
- usr id - SERIAL PRIMARY KEY
- usr name - VARCHAR(255) UNIQUE NOT NULL
- password hash - TEXT NOT NULL (to check for tampering)
- key - BYTEA (the key stored with nonce and salt given by encryptedMessage protocol)