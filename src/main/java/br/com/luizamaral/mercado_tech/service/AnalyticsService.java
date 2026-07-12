package br.com.luizamaral.mercado_tech.service;

import br.com.luizamaral.mercado_tech.service.dto.HiringByRegionDTO;
import br.com.luizamaral.mercado_tech.service.dto.RankingUfDTO;
import br.com.luizamaral.mercado_tech.service.dto.SalaryTrendDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
     * Consulta vw_salario_medio_tech: salário médio por ocupação de TI e ano.
     */
    public List<SalaryTrendDTO> getSalaryTrends() {
        String sql = "SELECT ocupacao, ano, salario_medio, total_registros FROM vw_salario_medio_tech";
        return jdbc.query(sql, (rs, rowNum) -> new SalaryTrendDTO(
                rs.getString("ocupacao"),
                rs.getInt("ano"),
                rs.getBigDecimal("salario_medio"),
                rs.getLong("total_registros")
        ));
    }

    /**
     * Consulta vw_saldo_mensal_regiao: saldo admissões vs desligamentos por região e mês.
     */
    public List<HiringByRegionDTO> getHiringByRegion() {
        String sql = """
                SELECT regiao, competencia, total_admissoes, total_desligamentos, saldo
                FROM vw_saldo_mensal_regiao
                """;
        return jdbc.query(sql, (rs, rowNum) -> new HiringByRegionDTO(
                rs.getString("regiao"),
                rs.getObject("competencia", LocalDate.class),
                rs.getLong("total_admissoes"),
                rs.getLong("total_desligamentos"),
                rs.getLong("saldo")
        ));
    }

    /**
     * Consulta vw_ranking_uf_tech: ranking de UFs por total de admissões em TI.
     */
    public List<RankingUfDTO> getRankingUf() {
        String sql = "SELECT uf, total_admissoes_tech FROM vw_ranking_uf_tech";
        return jdbc.query(sql, (rs, rowNum) -> new RankingUfDTO(
                rs.getString("uf"),
                rs.getLong("total_admissoes_tech")
        ));
    }
}
