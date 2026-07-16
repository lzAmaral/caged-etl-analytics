package br.com.luizamaral.mercado_tech.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.luizamaral.mercado_tech.repository.EtlExecucaoRepository;
import br.com.luizamaral.mercado_tech.domain.EtlExecucao;

@RestController
@RequestMapping("/etl")
public class EtlController {

    private static final Logger log = LoggerFactory.getLogger(EtlController.class);

    private final JobLauncher jobLauncher;
    private final Job admissoesJob;
    private final EtlExecucaoRepository etlExecucaoRepository;

    public EtlController(JobLauncher jobLauncher,
                         @Qualifier("admissoesJob") Job admissoesJob,
                         EtlExecucaoRepository etlExecucaoRepository) {
        this.jobLauncher = jobLauncher;
        this.admissoesJob = admissoesJob;
        this.etlExecucaoRepository = etlExecucaoRepository;
    }

    /**
     * GET /etl/status
     * Retorna informações da última execução do pipeline.
     */
    @GetMapping("/status")
    public ResponseEntity<EtlExecucao> getStatus() {
        return etlExecucaoRepository.findTopByOrderByIdDesc()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * POST /etl/run
     * Dispara o pipeline de ETL manualmente.
     *
     * O timestamp no JobParameters garante unicidade de cada execução —
     * sem ele, o Spring Batch recusaria uma segunda execução com os mesmos
     * parâmetros (comportamento de idempotência do framework).
     */
    @PostMapping("/run")
    public ResponseEntity<String> run() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            var execution = jobLauncher.run(admissoesJob, params);
            String status = execution.getStatus().toString();
            log.info("Job disparado via API — status inicial: {}", status);
            return ResponseEntity.accepted().body("Job iniciado com status: " + status);

        } catch (Exception e) {
            log.error("Erro ao disparar o job: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Erro ao iniciar job: " + e.getMessage());
        }
    }
}
