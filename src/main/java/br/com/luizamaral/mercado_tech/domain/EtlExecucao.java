package br.com.luizamaral.mercado_tech.domain;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "etl_execucoes")
public class EtlExecucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    @Column(name = "arquivo_origem", length = 255)
    private String arquivoOrigem;

    @Column(name = "linhas_lidas")
    private Integer linhasLidas;

    @Column(name = "linhas_processadas")
    private Integer linhasProcessadas;

    @Column(name = "linhas_rejeitadas")
    private Integer linhasRejeitadas;

    @Column(name = "iniciado_em", nullable = false)
    private LocalDateTime iniciadoEm;

    @Column(name = "finalizado_em")
    private LocalDateTime finalizadoEm;

    @Column(name = "status", length = 20)
    private String status;

    public EtlExecucao() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }

    public String getArquivoOrigem() { return arquivoOrigem; }
    public void setArquivoOrigem(String arquivoOrigem) { this.arquivoOrigem = arquivoOrigem; }

    public Integer getLinhasLidas() { return linhasLidas; }
    public void setLinhasLidas(Integer linhasLidas) { this.linhasLidas = linhasLidas; }

    public Integer getLinhasProcessadas() { return linhasProcessadas; }
    public void setLinhasProcessadas(Integer linhasProcessadas) { this.linhasProcessadas = linhasProcessadas; }

    public Integer getLinhasRejeitadas() { return linhasRejeitadas; }
    public void setLinhasRejeitadas(Integer linhasRejeitadas) { this.linhasRejeitadas = linhasRejeitadas; }

    public LocalDateTime getIniciadoEm() { return iniciadoEm; }
    public void setIniciadoEm(LocalDateTime iniciadoEm) { this.iniciadoEm = iniciadoEm; }

    public LocalDateTime getFinalizadoEm() { return finalizadoEm; }
    public void setFinalizadoEm(LocalDateTime finalizadoEm) { this.finalizadoEm = finalizadoEm; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
