CREATE TABLE users
(
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(64),
    password VARCHAR(64) NOT NULL,
    role_id  INT,
    CONSTRAINT fk_user_role
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
            ON DELETE CASCADE
);