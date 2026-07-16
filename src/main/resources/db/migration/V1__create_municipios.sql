CREATE TABLE municipios (
    id              BIGSERIAL PRIMARY KEY,
    codigo_ibge     VARCHAR(7) NOT NULL UNIQUE,
    nome            VARCHAR(150) NOT NULL,
    uf              VARCHAR(2) NOT NULL,
    regiao          VARCHAR(20)
);

CREATE INDEX idx_municipios_uf ON municipios(uf);
