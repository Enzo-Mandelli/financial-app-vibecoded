package com.pixelwallet.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.widget.EditText;
import android.widget.TextView;

import com.pixelwallet.R;
import com.pixelwallet.data.Repositorio;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalDate;

/**
 * Abre as telas de verdade (layouts, tema, fonte) e usa o app como uma pessoa usaria.
 * Um único teste porque o Repositorio é um singleton que guarda o banco.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class TelasTest {

    @Test
    public void fluxoCompleto() {
        // Config: renda de R$ 3.000 e aluguel de R$ 1.500
        ConfigActivity config = Robolectric.buildActivity(ConfigActivity.class).setup().get();
        EditText renda = config.findViewById(R.id.campoRenda);
        renda.setText("300000");
        assertEquals("R$ 3.000,00", renda.getText().toString());
        Repositorio repo = Repositorio.get(config);
        repo.adicionarDivida("ALUGUEL", 150000);
        config.findViewById(R.id.btnSalvar).performClick();
        assertEquals(300000, repo.renda());

        // Principal: saldo de hoje = sobra × dia ÷ dias do mês
        MainActivity main = Robolectric.buildActivity(MainActivity.class).setup().get();
        LocalDate hoje = LocalDate.now();
        long liberado = Math.floorDiv(150000L * hoje.getDayOfMonth(), hoje.lengthOfMonth());
        TextView saldo = main.findViewById(R.id.txtSaldo);
        assertEquals(com.pixelwallet.util.Dinheiro.formatar(liberado), saldo.getText().toString());

        // Registra um gasto de R$ 12,50
        EditText valor = main.findViewById(R.id.campoValor);
        valor.setText("1250");
        assertEquals("R$ 12,50", valor.getText().toString());
        main.findViewById(R.id.btnRegistrar).performClick();
        assertEquals("", valor.getText().toString());
        assertEquals(com.pixelwallet.util.Dinheiro.formatar(liberado - 1250), saldo.getText().toString());

        // Recebe um Pix de R$ 50
        valor.setText("5000");
        main.findViewById(R.id.btnGanho).performClick();
        assertEquals(com.pixelwallet.util.Dinheiro.formatar(liberado - 1250 + 5000), saldo.getText().toString());

        // Valor vazio não registra nada
        main.findViewById(R.id.btnRegistrar).performClick();
        assertEquals(2, repo.registrosDoDia(hoje).size());

        // Tela de registros abre e lista os dois itens
        RegistrosActivity registros = Robolectric.buildActivity(RegistrosActivity.class).setup().get();
        TextView total = registros.findViewById(R.id.txtTotal);
        assertTrue(total.getText().toString().contains("R$ 12,50"));
        assertTrue(total.getText().toString().contains("R$ 50,00"));
    }
}
