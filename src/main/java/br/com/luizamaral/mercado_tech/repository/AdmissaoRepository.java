package br.com.luizamaral.mercado_tech.repository;

import br.com.luizamaral.mercado_tech.domain.Admissao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdmissaoRepository extends JpaRepository<Admissao, Long> {
}
