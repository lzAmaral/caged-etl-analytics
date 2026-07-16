CREATE TABLE etl_execucoes (
    id                  BIGSERIAL PRIMARY KEY,
    job_name            VARCHAR(100) NOT NULL,
    arquivo_origem      VARCHAR(255),
    linhas_lidas        INTEGER,
    linhas_processadas  INTEGER,
    linhas_rejeitadas   INTEGER,
    iniciado_em         TIMESTAMP NOT NULL,
    finalizado_em       TIMESTAMP,
    status              VARCHAR(20)
);
