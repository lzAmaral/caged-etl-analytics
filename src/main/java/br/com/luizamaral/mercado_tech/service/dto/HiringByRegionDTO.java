package br.com.luizamaral.mercado_tech.service.dto;

import java.time.LocalDate;

/**
 * Representa uma linha da view vw_saldo_mensal_regiao:
 * saldo mensal de admissões vs desligamentos por região.
 */
public class HiringByRegionDTO {

    private String regiao;
    private LocalDate competencia;
    private Long totalAdmissoes;
    private Long totalDesligamentos;
    private Long saldo;

    public HiringByRegionDTO() {}

    public HiringByRegionDTO(String regiao, LocalDate competencia,
                              Long totalAdmissoes, Long totalDesligamentos, Long saldo) {
        this.regiao = regiao;
        this.competencia = competencia;
        this.totalAdmissoes = totalAdmissoes;
        this.totalDesligamentos = totalDesligamentos;
        this.saldo = saldo;
    }

    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }

    public LocalDate getCompetencia() { return competencia; }
    public void setCompetencia(LocalDate competencia) { this.competencia = competencia; }

    public Long getTotalAdmissoes() { return totalAdmissoes; }
    public void setTotalAdmissoes(Long totalAdmissoes) { this.totalAdmissoes = totalAdmissoes; }

    public Long getTotalDesligamentos() { return totalDesligamentos; }
    public void setTotalDesligamentos(Long totalDesligamentos) { this.totalDesligamentos = totalDesligamentos; }

    public Long getSaldo() { return saldo; }
    public void setSaldo(Long saldo) { this.saldo = saldo; }
}
