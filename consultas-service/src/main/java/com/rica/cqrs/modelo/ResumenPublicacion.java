package com.rica.cqrs.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenPublicacion {
    private String id;
    private String titulo;
    private String doi;
    private int año;
}
