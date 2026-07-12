CREATE TABLE admissoes (
    id                  BIGSERIAL PRIMARY KEY,
    municipio_id        INTEGER NOT NULL REFERENCES municipios(id),
    cbo_id              INTEGER NOT NULL REFERENCES cbo_ocupacoes(id),
    competencia         DATE NOT NULL,
    tipo_movimentacao   VARCHAR(20) NOT NULL,
    salario              NUMERIC(10,2),
    grau_instrucao      VARCHAR(50),
    sexo                CHAR(1),
    idade               SMALLINT,
    tipo_empregador     VARCHAR(50),
    criado_em           TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_admissoes_competencia ON admissoes(competencia);
CREATE INDEX idx_admissoes_municipio ON admissoes(municipio_id);
CREATE INDEX idx_admissoes_cbo ON admissoes(cbo_id);
CREATE INDEX idx_admissoes_tipo ON admissoes(tipo_movimentacao);
