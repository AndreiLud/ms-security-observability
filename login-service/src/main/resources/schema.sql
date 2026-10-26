-- ---------------------------------------------------------------------------
-- Tabelas do enunciado da atividade MS-AV-02
--   usuarios       -> consistencia de usuario / senha do POST /login
--   login_attempts -> controle de tentativas invalidas e de bloqueio
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS login_attempts (
    username VARCHAR(100) PRIMARY KEY,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    last_failed_at TIMESTAMP NULL,
    locked_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuarios (
    username VARCHAR(100) PRIMARY KEY,
    senha VARCHAR(10)
);

-- ---------------------------------------------------------------------------
-- Schema fixo exigido pelo JdbcUserDetailsManager (Spring Security).
-- Guarda os usuarios e as roles usadas na autorizacao dos end-points
-- protegidos (ROLE_MANUTENCAO e ROLE_ADMIN).
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS authorities (
    username VARCHAR(50) NOT NULL,
    authority VARCHAR_IGNORECASE(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users (username)
);

CREATE UNIQUE INDEX IF NOT EXISTS ix_auth_username ON authorities (username, authority);
