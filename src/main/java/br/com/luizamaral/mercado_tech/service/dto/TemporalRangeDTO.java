package br.com.luizamaral.mercado_tech.service.dto;

import java.time.LocalDate;

/**
 * DTO para expor o período temporal (mês/ano de início e fim) dos dados do banco.
 */
public class TemporalRangeDTO {

    private LocalDate minCompetencia;
    private LocalDate maxCompetencia;

    public TemporalRangeDTO() {}

    public TemporalRangeDTO(LocalDate minCompetencia, LocalDate maxCompetencia) {
        this.minCompetencia = minCompetencia;
        this.maxCompetencia = maxCompetencia;
    }

    public LocalDate getMinCompetencia() {
        return minCompetencia;
    }

    public void setMinCompetencia(LocalDate minCompetencia) {
        this.minCompetencia = minCompetencia;
    }

    public LocalDate getMaxCompetencia() {
        return maxCompetencia;
    }

    public void setMaxCompetencia(LocalDate maxCompetencia) {
        this.maxCompetencia = maxCompetencia;
    }
}
