-- Seed de municípios
INSERT INTO municipios (codigo_ibge, nome, uf, regiao) VALUES
('3550308', 'São Paulo', 'SP', 'SUDESTE'),
('3304557', 'Rio de Janeiro', 'RJ', 'SUDESTE'),
('3106200', 'Belo Horizonte', 'MG', 'SUDESTE'),
('4106902', 'Curitiba', 'PR', 'SUL'),
('5300108', 'Brasília', 'DF', 'CENTRO-OESTE')
ON CONFLICT (codigo_ibge) DO NOTHING;

-- Seed de CBOs de Tecnologia (area_tech = true) e Não-Tecnologia (area_tech = false)
INSERT INTO cbo_ocupacoes (codigo_cbo, titulo, area_tech) VALUES
('212405', 'Analista de Desenvolvimento de Sistemas', true),
('212420', 'Gerente de Projetos de TI', true),
('317110', 'Programador de Sistemas', true),
('317210', 'Operador de Computador', true),
('142105', 'Gerente Administrativo', false),
('252210', 'Contador', false)
ON CONFLICT (codigo_cbo) DO NOTHING;
