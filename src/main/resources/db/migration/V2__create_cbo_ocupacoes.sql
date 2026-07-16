CREATE TABLE cbo_ocupacoes (
    id              BIGSERIAL PRIMARY KEY,
    codigo_cbo      VARCHAR(6) NOT NULL UNIQUE,
    titulo          VARCHAR(200) NOT NULL,
    area_tech       BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_cbo_area_tech ON cbo_ocupacoes(area_tech);
