package com.pixelwallet.util;

import static org.junit.Assert.assertEquals;

import android.text.Editable;
import android.text.InputType;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Simula a digitação tecla por tecla, como o teclado do celular faz. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class CampoDinheiroTest {

    private EditText campo;

    @Before
    public void setUp() {
        campo = new EditText(RuntimeEnvironment.getApplication());
        campo.setInputType(InputType.TYPE_CLASS_NUMBER);
        CampoDinheiro.aplicar(campo);
    }

    private void digitar(String teclas) {
        for (char c : teclas.toCharArray()) {
            campo.getText().insert(campo.getSelectionEnd(), String.valueOf(c));
        }
    }

    private void apagar() {
        Editable t = campo.getText();
        int fim = campo.getSelectionEnd();
        t.delete(fim - 1, fim);
    }

    @Test
    public void formataEnquantoDigita() {
        digitar("1");
        assertEquals("R$ 0,01", campo.getText().toString());
        digitar("2");
        assertEquals("R$ 0,12", campo.getText().toString());
        digitar("5");
        assertEquals("R$ 1,25", campo.getText().toString());
        digitar("0");
        assertEquals("R$ 12,50", campo.getText().toString());
        digitar("000");
        assertEquals("R$ 12.500,00", campo.getText().toString());
        assertEquals(1250000, CampoDinheiro.valor(campo));
    }

    @Test
    public void apagarVoltaUmaCasa() {
        digitar("1250");
        apagar();
        assertEquals("R$ 1,25", campo.getText().toString());
        apagar();
        apagar();
        assertEquals("R$ 0,01", campo.getText().toString());
        apagar();
        assertEquals("", campo.getText().toString());
        assertEquals(0, CampoDinheiro.valor(campo));
    }

    @Test
    public void cursorFicaSempreNoFim() {
        digitar("999");
        assertEquals(campo.getText().length(), campo.getSelectionEnd());
    }
}
