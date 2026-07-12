package br.com.luizamaral.mercado_tech.batch.reader.dto;

/**
 * DTO intermediário representando uma linha crua do CSV do Novo CAGED.
 *
 * Todos os campos ficam como String aqui intencionalmente:
 * o CSV do governo traz dados inconsistentes (datas sem separador, salário com vírgula,
 * campos vazios onde não deveria) — converter no Processor permite tratar cada falha
 * individualmente e decidir se descarta ou loga a linha, sem explodir o job inteiro.
 *
 * Os nomes dos campos devem corresponder EXATAMENTE ao cabeçalho do CSV
 * para que o BeanWrapperFieldSetMapper consiga mapear automaticamente.
 */
public class AdmissaoCagedDTO {

    private String codigoMunicipio;
    private String codigoCbo;
    private String competencia;
    private String tipoMovimentacao;
    private String salario;
    private String grauInstrucao;
    private String sexo;
    private String idade;
    private String tipoEmpregador;

    public AdmissaoCagedDTO() {}

    public String getCodigoMunicipio() { return codigoMunicipio; }
    public void setCodigoMunicipio(String codigoMunicipio) { this.codigoMunicipio = codigoMunicipio; }

    public String getCodigoCbo() { return codigoCbo; }
    public void setCodigoCbo(String codigoCbo) { this.codigoCbo = codigoCbo; }

    public String getCompetencia() { return competencia; }
    public void setCompetencia(String competencia) { this.competencia = competencia; }

    public String getTipoMovimentacao() { return tipoMovimentacao; }
    public void setTipoMovimentacao(String tipoMovimentacao) { this.tipoMovimentacao = tipoMovimentacao; }

    public String getSalario() { return salario; }
    public void setSalario(String salario) { this.salario = salario; }

    public String getGrauInstrucao() { return grauInstrucao; }
    public void setGrauInstrucao(String grauInstrucao) { this.grauInstrucao = grauInstrucao; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getIdade() { return idade; }
    public void setIdade(String idade) { this.idade = idade; }

    public String getTipoEmpregador() { return tipoEmpregador; }
    public void setTipoEmpregador(String tipoEmpregador) { this.tipoEmpregador = tipoEmpregador; }
}
