package br.com.luizamaral.mercado_tech.domain;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cbo_ocupacoes")
public class CboOcupacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "codigo_cbo", nullable = false, unique = true, length = 7)
    private String codigoCbo;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "area_tech")
    private Boolean areaTech;

}
