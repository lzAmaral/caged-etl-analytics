package br.com.luizamaral.mercado_tech.controller;

import br.com.luizamaral.mercado_tech.service.AnalyticsService;
import br.com.luizamaral.mercado_tech.service.dto.HiringByRegionDTO;
import br.com.luizamaral.mercado_tech.service.dto.RankingUfDTO;
import br.com.luizamaral.mercado_tech.service.dto.SalaryTrendDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /analytics/salary-trends
     * Retorna salário médio por ocupação de TI e ano.
     */
    @GetMapping("/salary-trends")
    public ResponseEntity<List<SalaryTrendDTO>> getSalaryTrends() {
        return ResponseEntity.ok(analyticsService.getSalaryTrends());
    }

    /**
     * GET /analytics/hiring-by-region
     * Retorna saldo mensal de admissões vs desligamentos por região.
     */
    @GetMapping("/hiring-by-region")
    public ResponseEntity<List<HiringByRegionDTO>> getHiringByRegion() {
        return ResponseEntity.ok(analyticsService.getHiringByRegion());
    }

    /**
     * GET /analytics/ranking-uf
     * Retorna ranking de UFs por total de admissões em ocupações de TI.
     */
    @GetMapping("/ranking-uf")
    public ResponseEntity<List<RankingUfDTO>> getRankingUf() {
        return ResponseEntity.ok(analyticsService.getRankingUf());
    }
}
