package com.rica.cqrs.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fichas_investigadores")
public class FichaInvestigador {
    @Id
    private String id;
    private String nombre;
    private String email;
    private List<ResumenPublicacion> publicaciones = new ArrayList<>();

    public FichaInvestigador(String id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.publicaciones = new ArrayList<>();
    }

    public synchronized void registrarPublicacion(ResumenPublicacion pub) {
        if (pub == null || pub.getId() == null) return;
        boolean existe = publicaciones.stream()
                .anyMatch(p -> p.getId().equals(pub.getId()));
        if (!existe) {
            publicaciones.add(pub);
        }
    }

    public synchronized void eliminarPublicaciones() {
        publicaciones.clear();
    }
}
