CREATE VIEW vw_saldo_mensal_regiao AS
SELECT
    m.regiao,
    a.competencia,
    COUNT(*) FILTER (WHERE a.tipo_movimentacao = 'ADMISSAO') AS total_admissoes,
    COUNT(*) FILTER (WHERE a.tipo_movimentacao = 'DESLIGAMENTO') AS total_desligamentos,
    COUNT(*) FILTER (WHERE a.tipo_movimentacao = 'ADMISSAO')
        - COUNT(*) FILTER (WHERE a.tipo_movimentacao = 'DESLIGAMENTO') AS saldo
FROM admissoes a
JOIN municipios m ON m.id = a.municipio_id
GROUP BY m.regiao, a.competencia
ORDER BY a.competencia, m.regiao;

CREATE VIEW vw_salario_medio_tech AS
SELECT
    c.titulo AS ocupacao,
    EXTRACT(YEAR FROM a.competencia) AS ano,
    ROUND(AVG(a.salario), 2) AS salario_medio,
    COUNT(*) AS total_registros
FROM admissoes a
JOIN cbo_ocupacoes c ON c.id = a.cbo_id
WHERE c.area_tech = TRUE
  AND a.tipo_movimentacao = 'ADMISSAO'
GROUP BY c.titulo, EXTRACT(YEAR FROM a.competencia)
ORDER BY ano, ocupacao;

CREATE VIEW vw_ranking_uf_tech AS
SELECT
    m.uf,
    COUNT(*) AS total_admissoes_tech
FROM admissoes a
JOIN municipios m ON m.id = a.municipio_id
JOIN cbo_ocupacoes c ON c.id = a.cbo_id
WHERE c.area_tech = TRUE
  AND a.tipo_movimentacao = 'ADMISSAO'
GROUP BY m.uf
ORDER BY total_admissoes_tech DESC;
