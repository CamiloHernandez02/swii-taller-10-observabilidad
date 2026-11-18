package com.rica.publicacionesservice.compartido;

public class LimiteAnualExcedidoException extends RuntimeException {

    public LimiteAnualExcedidoException(String mensaje) {
        super(mensaje);
    }
}