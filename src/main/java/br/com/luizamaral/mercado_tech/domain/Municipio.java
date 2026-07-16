package br.com.luizamaral.mercado_tech.domain;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "municipios")
public class Municipio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_ibge", nullable = false, unique = true, length = 7) 
    private String codigoIbge;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;
    
    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Column(name = "regiao", length = 20)
    private String regiao;
}