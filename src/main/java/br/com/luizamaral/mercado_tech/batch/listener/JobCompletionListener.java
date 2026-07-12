package br.com.luizamaral.mercado_tech.batch.listener;

import br.com.luizamaral.mercado_tech.domain.EtlExecucao;
import br.com.luizamaral.mercado_tech.repository.EtlExecucaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;

@Component
public class JobCompletionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionListener.class);

    private final EtlExecucaoRepository etlExecucaoRepository;

    // Guardamos o ID do registro criado no beforeJob para atualizá-lo no afterJob.
    private Long execucaoId;

    public JobCompletionListener(EtlExecucaoRepository etlExecucaoRepository) {
        this.etlExecucaoRepository = etlExecucaoRepository;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Criamos o registro antes do job começar — assim, se o processo morrer no meio,
        // ainda temos o iniciadoEm gravado e status "EM_ANDAMENTO" visível para diagnóstico.
        EtlExecucao execucao = new EtlExecucao();
        execucao.setJobName(jobExecution.getJobInstance().getJobName());
        execucao.setIniciadoEm(LocalDateTime.now());
        execucao.setStatus("EM_ANDAMENTO");

        EtlExecucao saved = etlExecucaoRepository.save(execucao);
        execucaoId = saved.getId();
        log.info("ETL iniciado — execucaoId={}, job={}", execucaoId, execucao.getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (execucaoId == null) {
            log.warn("execucaoId nulo no afterJob — registro de auditoria não encontrado.");
            return;
        }

        etlExecucaoRepository.findById(execucaoId).ifPresent(execucao -> {
            // Agrega contadores de todos os steps (funciona mesmo com múltiplos steps no futuro).
            // Em Spring Batch 6 os contadores são long — convertemos para Integer para o campo da entidade.
            Collection<StepExecution> steps = jobExecution.getStepExecutions();
            int linhasLidas       = (int) steps.stream().mapToLong(StepExecution::getReadCount).sum();
            int linhasProcessadas  = (int) steps.stream().mapToLong(StepExecution::getWriteCount).sum();
            // filterCount = items rejeitados pelo processor (return null)
            // skipCount   = items com Exception pulados pelo faultTolerant
            int linhasRejeitadas  = (int) steps.stream()
                    .mapToLong(s -> s.getSkipCount() + s.getFilterCount())
                    .sum();

            execucao.setLinhasLidas(linhasLidas);
            execucao.setLinhasProcessadas(linhasProcessadas);
            execucao.setLinhasRejeitadas(linhasRejeitadas);
            execucao.setFinalizadoEm(LocalDateTime.now());

            String status = jobExecution.getStatus() == BatchStatus.COMPLETED ? "SUCESSO" : "FALHA";
            execucao.setStatus(status);

            etlExecucaoRepository.save(execucao);
            log.info("ETL finalizado — status={}, lidas={}, processadas={}, rejeitadas={}",
                    status, linhasLidas, linhasProcessadas, linhasRejeitadas);
        });
    }
}
