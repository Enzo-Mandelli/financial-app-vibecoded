package com.pixelwallet.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DinheiroTest {

    @Test
    public void formataValores() {
        assertEquals("R$ 0,00", Dinheiro.formatar(0));
        assertEquals("R$ 0,01", Dinheiro.formatar(1));
        assertEquals("R$ 0,10", Dinheiro.formatar(10));
        assertEquals("R$ 12,50", Dinheiro.formatar(1250));
        assertEquals("R$ 999,99", Dinheiro.formatar(99999));
        assertEquals("R$ 1.000,00", Dinheiro.formatar(100000));
        assertEquals("R$ 1.234.567,89", Dinheiro.formatar(123456789));
    }

    @Test
    public void formataNegativos() {
        assertEquals("-R$ 53,23", Dinheiro.formatar(-5323));
        assertEquals("-R$ 1.000,00", Dinheiro.formatar(-100000));
    }

    @Test
    public void leDigitosComoMaquininha() {
        assertEquals(0, Dinheiro.centavosDosDigitos(""));
        assertEquals(1, Dinheiro.centavosDosDigitos("1"));
        assertEquals(1250, Dinheiro.centavosDosDigitos("1250"));
        assertEquals(1250, Dinheiro.centavosDosDigitos("R$ 12,50"));
        assertEquals(12505, Dinheiro.centavosDosDigitos("R$ 12,505"));
        assertEquals(5, Dinheiro.centavosDosDigitos("R$ 0,005"));
    }

    @Test
    public void limitaQuantidadeDeDigitos() {
        assertEquals(99999999999L, Dinheiro.centavosDosDigitos("999999999999999"));
    }
}
