package br.com.luizamaral.mercado_tech.repository;

import br.com.luizamaral.mercado_tech.domain.EtlExecucao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtlExecucaoRepository extends JpaRepository<EtlExecucao, Long> {
}
