User keys will be stored as 
- usr name - VARCHAR(255) PRIMARY KEY
- password hash - TEXT NOT NULL (to check for tampering)
- key - BYTEA (the key stored with nonce and salt given by encryptedMessage protocol)


Messages wil be stored as 
- id - SERIAL PRIMARY KEY   
- msg - BYTEA NOT NULL 
- pad_start_offset - BIGINT NOT NULL 
- user - VARCHAR(255) NOT NULL
- recipient - VARCHAR(255) NOT NULL
- timestamp - TIMESTAMP(3) NOT NULL DEFAULT (now() AT TIME ZONE 'utc')