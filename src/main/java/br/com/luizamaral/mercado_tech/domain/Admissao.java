package br.com.luizamaral.mercado_tech.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "admissoes")
public class Admissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id", nullable = false)
    private Municipio municipio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cbo_id", nullable = false)
    private CboOcupacao cboOcupacao;

    @Column(name = "competencia", nullable = false)
    private LocalDate competencia;

    @Column(name = "tipo_movimentacao", nullable = false, length = 20)
    private String tipoMovimentacao;

    @Column(name = "salario", precision = 10, scale = 2)
    private BigDecimal salario;

    @Column(name = "grau_instrucao", length = 50)
    private String grauInstrucao;

    @Column(name = "sexo", length = 1)
    private String sexo;

    @Column(name = "idade")
    private Short idade;

    @Column(name = "tipo_empregador", length = 50)
    private String tipoEmpregador;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

}
