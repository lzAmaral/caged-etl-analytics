package br.com.luizamaral.mercado_tech.service.dto;

/**
 * Representa uma linha da view vw_ranking_uf_tech:
 * total de admissões em ocupações de TI por UF, ordenado decrescente.
 */
public class RankingUfDTO {

    private String uf;
    private Long totalAdmissoesTech;

    public RankingUfDTO() {}

    public RankingUfDTO(String uf, Long totalAdmissoesTech) {
        this.uf = uf;
        this.totalAdmissoesTech = totalAdmissoesTech;
    }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public Long getTotalAdmissoesTech() { return totalAdmissoesTech; }
    public void setTotalAdmissoesTech(Long totalAdmissoesTech) { this.totalAdmissoesTech = totalAdmissoesTech; }
}
