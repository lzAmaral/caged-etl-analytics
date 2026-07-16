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
    private BigDecimal salarioJunior;
    private BigDecimal salarioPleno;
    private BigDecimal salarioSenior;
    private Long totalRegistros;

    public SalaryTrendDTO() {}

    public SalaryTrendDTO(String ocupacao, Integer ano, BigDecimal salarioMedio,
                          BigDecimal salarioJunior, BigDecimal salarioPleno, BigDecimal salarioSenior,
                          Long totalRegistros) {
        this.ocupacao = ocupacao;
        this.ano = ano;
        this.salarioMedio = salarioMedio;
        this.salarioJunior = salarioJunior;
        this.salarioPleno = salarioPleno;
        this.salarioSenior = salarioSenior;
        this.totalRegistros = totalRegistros;
    }

    public String getOcupacao() { return ocupacao; }
    public void setOcupacao(String ocupacao) { this.ocupacao = ocupacao; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public BigDecimal getSalarioMedio() { return salarioMedio; }
    public void setSalarioMedio(BigDecimal salarioMedio) { this.salarioMedio = salarioMedio; }

    public BigDecimal getSalarioJunior() { return salarioJunior; }
    public void setSalarioJunior(BigDecimal salarioJunior) { this.salarioJunior = salarioJunior; }

    public BigDecimal getSalarioPleno() { return salarioPleno; }
    public void setSalarioPleno(BigDecimal salarioPleno) { this.salarioPleno = salarioPleno; }

    public BigDecimal getSalarioSenior() { return salarioSenior; }
    public void setSalarioSenior(BigDecimal salarioSenior) { this.salarioSenior = salarioSenior; }

    public Long getTotalRegistros() { return totalRegistros; }
    public void setTotalRegistros(Long totalRegistros) { this.totalRegistros = totalRegistros; }
}
