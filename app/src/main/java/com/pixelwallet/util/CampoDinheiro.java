package com.pixelwallet.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Máscara de dinheiro estilo maquininha: o usuário digita só números e o
 * valor vai sendo preenchido da direita ("1250" → "R$ 12,50").
 */
public final class CampoDinheiro implements TextWatcher {

    private final EditText campo;
    private boolean formatando;

    private CampoDinheiro(EditText campo) {
        this.campo = campo;
    }

    public static void aplicar(EditText campo) {
        campo.addTextChangedListener(new CampoDinheiro(campo));
    }

    public static long valor(EditText campo) {
        return Dinheiro.centavosDosDigitos(campo.getText());
    }

    public static void definir(EditText campo, long centavos) {
        campo.setText(centavos == 0 ? "" : Dinheiro.formatar(centavos));
        campo.setSelection(campo.length());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (formatando) {
            return;
        }
        long centavos = Dinheiro.centavosDosDigitos(s);
        // Vazio mostra o hint "R$ 0,00".
        String formatado = centavos == 0 ? "" : Dinheiro.formatar(centavos);
        if (!formatado.contentEquals(s)) {
            formatando = true;
            s.replace(0, s.length(), formatado);
            formatando = false;
        }
        campo.setSelection(s.length());
    }
}
