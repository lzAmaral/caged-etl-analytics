package br.com.luizamaral.mercado_tech.config;

import br.com.luizamaral.mercado_tech.domain.CboOcupacao;
import br.com.luizamaral.mercado_tech.domain.Municipio;
import br.com.luizamaral.mercado_tech.repository.CboOcupacaoRepository;
import br.com.luizamaral.mercado_tech.repository.MunicipioRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final MunicipioRepository municipioRepository;
    private final CboOcupacaoRepository cboOcupacaoRepository;
    private final ObjectMapper objectMapper;

    public DataInitializer(MunicipioRepository municipioRepository,
                           CboOcupacaoRepository cboOcupacaoRepository,
                           ObjectMapper objectMapper) {
        this.municipioRepository = municipioRepository;
        this.cboOcupacaoRepository = cboOcupacaoRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        seedMunicipios();
        seedCboOcupacoes();
    }

    private void seedMunicipios() {
        if (municipioRepository.count() > 100) {
            log.info("Municípios já estão populados.");
            return;
        }

        log.info("Iniciando carga de municípios da API do IBGE...");
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://servicodados.ibge.gov.br/api/v1/localidades/municipios"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Falha ao obter municípios da API do IBGE: status {}", response.statusCode());
                return;
            }

            java.util.Set<String> existingCodes = new java.util.HashSet<>();
            municipioRepository.findAll().forEach(m -> existingCodes.add(m.getCodigoIbge()));

            JsonNode root = objectMapper.readTree(response.body());
            List<Municipio> municipios = new ArrayList<>();

            for (JsonNode node : root) {
                String codigoIbge = node.get("id").asText();
                if (existingCodes.contains(codigoIbge)) {
                    continue;
                }

                String nome = node.get("nome").asText();
                String uf = "";
                String regiao = "";

                JsonNode microrregiao = node.get("microrregiao");
                if (microrregiao != null) {
                    JsonNode mesorregiao = microrregiao.get("mesorregiao");
                    if (mesorregiao != null) {
                        JsonNode ufNode = mesorregiao.get("UF");
                        if (ufNode != null) {
                            uf = ufNode.get("sigla").asText();
                            JsonNode regiaoNode = ufNode.get("regiao");
                            if (regiaoNode != null) {
                                regiao = regiaoNode.get("nome").asText();
                            }
                        }
                    }
                }

                Municipio m = new Municipio();
                m.setCodigoIbge(codigoIbge);
                m.setNome(nome);
                m.setUf(uf);
                m.setRegiao(regiao.toUpperCase());
                municipios.add(m);
            }

            if (!municipios.isEmpty()) {
                log.info("Salvando {} novos municípios no banco de dados...", municipios.size());
                municipioRepository.saveAll(municipios);
            }
            log.info("Municípios carregados com sucesso.");

        } catch (Exception e) {
            log.error("Erro ao carregar municípios da API do IBGE: {}", e.getMessage(), e);
        }
    }

    private void seedCboOcupacoes() {
        if (cboOcupacaoRepository.count() > 50) {
            log.info("CBO Ocupações já estão populadas.");
            return;
        }

        log.info("Seeding CBOs de Tecnologia da Informação...");
        
        Map<String, String> techCbos = Map.ofEntries(
                Map.entry("142505", "Gerente de tecnologia da informação"),
                Map.entry("212205", "Engenheiro de aplicativos em computação"),
                Map.entry("212210", "Engenheiro de sistemas em computação"),
                Map.entry("212215", "Engenheiro de computação"),
                Map.entry("212305", "Administrador de banco de dados"),
                Map.entry("212310", "Administrador de redes e sistemas operacionais"),
                Map.entry("212315", "Administrador de sistemas operacionais"),
                Map.entry("212320", "Administrador em segurança da informação"),
                Map.entry("212405", "Analista de desenvolvimento de sistemas"),
                Map.entry("212410", "Analista de redes e de comunicação de dados"),
                Map.entry("212415", "Analista de sistemas de computador"),
                Map.entry("212420", "Gerente de projetos de tecnologia da informação"),
                Map.entry("212430", "Analista de suporte computacional"),
                Map.entry("317105", "Programador de internet"),
                Map.entry("317110", "Programador de sistemas de informação"),
                Map.entry("317120", "Programador de multimídia"),
                Map.entry("317205", "Operador de computador e de equipamentos de apoio"),
                Map.entry("317210", "Operador de suporte técnico")
        );

        java.util.Set<String> existingCbos = new java.util.HashSet<>();
        cboOcupacaoRepository.findAll().forEach(c -> existingCbos.add(c.getCodigoCbo()));

        List<CboOcupacao> cbos = new ArrayList<>();
        techCbos.forEach((codigo, titulo) -> {
            if (!existingCbos.contains(codigo)) {
                CboOcupacao cbo = new CboOcupacao();
                cbo.setCodigoCbo(codigo);
                cbo.setTitulo(titulo);
                cbo.setAreaTech(true);
                cbos.add(cbo);
            }
        });

        if (!cbos.isEmpty()) {
            cboOcupacaoRepository.saveAll(cbos);
        }
        log.info("CBOs de Tecnologia cadastradas com sucesso.");
    }
}
