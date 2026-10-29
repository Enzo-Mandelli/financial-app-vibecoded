package com.pixelwallet.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pixelwallet.R;
import com.pixelwallet.data.Gasto;
import com.pixelwallet.data.Repositorio;
import com.pixelwallet.util.CampoDinheiro;
import com.pixelwallet.util.Chiptune;
import com.pixelwallet.util.Datas;
import com.pixelwallet.util.Dinheiro;
import com.pixelwallet.util.Pixel;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Lista os gastos do mês agrupados por dia; tocar num gasto permite alterar ou apagar. */
public class RegistrosActivity extends AppCompatActivity {

    private Repositorio repo;
    private YearMonth mes = YearMonth.now();

    private TextView txtMes;
    private TextView txtTotal;
    private TextView txtVazio;
    private View btnProximo;
    private final Adaptador adaptador = new Adaptador();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registros);
        repo = Repositorio.get(this);

        txtMes = findViewById(R.id.txtMes);
        txtTotal = findViewById(R.id.txtTotal);
        txtVazio = findViewById(R.id.txtVazio);
        btnProximo = findViewById(R.id.btnProximo);

        RecyclerView lista = findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adaptador);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnAnterior).setOnClickListener(v -> {
            mes = mes.minusMonths(1);
            carregar();
        });
        btnProximo.setOnClickListener(v -> {
            mes = mes.plusMonths(1);
            carregar();
        });

        carregar();
    }

    private void carregar() {
        List<Gasto> gastos = repo.gastosDoMes(mes);
        List<Object> itens = new ArrayList<>();
        LocalDate diaAtual = null;
        long total = 0;
        for (Gasto g : gastos) {
            LocalDate dia = Datas.deMillis(g.dataHora).toLocalDate();
            if (!dia.equals(diaAtual)) {
                itens.add(dia);
                diaAtual = dia;
            }
            itens.add(g);
            total += g.valorCentavos;
        }
        adaptador.definir(itens);

        txtMes.setText(Datas.mes(mes));
        txtTotal.setText("TOTAL: " + Dinheiro.formatar(total));
        txtVazio.setVisibility(gastos.isEmpty() ? View.VISIBLE : View.GONE);
        // Não tem gasto no futuro.
        btnProximo.setVisibility(mes.isBefore(YearMonth.now()) ? View.VISIBLE : View.INVISIBLE);
    }

    private void abrirOpcoes(Gasto g) {
        new PixelDialog(this)
                .titulo(Dinheiro.formatar(g.valorCentavos))
                .mensagem(Datas.dia(Datas.deMillis(g.dataHora).toLocalDate())
                        + " AS " + Datas.hora(g.dataHora))
                .botao("ALTERAR VALOR", R.drawable.btn_primario, () -> alterar(g))
                .botao("APAGAR", R.drawable.btn_perigo, () -> confirmarApagar(g))
                .cancelar()
                .show();
    }

    private void alterar(Gasto g) {
        EditText campo = (EditText) LayoutInflater.from(this)
                .inflate(R.layout.campo_dinheiro, null, false);
        CampoDinheiro.aplicar(campo);
        CampoDinheiro.definir(campo, g.valorCentavos);
        campo.requestFocus();

        new PixelDialog(this)
                .titulo("NOVO VALOR")
                .conteudo(campo)
                .botao("SALVAR", R.drawable.btn_primario, () -> {
                    long valor = CampoDinheiro.valor(campo);
                    if (valor <= 0) {
                        Chiptune.erro(this);
                        Pixel.tremer(campo);
                        return false;
                    }
                    repo.alterarGasto(g.id, valor);
                    Chiptune.confirmar(this);
                    carregar();
                    return true;
                })
                .cancelar()
                .show();
    }

    private void confirmarApagar(Gasto g) {
        new PixelDialog(this)
                .titulo("APAGAR?")
                .mensagem("TEM CERTEZA QUE QUER APAGAR " + Dinheiro.formatar(g.valorCentavos) + "?")
                .botao("SIM, APAGAR", R.drawable.btn_perigo, () -> {
                    repo.apagarGasto(g.id);
                    Chiptune.apagar(this);
                    carregar();
                })
                .botao("NAO", R.drawable.btn_neutro, (Runnable) null)
                .show();
    }

    private final class Adaptador extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TIPO_DIA = 0;
        private static final int TIPO_GASTO = 1;

        private List<Object> itens = new ArrayList<>();

        @SuppressWarnings("NotifyDataSetChanged")
        void definir(List<Object> novos) {
            itens = novos;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return itens.get(position) instanceof LocalDate ? TIPO_DIA : TIPO_GASTO;
        }

        @Override
        public int getItemCount() {
            return itens.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = viewType == TIPO_DIA ? R.layout.item_dia : R.layout.item_gasto;
            View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new RecyclerView.ViewHolder(v) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object item = itens.get(position);
            View v = holder.itemView;
            if (item instanceof LocalDate) {
                ((TextView) v).setText(Datas.dia((LocalDate) item));
                return;
            }
            Gasto g = (Gasto) item;
            ((TextView) v.findViewById(R.id.itemValor)).setText("- " + Dinheiro.formatar(g.valorCentavos));
            ((TextView) v.findViewById(R.id.itemHora)).setText(Datas.hora(g.dataHora));
            v.setOnClickListener(x -> abrirOpcoes(g));
        }
    }
}
