package br.com.luizamaral.mercado_tech.controller;

import br.com.luizamaral.mercado_tech.service.AnalyticsService;
import br.com.luizamaral.mercado_tech.service.dto.HiringByRegionDTO;
import br.com.luizamaral.mercado_tech.service.dto.RankingUfDTO;
import br.com.luizamaral.mercado_tech.service.dto.SalaryTrendDTO;
import br.com.luizamaral.mercado_tech.service.dto.TemporalRangeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /analytics/salary-trends
     * Retorna salário médio por ocupação de TI e ano. Filtro opcional por UF.
     */
    @GetMapping("/salary-trends")
    public ResponseEntity<List<SalaryTrendDTO>> getSalaryTrends(@RequestParam(required = false) String uf) {
        return ResponseEntity.ok(analyticsService.getSalaryTrends(uf));
    }

    /**
     * GET /analytics/hiring-by-region
     * Retorna saldo mensal de admissões vs desligamentos. Filtros opcionais por UF e Cargo.
     */
    @GetMapping("/hiring-by-region")
    public ResponseEntity<List<HiringByRegionDTO>> getHiringByRegion(
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) String cbo) {
        return ResponseEntity.ok(analyticsService.getHiringByRegion(uf, cbo));
    }

    /**
     * GET /analytics/ranking-uf
     * Retorna ranking de UFs por total de admissões. Filtro opcional por Cargo.
     */
    @GetMapping("/ranking-uf")
    public ResponseEntity<List<RankingUfDTO>> getRankingUf(@RequestParam(required = false) String cbo) {
        return ResponseEntity.ok(analyticsService.getRankingUf(cbo));
    }

    /**
     * GET /analytics/ufs
     * Retorna lista de UFs disponíveis para filtro.
     */
    @GetMapping("/ufs")
    public ResponseEntity<List<String>> getUfs() {
        return ResponseEntity.ok(analyticsService.getUfs());
    }

    /**
     * GET /analytics/cbos
     * Retorna lista de Ocupações (cargos de TI) disponíveis para filtro.
     */
    @GetMapping("/cbos")
    public ResponseEntity<List<String>> getCbos() {
        return ResponseEntity.ok(analyticsService.getCbos());
    }

    /**
     * GET /analytics/temporal-range
     * Retorna a data mínima e máxima presentes no banco de dados.
     */
    @GetMapping("/temporal-range")
    public ResponseEntity<TemporalRangeDTO> getTemporalRange() {
        return ResponseEntity.ok(analyticsService.getTemporalRange());
    }
}
