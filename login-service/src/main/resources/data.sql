-- ---------------------------------------------------------------------------
-- Usuarios de negocio (tabela usuarios)
-- Senha em texto puro porque a coluna definida no enunciado e VARCHAR(10)
-- e a consistencia do POST /login e feita contra esta propria tabela.
-- ---------------------------------------------------------------------------
INSERT INTO usuarios (username, senha) VALUES ('joao', '123456');
INSERT INTO usuarios (username, senha) VALUES ('maria', 'abc123');
INSERT INTO usuarios (username, senha) VALUES ('admin', 'admin');
INSERT INTO usuarios (username, senha) VALUES ('manutencao', 'manutencao');

-- ---------------------------------------------------------------------------
-- Usuarios do JdbcUserDetailsManager e suas roles.
-- Usados na autenticacao HTTP Basic dos end-points protegidos.
-- {noop} indica senha sem criptografia (vide DelegatingPasswordEncoder).
-- ---------------------------------------------------------------------------
INSERT INTO users (username, password, enabled) VALUES ('admin', '{noop}admin', TRUE);
INSERT INTO users (username, password, enabled) VALUES ('manutencao', '{noop}manutencao', TRUE);

INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
INSERT INTO authorities (username, authority) VALUES ('manutencao', 'ROLE_MANUTENCAO');
