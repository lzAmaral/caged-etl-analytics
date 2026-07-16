package br.com.luizamaral.mercado_tech.repository;

import br.com.luizamaral.mercado_tech.domain.EtlExecucao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EtlExecucaoRepository extends JpaRepository<EtlExecucao, Long> {
    Optional<EtlExecucao> findTopByOrderByIdDesc();
}
