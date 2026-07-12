package br.com.luizamaral.mercado_tech.batch.job;

import br.com.luizamaral.mercado_tech.batch.listener.JobCompletionListener;
import br.com.luizamaral.mercado_tech.batch.processor.AdmissaoItemProcessor;
import br.com.luizamaral.mercado_tech.batch.reader.dto.AdmissaoCagedDTO;
import br.com.luizamaral.mercado_tech.domain.Admissao;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AdmissoesJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final AdmissaoItemProcessor processor;
    private final JobCompletionListener listener;

    // Caminho do CSV injetado via application.yml (caged.csv.path=...)
    // Isso evita hardcode e permite mudar o arquivo sem recompilar.
    @Value("${caged.csv.path:src/main/resources/data/amostra_caged.csv}")
    private String csvPath;

    public AdmissoesJobConfig(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               EntityManagerFactory entityManagerFactory,
                               AdmissaoItemProcessor processor,
                               JobCompletionListener listener) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.entityManagerFactory = entityManagerFactory;
        this.processor = processor;
        this.listener = listener;
    }

    @Bean
    public ItemReader<AdmissaoCagedDTO> reader() {
        // BeanWrapperFieldSetMapper mapeia colunas do CSV para campos do DTO via reflexão.
        // Os nomes em .names() devem coincidir com os setters do AdmissaoCagedDTO.
        // Separador ";" é o padrão do Novo CAGED.
        BeanWrapperFieldSetMapper<AdmissaoCagedDTO> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(AdmissaoCagedDTO.class);

        return new FlatFileItemReaderBuilder<AdmissaoCagedDTO>()
                .name("cagedCsvItemReader")
                .resource(new FileSystemResource(csvPath))
                .linesToSkip(1) // pula o cabeçalho
                .delimited()
                .delimiter(";")
                // Ajuste esses nomes para corresponder ao cabeçalho exato do seu CSV:
                .names("codigoMunicipio", "codigoCbo", "competencia", "tipoMovimentacao",
                       "salario", "grauInstrucao", "sexo", "idade", "tipoEmpregador")
                .fieldSetMapper(fieldSetMapper)
                .build();
    }

    @Bean
    public ItemWriter<Admissao> writer() {
        // JpaItemWriter chama entityManager.merge() em cada item do chunk.
        // O Hibernate acumula os INSERTs dentro do chunk e os executa em batch antes do commit.
        return new JpaItemWriterBuilder<Admissao>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step admissoesStep() {
        return new StepBuilder("admissoesStep", jobRepository)
                // Chunk size 500: equilibra memória (não explodir o heap com
                // arquivos de milhões de linhas) e performance (não fazer um
                // commit por linha, que seria lentíssimo no Postgres).
                .<AdmissaoCagedDTO, Admissao>chunk(500, transactionManager)
                .reader(reader())
                .processor((ItemProcessor<? super AdmissaoCagedDTO, ? extends Admissao>) processor)
                .writer(writer())
                // faultTolerant: o job não morre se uma linha explodir.
                // skipLimit(100): tolera até 100 itens com erro antes de considerar o job falho.
                // Dados públicos de governo quase sempre têm sujeira — sem isso
                // um único registro mal-formado derrubaria o pipeline inteiro.
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public Job admissoesJob() {
        return new JobBuilder("admissoesJob", jobRepository)
                .start(admissoesStep())
                .listener(listener)
                .build();
    }
}
