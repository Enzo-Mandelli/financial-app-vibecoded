package com.pixelwallet.data;

/** Um lançamento do dia: gasto ou ganho extra (Pix recebido, freela...). */
public final class Registro {
    public final long id;
    public final long valorCentavos;
    /** Momento do registro em epoch millis. */
    public final long dataHora;
    /** true = dinheiro que entrou; false = gasto. */
    public final boolean ganho;

    public Registro(long id, long valorCentavos, long dataHora, boolean ganho) {
        this.id = id;
        this.valorCentavos = valorCentavos;
        this.dataHora = dataHora;
        this.ganho = ganho;
    }
}
