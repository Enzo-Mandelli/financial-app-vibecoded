package com.pixelwallet.ui;

import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.pixelwallet.R;
import com.pixelwallet.data.Registro;
import com.pixelwallet.util.Datas;
import com.pixelwallet.util.Dinheiro;

/** Preenche uma linha da lista: "- R$ 12,50" para gasto, "+ R$ 50,00" em verde para ganho. */
final class ItemRegistro {

    private ItemRegistro() {
    }

    static void preencher(View item, Registro reg) {
        TextView valor = item.findViewById(R.id.itemValor);
        valor.setText((reg.ganho ? "+ " : "- ") + Dinheiro.formatar(reg.valorCentavos));
        valor.setTextColor(ContextCompat.getColor(item.getContext(),
                reg.ganho ? R.color.pw_verde : R.color.pw_texto));
        ((TextView) item.findViewById(R.id.itemHora)).setText(Datas.hora(reg.dataHora));
    }
}
