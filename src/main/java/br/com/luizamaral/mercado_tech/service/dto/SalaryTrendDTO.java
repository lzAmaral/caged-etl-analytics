package br.com.luizamaral.mercado_tech.service.dto;

import java.math.BigDecimal;

/**
 * Representa uma linha da view vw_salario_medio_tech:
 * salário médio por ocupação de TI e ano.
 */
public class SalaryTrendDTO {

    private String ocupacao;
    private Integer ano;
    private BigDecimal salarioMedio;
    private Long totalRegistros;

    public SalaryTrendDTO() {}

    public SalaryTrendDTO(String ocupacao, Integer ano, BigDecimal salarioMedio, Long totalRegistros) {
        this.ocupacao = ocupacao;
        this.ano = ano;
        this.salarioMedio = salarioMedio;
        this.totalRegistros = totalRegistros;
    }

    public String getOcupacao() { return ocupacao; }
    public void setOcupacao(String ocupacao) { this.ocupacao = ocupacao; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public BigDecimal getSalarioMedio() { return salarioMedio; }
    public void setSalarioMedio(BigDecimal salarioMedio) { this.salarioMedio = salarioMedio; }

    public Long getTotalRegistros() { return totalRegistros; }
    public void setTotalRegistros(Long totalRegistros) { this.totalRegistros = totalRegistros; }
}
