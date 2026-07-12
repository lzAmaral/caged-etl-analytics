# Estrutura do Projeto — Mercado Tech Brasil (ETL + Análise)

Estrutura de referência. Os arquivos estão listados vazios/com comentário do que devem conter — a implementação é sua.

```
mercado-tech-brasil/
│
├── backend/
│   └── src/
│       ├── main/
│       │   ├── java/br/com/luizamaral/mercadotech/
│       │   │   │
│       │   │   ├── McTechApplication.java              # classe main do Spring Boot
│       │   │   │
│       │   │   ├── config/
│       │   │   │   ├── BatchConfig.java                # configuração geral do Spring Batch
│       │   │   │   └── DataSourceConfig.java           # configuração de conexão com o banco (se precisar customizar)
│       │   │   │
│       │   │   ├── batch/
│       │   │   │   ├── job/
│       │   │   │   │   └── AdmissoesJobConfig.java      # define o Job e os Steps do ETL
│       │   │   │   ├── reader/
│       │   │   │   │   └── CagedCsvItemReader.java      # lê o CSV de origem
│       │   │   │   ├── processor/
│       │   │   │   │   └── AdmissaoItemProcessor.java   # limpeza, validação e transformação dos dados
│       │   │   │   ├── writer/
│       │   │   │   │   └── AdmissaoItemWriter.java      # grava os dados processados no banco
│       │   │   │   └── listener/
│       │   │   │       └── JobCompletionListener.java   # loga métricas de execução (lidas/processadas/rejeitadas)
│       │   │   │
│       │   │   ├── domain/
│       │   │   │   ├── Admissao.java                    # entidade JPA
│       │   │   │   ├── Municipio.java                   # entidade JPA
│       │   │   │   ├── CboOcupacao.java                 # entidade JPA
│       │   │   │   └── EtlExecucao.java                 # entidade JPA (auditoria do ETL)
│       │   │   │
│       │   │   ├── repository/
│       │   │   │   ├── AdmissaoRepository.java          # Spring Data JPA
│       │   │   │   ├── MunicipioRepository.java         # Spring Data JPA
│       │   │   │   └── CboOcupacaoRepository.java       # Spring Data JPA
│       │   │   │
│       │   │   ├── service/
│       │   │   │   ├── AnalyticsService.java            # regras de negócio / lógica das agregações
│       │   │   │   └── dto/
│       │   │   │       ├── SalaryTrendDTO.java
│       │   │   │       ├── HiringByRegionDTO.java
│       │   │   │       └── RankingUfDTO.java
│       │   │   │
│       │   │   ├── controller/
│       │   │   │   ├── AnalyticsController.java         # endpoints /analytics/*
│       │   │   │   └── EtlController.java               # endpoint opcional pra disparar o job manualmente
│       │   │   │
│       │   │   └── exception/
│       │   │       ├── DataProcessingException.java
│       │   │       └── GlobalExceptionHandler.java
│       │   │
│       │   └── resources/
│       │       ├── application.yml                      # configuração geral (profiles)
│       │       ├── application-dev.yml                  # configuração do ambiente de desenvolvimento
│       │       ├── db/migration/
│       │       │   ├── V1__create_municipios.sql
│       │       │   ├── V2__create_cbo_ocupacoes.sql
│       │       │   ├── V3__create_admissoes.sql
│       │       │   ├── V4__create_etl_execucoes.sql
│       │       │   └── V5__create_views_analiticas.sql
│       │       └── data/
│       │           └── amostra_caged.csv                # amostra pequena pra testes locais
│       │
│       └── test/
│           └── java/br/com/luizamaral/mercadotech/
│               ├── batch/
│               │   └── AdmissaoItemProcessorTest.java   # testes do processor (limpeza/validação)
│               └── service/
│                   └── AnalyticsServiceTest.java         # testes das agregações
│
├── frontend/
│   └── (estrutura a definir na Semana 3 — React ou Thymeleaf)
│
├── docs/
│   ├── arquitetura.md                    # diagrama + decisões técnicas do projeto
│   └── insights.md                       # achados da análise, com números concretos
│
├── .github/
│   └── workflows/
│       └── ci.yml                        # build automático (opcional, mas bom diferencial)
│
├── docker-compose.yml                    # sobe o Postgres localmente com um comando
├── .gitignore
└── README.md                             # problema de negócio, arquitetura, como rodar, principais achados
```

## Ordem sugerida de implementação (acompanha o calendário das 3 semanas)

1. `db/migration/` (schema) → `domain/` (entidades) → `repository/`
2. `batch/reader` → `batch/processor` → `batch/writer` → `batch/job` → `batch/listener`
3. `service/` (análises) → `controller/` (API)
4. `frontend/` → `docs/` → `README.md`

Vá criando cada arquivo só quando chegar a hora de implementá-lo — não crie tudo de uma vez vazio, isso é só o mapa.
