# Guia de Finalização — Mercado Tech Brasil

Domain e Repository prontos ✅. Este guia cobre o resto, na ordem certa, explicando os conceitos (sem código pronto) pra você mesmo implementar. Segue o calendário: o que está aqui é essencialmente as Semanas 2 e 3.

---

## PARTE 1 — Camada Batch (o ETL em si)

Ordem de implementação: **Reader → Processor → Writer → Job → Listener**

### 1.1 `CagedCsvItemReader` (ou configuração de reader)

**O que faz:** lê o CSV linha por linha e transforma cada linha num objeto Java "cru" (um DTO intermediário, tipo `AdmissaoCagedDTO`, que representa exatamente os campos como vêm no arquivo — ainda sem tratamento).

**Conceitos que você vai precisar pesquisar:**
- `FlatFileItemReader<T>` — classe do Spring Batch feita pra ler arquivos delimitados (CSV, ponto e vírgula, etc.)
- `DelimitedLineTokenizer` — define qual o separador do seu CSV (no CAGED costuma ser `;`)
- `BeanWrapperFieldSetMapper<T>` — mapeia cada coluna do CSV pra um campo do seu DTO cru
- **Decisão importante:** crie um DTO separado (ex: `AdmissaoCagedDTO`) pra representar a linha crua do CSV — não tente ler direto pra entidade `Admissao`, porque a entidade espera objetos (`Municipio`, `CboOcupacao`) e o CSV só tem códigos soltos. Essa "tradução" é trabalho do Processor.

### 1.2 `AdmissaoItemProcessor`

**O que faz:** recebe o DTO cru (linha do CSV) e devolve uma entidade `Admissao` pronta pra salvar — ou `null`, se a linha for inválida (o Spring Batch entende `null` como "descarta esse item, não escreve".

**Conceitos:**
- Implementa a interface `ItemProcessor<AdmissaoCagedDTO, Admissao>`
- Aqui é onde você **busca** o `Municipio` e o `CboOcupacao` pelos códigos vindos do CSV (usando os métodos `findByCodigoIbge` e `findByCodigoCbo` que você já criou nos repositories)
- Aqui também é onde você trata dados sujos: valores nulos, formatos de data inconsistentes, salário negativo ou absurdo, código que não existe na sua tabela de referência
- **Decisão de design:** o que fazer quando o município ou CBO não é encontrado? Duas opções válidas: (a) descartar a linha (retorna `null`), ou (b) logar como rejeitada e seguir. Pra fins de portfólio, vale contar quantas linhas foram rejeitadas — é dado interessante pra colocar no README depois.

### 1.3 `AdmissaoItemWriter`

**O que faz:** recebe uma lista (chunk) de entidades `Admissao` já processadas e grava no banco.

**Conceitos:**
- Implementa `ItemWriter<Admissao>`
- Se você usa Spring Data JPA, o `JpaItemWriter<T>` já faz o trabalho pesado — você só configura ele apontando pro `EntityManagerFactory`, sem escrever lógica de INSERT manual

### 1.4 `AdmissoesJobConfig`

**O que faz:** amarra tudo — define o `Job` e o `Step` (ou múltiplos steps), especificando chunk size, reader, processor, writer.

**Conceitos que você precisa entender, não só copiar:**
- **Chunk size:** o Spring Batch não processa tudo de uma vez na memória — ele processa em lotes (chunks). Se você definir chunk de 100, ele lê 100 linhas, processa as 100, escreve as 100, e só então parte pro próximo lote. Isso é o que permite processar arquivos de vários GB sem estourar memória. Escolha um valor (100, 500, 1000) e pense no porquê.
- **`@Bean Job` e `@Bean Step`:** como você declara essas peças no Spring
- **Skip policy / fault tolerance:** o Spring Batch permite configurar "pule até N itens com erro sem derrubar o job inteiro" — pesquise `faultTolerant()` e `skipLimit()`. Isso é relevante porque dado público de governo *sempre* tem sujeira.

### 1.5 `JobCompletionListener`

**O que faz:** escuta o evento de job finalizado (sucesso ou falha) e grava um registro na tabela `etl_execucoes` — é aqui que os campos `iniciadoEm`, `finalizadoEm`, `linhasLidas`, `linhasProcessadas`, `linhasRejeitadas` e `status` que você já modelou na entidade `EtlExecucao` finalmente são preenchidos.

**Conceitos:**
- Implementa `JobExecutionListener` (métodos `beforeJob` e `afterJob`)
- Dentro de `afterJob`, você usa o `EtlExecucaoRepository` (opa — você ainda não criou esse repository! adicione ele junto com os outros, é o mesmo padrão) pra salvar o resultado

---

## PARTE 2 — Camada Service (análise/regras de negócio)

### 2.1 `AnalyticsService`

**O que faz:** consulta as views analíticas que você já criou no banco (`vw_saldo_mensal_regiao`, `vw_salario_medio_tech`, `vw_ranking_uf_tech`) e devolve os dados prontos pro Controller expor.

**Conceitos:**
- Duas abordagens possíveis: (a) mapear as views como `@Entity` "somente leitura" também (funciona, mas é meio contraintuitivo pra views), ou (b) usar `@Query(nativeQuery = true)` direto no repository, ou (c) usar `JdbcTemplate` pra rodar `SELECT * FROM vw_...` cru. Pra esse projeto, a opção (c) ou consultas nativas costumam ser mais simples de justificar numa entrevista ("view é uma agregação, não uma entidade de domínio").
- Os DTOs que você já esqueletizou (`SalaryTrendDTO`, `HiringByRegionDTO`, `RankingUfDTO`) são o formato de retorno desse service — pense neles como "o que a API vai devolver", não como espelho de tabela.

---

## PARTE 3 — Camada Controller (API REST)

### 3.1 `AnalyticsController`

**Endpoints sugeridos** (ajuste os nomes se quiser):
- `GET /analytics/salary-trends` → retorna lista de `SalaryTrendDTO`
- `GET /analytics/hiring-by-region` → retorna lista de `HiringByRegionDTO`
- `GET /analytics/ranking-uf` → retorna lista de `RankingUfDTO`

**Conceitos:**
- `@RestController`, `@RequestMapping("/analytics")`, `@GetMapping("/salary-trends")`
- Pense em parâmetros de filtro opcionais (`@RequestParam`) — tipo filtrar por ano ou por UF. Isso deixa a API mais rica e é fácil de mencionar em entrevista.

### 3.2 `EtlController` (opcional, mas legal)

Um endpoint tipo `POST /etl/run` que dispara o Job manualmente via `JobLauncher`. Isso evita você ter que reiniciar a aplicação toda vez que quiser rodar o pipeline de novo durante os testes — mais prático que ficar reiniciando o Spring Boot.

---

## PARTE 4 — Frontend (Semana 3)

Não precisa ser bonito. Sugestão mínima:
- Uma tela com 2-3 gráficos consumindo os 3 endpoints acima (Recharts se for React)
- Se quiser economizar tempo, um HTML+JS simples com Chart.js também resolve — o valor está nos dados, não no framework

---

## PARTE 5 — Documentação

### `docs/insights.md`
Anote aqui, conforme for descobrindo, os números reais que os dados mostraram. Ex: "admissões em ocupações de TI cresceram X% entre 2023 e 2025 na região Sudeste". Isso vira sua munição de entrevista.

### `docs/arquitetura.md`
Um diagrama simples (pode ser texto mesmo, tipo ASCII ou um desenho no Excalidraw) mostrando o fluxo: CSV → Batch (Reader/Processor/Writer) → Postgres → Service → API → Front. Documente também decisões técnicas: por que Spring Batch, por que chunk de tal tamanho, por que LAZY, etc.

### `README.md`
Estrutura sugerida: (1) o problema de negócio que o projeto resolve, (2) arquitetura resumida, (3) como rodar localmente (docker-compose + comandos), (4) principais achados da análise com números, (5) stack técnica.

---

## Ordem de execução recomendada (retomando o calendário)

1. Criar `EtlExecucaoRepository` (faltou, é rápido)
2. Reader → Processor → Writer → Job → Listener (nessa ordem, testando incrementalmente com poucos registros antes de rodar o CSV completo)
3. Rodar o pipeline completo, validar dados no banco
4. Service (consultas às views)
5. Controller (expor API)
6. Testar tudo com Postman/Insomnia
7. Front simples
8. Documentação (`insights.md`, `arquitetura.md`, `README.md`)

Vá me chamando conforme for avançando em cada etapa — sigo te explicando os conceitos sem entregar código pronto, do mesmo jeito que fizemos até aqui.
