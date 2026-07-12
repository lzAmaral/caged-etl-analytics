package br.com.luizamaral.mercado_tech.repository;

import br.com.luizamaral.mercado_tech.domain.CboOcupacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CboOcupacaoRepository extends JpaRepository<CboOcupacao, Long> {
    Optional<CboOcupacao> findByCodigoCbo(String codigoCbo);
}
