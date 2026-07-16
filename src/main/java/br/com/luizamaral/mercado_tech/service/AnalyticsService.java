package br.com.luizamaral.mercado_tech.service;

import br.com.luizamaral.mercado_tech.service.dto.HiringByRegionDTO;
import br.com.luizamaral.mercado_tech.service.dto.RankingUfDTO;
import br.com.luizamaral.mercado_tech.service.dto.SalaryTrendDTO;
import br.com.luizamaral.mercado_tech.service.dto.TemporalRangeDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    private final JdbcTemplate jdbc;

    // Usamos JdbcTemplate ao invés de @Query em repositórios porque as views
    // são agregações — não representam entidades de domínio e não fazem sentido
    // como @Entity. JdbcTemplate permite mapear o ResultSet diretamente para DTOs
    // sem a cerimônia de criar entidades "somente leitura" só para isso.
    public AnalyticsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Consulta salário médio por ocupação de TI e ano, com filtro opcional por UF.
     */
    public List<SalaryTrendDTO> getSalaryTrends(String uf) {
        String sql = """
                SELECT
                    c.titulo AS ocupacao,
                    CAST(EXTRACT(YEAR FROM a.competencia) AS INTEGER) AS ano,
                    ROUND(AVG(a.salario), 2) AS salario_medio,
                    ROUND(CAST(percentile_cont(0.25) WITHIN GROUP (ORDER BY a.salario) AS NUMERIC), 2) AS salario_junior,
                    ROUND(CAST(percentile_cont(0.50) WITHIN GROUP (ORDER BY a.salario) AS NUMERIC), 2) AS salario_pleno,
                    ROUND(CAST(percentile_cont(0.75) WITHIN GROUP (ORDER BY a.salario) AS NUMERIC), 2) AS salario_senior,
                    COUNT(*) AS total_registros
                FROM admissoes a
                JOIN cbo_ocupacoes c ON c.id = a.cbo_id
                JOIN municipios m ON m.id = a.municipio_id
                WHERE c.area_tech = TRUE
                  AND a.tipo_movimentacao = 'ADMISSAO'
                """;

        if (uf != null && !uf.isEmpty() && !uf.equalsIgnoreCase("ALL")) {
            sql += " AND m.uf = ? ";
            sql += " GROUP BY c.titulo, EXTRACT(YEAR FROM a.competencia) ORDER BY ano, ocupacao ";
            return jdbc.query(sql, (rs, rowNum) -> new SalaryTrendDTO(
                    rs.getString("ocupacao"),
                    rs.getInt("ano"),
                    rs.getBigDecimal("salario_medio"),
                    rs.getBigDecimal("salario_junior"),
                    rs.getBigDecimal("salario_pleno"),
                    rs.getBigDecimal("salario_senior"),
                    rs.getLong("total_registros")
            ), uf);
        } else {
            sql += " GROUP BY c.titulo, EXTRACT(YEAR FROM a.competencia) ORDER BY ano, ocupacao ";
            return jdbc.query(sql, (rs, rowNum) -> new SalaryTrendDTO(
                    rs.getString("ocupacao"),
                    rs.getInt("ano"),
                    rs.getBigDecimal("salario_medio"),
                    rs.getBigDecimal("salario_junior"),
                    rs.getBigDecimal("salario_pleno"),
                    rs.getBigDecimal("salario_senior"),
                    rs.getLong("total_registros")
            ));
        }
    }

    /**
     * Consulta saldo admissões vs desligamentos por região/UF e mês, com filtros opcionais.
     */
    public List<HiringByRegionDTO> getHiringByRegion(String uf, String cbo) {
        boolean hasUf = uf != null && !uf.isEmpty() && !uf.equalsIgnoreCase("ALL");
        boolean hasCbo = cbo != null && !cbo.isEmpty() && !cbo.equalsIgnoreCase("ALL");
        
        String selectField = hasUf ? "m.uf" : "m.regiao";
        
        String sql = "SELECT " + selectField + " AS regiao, a.competencia, " +
                     "COUNT(*) FILTER (WHERE a.tipo_movimentacao = 'ADMISSAO') AS total_admissoes, " +
                     "COUNT(*) FILTER (WHERE a.tipo_movimentacao = 'DESLIGAMENTO') AS total_desligamentos, " +
                     "COUNT(*) FILTER (WHERE a.tipo_movimentacao = 'ADMISSAO') - COUNT(*) FILTER (WHERE a.tipo_movimentacao = 'DESLIGAMENTO') AS saldo " +
                     "FROM admissoes a " +
                     "JOIN municipios m ON m.id = a.municipio_id " +
                     "JOIN cbo_ocupacoes c ON c.id = a.cbo_id " +
                     "WHERE c.area_tech = TRUE ";
                     
        List<Object> params = new ArrayList<>();
        if (hasUf) {
            sql += " AND m.uf = ? ";
            params.add(uf);
        }
        if (hasCbo) {
            sql += " AND c.titulo = ? ";
            params.add(cbo);
        }
        
        sql += " GROUP BY " + selectField + ", a.competencia ORDER BY a.competencia, regiao ";
        
        return jdbc.query(sql, (rs, rowNum) -> new HiringByRegionDTO(
                rs.getString("regiao"),
                rs.getObject("competencia", LocalDate.class),
                rs.getLong("total_admissoes"),
                rs.getLong("total_desligamentos"),
                rs.getLong("saldo")
        ), params.toArray());
    }

    /**
     * Consulta ranking de UFs por total de admissões em TI, com filtro opcional por CBO.
     */
    public List<RankingUfDTO> getRankingUf(String cbo) {
        String sql = """
                SELECT
                    m.uf,
                    COUNT(*) AS total_admissoes_tech
                FROM admissoes a
                JOIN municipios m ON m.id = a.municipio_id
                JOIN cbo_ocupacoes c ON c.id = a.cbo_id
                WHERE c.area_tech = TRUE
                  AND a.tipo_movimentacao = 'ADMISSAO'
                """;

        if (cbo != null && !cbo.isEmpty() && !cbo.equalsIgnoreCase("ALL")) {
            sql += " AND c.titulo = ? ";
            sql += " GROUP BY m.uf ORDER BY total_admissoes_tech DESC ";
            return jdbc.query(sql, (rs, rowNum) -> new RankingUfDTO(
                    rs.getString("uf"),
                    rs.getLong("total_admissoes_tech")
            ), cbo);
        } else {
            sql += " GROUP BY m.uf ORDER BY total_admissoes_tech DESC ";
            return jdbc.query(sql, (rs, rowNum) -> new RankingUfDTO(
                    rs.getString("uf"),
                    rs.getLong("total_admissoes_tech")
            ));
        }
    }

    /**
     * Retorna a lista de UFs únicas cadastradas nos municípios de forma ordenada.
     */
    public List<String> getUfs() {
        String sql = "SELECT DISTINCT uf FROM municipios WHERE uf IS NOT NULL AND uf != '' ORDER BY uf";
        return jdbc.queryForList(sql, String.class);
    }

    /**
     * Retorna a lista de cargos (CBOs) de TI únicos de forma ordenada.
     */
    public List<String> getCbos() {
        String sql = "SELECT DISTINCT titulo FROM cbo_ocupacoes WHERE area_tech = TRUE ORDER BY titulo";
        return jdbc.queryForList(sql, String.class);
    }

    /**
     * Recupera o período (data mínima e máxima de competência) presente no banco de dados.
     */
    public TemporalRangeDTO getTemporalRange() {
        String sql = "SELECT MIN(competencia) AS min_comp, MAX(competencia) AS max_comp FROM admissoes";
        return jdbc.queryForObject(sql, (rs, rowNum) -> {
            java.sql.Date minSql = rs.getDate("min_comp");
            java.sql.Date maxSql = rs.getDate("max_comp");
            return new TemporalRangeDTO(
                    minSql != null ? minSql.toLocalDate() : null,
                    maxSql != null ? maxSql.toLocalDate() : null
            );
        });
    }
}
