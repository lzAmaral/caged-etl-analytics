package br.com.luizamaral.mercado_tech.batch.processor;

import br.com.luizamaral.mercado_tech.batch.reader.dto.AdmissaoCagedDTO;
import br.com.luizamaral.mercado_tech.domain.Admissao;
import br.com.luizamaral.mercado_tech.domain.CboOcupacao;
import br.com.luizamaral.mercado_tech.domain.Municipio;
import br.com.luizamaral.mercado_tech.repository.CboOcupacaoRepository;
import br.com.luizamaral.mercado_tech.repository.MunicipioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Component
public class AdmissaoItemProcessor implements ItemProcessor<AdmissaoCagedDTO, Admissao> {

    private static final Logger log = LoggerFactory.getLogger(AdmissaoItemProcessor.class);

    // Formato padrão do CAGED: competência vem como "YYYYMM" (ex: "202401")
    // e é convertida para o primeiro dia do mês para ter um LocalDate válido.
    private static final DateTimeFormatter COMPETENCIA_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final MunicipioRepository municipioRepository;
    private final CboOcupacaoRepository cboOcupacaoRepository;

    // Injeção via construtor — preferível a @Autowired em campo:
    // deixa as dependências explícitas e facilita testes unitários.
    public AdmissaoItemProcessor(MunicipioRepository municipioRepository,
                                  CboOcupacaoRepository cboOcupacaoRepository) {
        this.municipioRepository = municipioRepository;
        this.cboOcupacaoRepository = cboOcupacaoRepository;
    }

    @Override
    public Admissao process(AdmissaoCagedDTO item) throws Exception {

        // --- Validação de campos obrigatórios ---
        if (isBlank(item.getCodigoMunicipio()) || isBlank(item.getCodigoCbo())
                || isBlank(item.getCompetencia()) || isBlank(item.getTipoMovimentacao())) {
            log.warn("Linha rejeitada — campo obrigatório ausente: municipio={}, cbo={}, competencia={}, tipo={}",
                    item.getCodigoMunicipio(), item.getCodigoCbo(),
                    item.getCompetencia(), item.getTipoMovimentacao());
            // Retornar null instrui o Spring Batch a descartar o item sem escrevê-lo.
            // O skip counter do step contabiliza esse descarte como "skipped".
            return null;
        }

        // --- Lookup de Municipio ---
        // Usamos o codigoIbge como chave natural porque o CSV não traz o PK interno.
        // Se o município não estiver na tabela de referência, a linha é inválida — código
        // desconhecido pode indicar arquivo de período diferente ou dado corrompido.
        Optional<Municipio> municipioOpt = municipioRepository.findByCodigoIbge(
                item.getCodigoMunicipio().trim());
        if (municipioOpt.isEmpty()) {
            log.warn("Linha rejeitada — município não encontrado: codigoIbge={}", item.getCodigoMunicipio());
            return null;
        }

        // --- Lookup de CboOcupacao ---
        Optional<CboOcupacao> cboOpt = cboOcupacaoRepository.findByCodigoCbo(
                item.getCodigoCbo().trim());
        if (cboOpt.isEmpty()) {
            log.warn("Linha rejeitada — CBO não encontrado: codigoCbo={}", item.getCodigoCbo());
            return null;
        }

        // --- Conversão de tipos ---
        LocalDate competencia = parseCompetencia(item.getCompetencia());
        if (competencia == null) {
            log.warn("Linha rejeitada — competência inválida: '{}'", item.getCompetencia());
            return null;
        }

        BigDecimal salario = parseSalario(item.getSalario());
        // Salário é nullable na tabela; apenas logamos se vier com formato estranho.
        // Não rejeitamos a linha por salário ausente — é um campo analítico, não chave.

        Short idade = parseIdade(item.getIdade());

        // --- Montagem da entidade ---
        Admissao admissao = new Admissao();
        admissao.setMunicipio(municipioOpt.get());
        admissao.setCboOcupacao(cboOpt.get());
        admissao.setCompetencia(competencia);
        admissao.setTipoMovimentacao(item.getTipoMovimentacao().trim().toUpperCase());
        admissao.setSalario(salario);
        admissao.setGrauInstrucao(item.getGrauInstrucao() != null ? item.getGrauInstrucao().trim() : null);
        admissao.setSexo(item.getSexo() != null ? item.getSexo().trim() : null);
        admissao.setIdade(idade);
        admissao.setTipoEmpregador(item.getTipoEmpregador() != null ? item.getTipoEmpregador().trim() : null);

        return admissao;
    }

    // CAGED usa formato YYYYMM sem separador; convertemos para o 1º dia do mês.
    private LocalDate parseCompetencia(String value) {
        if (isBlank(value)) return null;
        try {
            return LocalDate.parse(value.trim(), COMPETENCIA_FORMATTER).withDayOfMonth(1);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // Salário pode vir com vírgula decimal (padrão BR) ou ponto — normalizamos.
    private BigDecimal parseSalario(String value) {
        if (isBlank(value)) return null;
        try {
            return new BigDecimal(value.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            log.debug("Salário não parseável: '{}' — será gravado como null", value);
            return null;
        }
    }

    private Short parseIdade(String value) {
        if (isBlank(value)) return null;
        try {
            return Short.parseShort(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
