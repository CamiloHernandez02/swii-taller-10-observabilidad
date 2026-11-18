package com.rica.cqrs.evento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestigadorEliminadoEvent {
    private String investigadorId;
    private long timestamp;

    public InvestigadorEliminadoEvent(String investigadorId) {
        this.investigadorId = investigadorId;
        this.timestamp = System.currentTimeMillis();
    }
}
