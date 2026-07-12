package br.com.luizamaral.mercado_tech.repository;

import br.com.luizamaral.mercado_tech.domain.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MunicipioRepository extends JpaRepository<Municipio, Long> {
    Optional<Municipio> findByCodigoIbge(String codigoIbge);
}
