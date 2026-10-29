package com.pixelwallet.data;

public final class Gasto {
    public final long id;
    public final long valorCentavos;
    /** Momento do registro em epoch millis. */
    public final long dataHora;

    public Gasto(long id, long valorCentavos, long dataHora) {
        this.id = id;
        this.valorCentavos = valorCentavos;
        this.dataHora = dataHora;
    }
}
