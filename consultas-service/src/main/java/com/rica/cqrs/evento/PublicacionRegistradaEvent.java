package com.rica.cqrs.evento;

import com.rica.cqrs.modelo.ResumenPublicacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicacionRegistradaEvent {
    private String investigadorId;
    private ResumenPublicacion publicacion;
    private long timestamp;

    public PublicacionRegistradaEvent(String investigadorId, ResumenPublicacion publicacion) {
        this.investigadorId = investigadorId;
        this.publicacion = publicacion;
        this.timestamp = System.currentTimeMillis();
    }
}
