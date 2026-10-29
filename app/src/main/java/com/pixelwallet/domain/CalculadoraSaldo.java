package com.pixelwallet.domain;

/**
 * Calcula o saldo do dia no modo ACUMULADO (modo B).
 *
 * <pre>
 * sobra do mês     = renda − dívidas fixas
 * liberado até hoje = sobra do mês × dia de hoje ÷ dias do mês
 * saldo de hoje     = liberado até hoje − total gasto no mês
 * </pre>
 *
 * Economizou ontem? Sobra pra hoje. Estourou ontem? Hoje tem menos.
 * Se o saldo nunca ficar negativo, o mês fecha no positivo.
 *
 * Todos os valores são em centavos. Divisões arredondam para baixo
 * (sempre a favor de sobrar dinheiro).
 */
public final class CalculadoraSaldo {

    private CalculadoraSaldo() {
    }

    public static Resultado calcular(long rendaCentavos,
                                     long fixasCentavos,
                                     int diasNoMes,
                                     int diaDeHoje,
                                     long gastoNoMesCentavos,
                                     long gastoHojeCentavos) {
        if (diasNoMes < 1) {
            throw new IllegalArgumentException("diasNoMes deve ser >= 1");
        }
        if (diaDeHoje < 1 || diaDeHoje > diasNoMes) {
            throw new IllegalArgumentException("diaDeHoje fora do mês");
        }

        long sobraDoMes = rendaCentavos - fixasCentavos;
        long diaria = Math.floorDiv(sobraDoMes, diasNoMes);
        // Multiplica antes de dividir para não acumular erro de arredondamento:
        // no último dia o liberado é exatamente a sobra do mês.
        long liberadoAteHoje = Math.floorDiv(sobraDoMes * diaDeHoje, diasNoMes);
        long saldoHoje = liberadoAteHoje - gastoNoMesCentavos;

        return new Resultado(sobraDoMes, diaria, liberadoAteHoje, saldoHoje,
                gastoNoMesCentavos, gastoHojeCentavos, diaDeHoje, diasNoMes);
    }

    public static final class Resultado {
        public final long sobraDoMes;
        public final long diaria;
        public final long liberadoAteHoje;
        public final long saldoHoje;
        public final long gastoNoMes;
        public final long gastoHoje;
        public final int diaDeHoje;
        public final int diasNoMes;

        Resultado(long sobraDoMes, long diaria, long liberadoAteHoje, long saldoHoje,
                  long gastoNoMes, long gastoHoje, int diaDeHoje, int diasNoMes) {
            this.sobraDoMes = sobraDoMes;
            this.diaria = diaria;
            this.liberadoAteHoje = liberadoAteHoje;
            this.saldoHoje = saldoHoje;
            this.gastoNoMes = gastoNoMes;
            this.gastoHoje = gastoHoje;
            this.diaDeHoje = diaDeHoje;
            this.diasNoMes = diasNoMes;
        }

        public boolean negativo() {
            return saldoHoje < 0;
        }

        /** Renda menor que as dívidas fixas: não tem como fechar positivo. */
        public boolean orcamentoInviavel() {
            return sobraDoMes < 0;
        }
    }
}
