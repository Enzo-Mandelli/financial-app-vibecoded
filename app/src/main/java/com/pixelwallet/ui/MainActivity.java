package com.pixelwallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.pixelwallet.R;
import com.pixelwallet.data.Gasto;
import com.pixelwallet.data.Repositorio;
import com.pixelwallet.domain.CalculadoraSaldo;
import com.pixelwallet.util.CampoDinheiro;
import com.pixelwallet.util.Chiptune;
import com.pixelwallet.util.Datas;
import com.pixelwallet.util.Dinheiro;
import com.pixelwallet.util.Pixel;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Repositorio repo;

    private TextView txtSaldo;
    private TextView txtStatus;
    private TextView txtDiaria;
    private TextView txtDia;
    private TextView txtGastoMes;
    private BarraDoMes barraMes;
    private EditText campoValor;
    private LinearLayout listaHoje;
    private TextView txtVazioHoje;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable pulsar = new Runnable() {
        @Override
        public void run() {
            txtSaldo.setAlpha(txtSaldo.getAlpha() < 1f ? 1f : 0.35f);
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        repo = Repositorio.get(this);

        txtSaldo = findViewById(R.id.txtSaldo);
        txtStatus = findViewById(R.id.txtStatus);
        txtDiaria = findViewById(R.id.txtDiaria);
        txtDia = findViewById(R.id.txtDia);
        txtGastoMes = findViewById(R.id.txtGastoMes);
        barraMes = findViewById(R.id.barraMes);
        campoValor = findViewById(R.id.campoValor);
        listaHoje = findViewById(R.id.listaHoje);
        txtVazioHoje = findViewById(R.id.txtVazioHoje);

        CampoDinheiro.aplicar(campoValor);
        campoValor.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                registrar();
                return true;
            }
            return false;
        });

        findViewById(R.id.btnRegistrar).setOnClickListener(v -> registrar());
        findViewById(R.id.btnEditar).setOnClickListener(v ->
                startActivity(new Intent(this, RegistrosActivity.class)));
        findViewById(R.id.btnConfig).setOnClickListener(v ->
                startActivity(new Intent(this, ConfigActivity.class)));

        // Primeira vez: pede renda e dívidas fixas antes de tudo.
        if (savedInstanceState == null && !repo.configurado()) {
            startActivity(new Intent(this, ConfigActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizar(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pararPulso();
    }

    private void registrar() {
        long valor = CampoDinheiro.valor(campoValor);
        if (valor <= 0) {
            Chiptune.erro(this);
            Pixel.tremer(campoValor);
            return;
        }
        repo.registrarGasto(valor);
        campoValor.setText("");
        Chiptune.moeda(this);
        atualizar(true);
    }

    private void atualizar(boolean animar) {
        LocalDate hoje = LocalDate.now();
        YearMonth mes = YearMonth.from(hoje);

        CalculadoraSaldo.Resultado r = CalculadoraSaldo.calcular(
                repo.renda(),
                repo.totalFixas(),
                mes.lengthOfMonth(),
                hoje.getDayOfMonth(),
                repo.totalGastoNoMes(mes),
                repo.totalGastoNoDia(hoje));

        txtSaldo.setText(Dinheiro.formatar(r.saldoHoje));
        txtSaldo.setTextColor(ContextCompat.getColor(this,
                r.negativo() ? R.color.pw_vermelho : R.color.pw_verde));

        if (!repo.configurado()) {
            txtStatus.setText("CONFIGURE SUA RENDA EM CONFIG");
        } else if (r.orcamentoInviavel()) {
            txtStatus.setText("FIXAS > RENDA! REVEJA AS CONTAS");
        } else if (r.negativo()) {
            txtStatus.setText("ESTOUROU! SEGURA OS GASTOS");
        } else if (r.gastoHoje == 0) {
            txtStatus.setText("PODE GASTAR HOJE");
        } else {
            txtStatus.setText("AINDA PODE GASTAR HOJE");
        }

        txtDiaria.setText("DIARIA:   " + Dinheiro.formatar(r.diaria));
        txtGastoMes.setText("NO MES:   " + Dinheiro.formatar(r.gastoNoMes));
        txtDia.setText("DIA " + Datas.doisDigitos(r.diaDeHoje) + "/" + r.diasNoMes
                + "  " + Datas.mes(mes));
        barraMes.definir(r.diaDeHoje, r.diasNoMes);

        preencherHoje(repo.gastosDoDia(hoje));

        pararPulso();
        if (animar) {
            Pixel.piscar(txtSaldo);
        }
        if (r.negativo()) {
            handler.postDelayed(pulsar, animar ? 900 : 500);
        }
    }

    private void pararPulso() {
        handler.removeCallbacks(pulsar);
        txtSaldo.setAlpha(1f);
    }

    private void preencherHoje(List<Gasto> gastos) {
        listaHoje.removeAllViews();
        txtVazioHoje.setVisibility(gastos.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Gasto g : gastos) {
            View item = inflater.inflate(R.layout.item_gasto, listaHoje, false);
            ((TextView) item.findViewById(R.id.itemValor)).setText("- " + Dinheiro.formatar(g.valorCentavos));
            ((TextView) item.findViewById(R.id.itemHora)).setText(Datas.hora(g.dataHora));
            listaHoje.addView(item);
        }
    }
}
