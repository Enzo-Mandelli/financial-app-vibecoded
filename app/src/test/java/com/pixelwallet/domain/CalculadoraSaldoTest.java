package com.pixelwallet.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CalculadoraSaldoTest {

    @Test
    public void diariaDoExemploDoPlanejamento() {
        // R$ 3.000 − R$ 1.500 em 31 dias = R$ 48,38 (48,387... arredondado pra baixo)
        CalculadoraSaldo.Resultado r = CalculadoraSaldo.calcular(300000, 150000, 31, 1, 0, 0, 0);
        assertEquals(150000, r.sobraDoMes);
        assertEquals(4838, r.diaria);
        assertEquals(4838, r.saldoHoje);
    }

    @Test
    public void sobraDeOntemAcumulaParaHoje() {
        // Dia 3, liberado = 150000 × 3 ÷ 31 = 14516; gastou R$ 100 → sobra R$ 45,16
        CalculadoraSaldo.Resultado r = CalculadoraSaldo.calcular(300000, 150000, 31, 3, 10000, 0, 0);
        assertEquals(14516, r.liberadoAteHoje);
        assertEquals(4516, r.saldoHoje);
        assertFalse(r.negativo());
    }

    @Test
    public void estouroDeOntemReduzHoje() {
        // Dia 2 com diária ~R$ 48: gastou R$ 150 no mês → negativo
        CalculadoraSaldo.Resultado r = CalculadoraSaldo.calcular(300000, 150000, 31, 2, 15000, 15000, 0);
        assertEquals(9677, r.liberadoAteHoje);
        assertEquals(-5323, r.saldoHoje);
        assertTrue(r.negativo());
    }

    @Test
    public void ultimoDiaLiberaExatamenteASobraDoMes() {
        CalculadoraSaldo.Resultado r = CalculadoraSaldo.calcular(300000, 150000, 31, 31, 0, 0, 0);
        assertEquals(150000, r.liberadoAteHoje);
    }

    @Test
    public void gastarTudoNoUltimoDiaFechaEmZero() {
        CalculadoraSaldo.Resultado r = CalculadoraSaldo.calcular(300000, 150000, 30, 30, 150000, 0, 0);
        assertEquals(0, r.saldoHoje);
        assertFalse(r.negativo());
    }

    @Test
    public void fixasMaioresQueRendaEhInviavel() {
        CalculadoraSaldo.Resultado r = CalculadoraSaldo.calcular(100000, 150000, 30, 1, 0, 0, 0);
        assertTrue(r.orcamentoInviavel());
        assertTrue(r.negativo());
        // Arredonda pra baixo também no negativo (−50000 ÷ 30 = −1666,67 → −1667)
        assertEquals(-1667, r.diaria);
    }

    @Test
    public void ganhoExtraEntraInteiroNoSaldoDeHoje() {
        // Dia 3: liberado R$ 145,16, gastou R$ 100, recebeu um Pix de R$ 50
        CalculadoraSaldo.Resultado r = CalculadoraSaldo.calcular(300000, 150000, 31, 3, 10000, 0, 5000);
        assertEquals(9516, r.saldoHoje);
    }

    @Test
    public void ganhoExtraCobreEstouro() {
        // Dia 2 estourado em R$ 53,23; um ganho de R$ 60 volta pro positivo
        CalculadoraSaldo.Resultado r = CalculadoraSaldo.calcular(300000, 150000, 31, 2, 15000, 15000, 6000);
        assertEquals(677, r.saldoHoje);
        assertFalse(r.negativo());
    }

    @Test(expected = IllegalArgumentException.class)
    public void diaForaDoMesFalha() {
        CalculadoraSaldo.calcular(100000, 0, 28, 29, 0, 0, 0);
    }
}
