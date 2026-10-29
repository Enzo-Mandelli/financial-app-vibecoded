package com.pixelwallet.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.pixelwallet.R;
import com.pixelwallet.data.DividaFixa;
import com.pixelwallet.data.Repositorio;
import com.pixelwallet.util.CampoDinheiro;
import com.pixelwallet.util.Chiptune;
import com.pixelwallet.util.Dinheiro;
import com.pixelwallet.util.Pixel;

import java.time.YearMonth;
import java.util.List;

/** Renda mensal, dívidas fixas e som. */
public class ConfigActivity extends AppCompatActivity {

    private Repositorio repo;

    private EditText campoRenda;
    private LinearLayout listaDividas;
    private TextView txtSemDividas;
    private TextView txtFixas;
    private TextView txtSobra;
    private TextView txtDiaria;
    private Button btnSom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        repo = Repositorio.get(this);

        campoRenda = findViewById(R.id.campoRenda);
        listaDividas = findViewById(R.id.listaDividas);
        txtSemDividas = findViewById(R.id.txtSemDividas);
        txtFixas = findViewById(R.id.txtFixas);
        txtSobra = findViewById(R.id.txtSobra);
        txtDiaria = findViewById(R.id.txtDiaria);
        btnSom = findViewById(R.id.btnSom);

        CampoDinheiro.aplicar(campoRenda);
        if (savedInstanceState == null) {
            CampoDinheiro.definir(campoRenda, repo.renda());
        }
        campoRenda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                atualizarResumo();
            }
        });

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnAdicionar).setOnClickListener(v -> editarDivida(null));
        findViewById(R.id.btnSalvar).setOnClickListener(v -> salvar());
        btnSom.setOnClickListener(v -> {
            repo.definirSom(!repo.somLigado());
            atualizarSom();
            Chiptune.confirmar(this);
        });

        atualizarSom();
        carregarDividas();
    }

    private void salvar() {
        long renda = CampoDinheiro.valor(campoRenda);
        if (renda <= 0) {
            Chiptune.erro(this);
            Pixel.tremer(campoRenda);
            return;
        }
        repo.salvarRenda(renda);
        Chiptune.confirmar(this);
        finish();
    }

    private void atualizarSom() {
        btnSom.setText(repo.somLigado() ? "SOM: LIGADO" : "SOM: DESLIGADO");
    }

    private void carregarDividas() {
        List<DividaFixa> dividas = repo.dividasFixas();
        listaDividas.removeAllViews();
        txtSemDividas.setVisibility(dividas.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (DividaFixa d : dividas) {
            View item = inflater.inflate(R.layout.item_gasto, listaDividas, false);
            ((TextView) item.findViewById(R.id.itemValor)).setText(d.nome);
            TextView valor = item.findViewById(R.id.itemHora);
            valor.setText(Dinheiro.formatar(d.valorCentavos));
            valor.setTextColor(ContextCompat.getColor(this, R.color.pw_vermelho));
            item.setOnClickListener(v -> abrirOpcoes(d));
            listaDividas.addView(item);
        }
        atualizarResumo();
    }

    private void atualizarResumo() {
        long renda = CampoDinheiro.valor(campoRenda);
        long fixas = repo.totalFixas();
        long sobra = renda - fixas;
        int dias = YearMonth.now().lengthOfMonth();
        txtFixas.setText("FIXAS:  " + Dinheiro.formatar(fixas));
        txtSobra.setText("SOBRA:  " + Dinheiro.formatar(sobra));
        txtDiaria.setText("DIARIA: " + Dinheiro.formatar(Math.floorDiv(sobra, dias)));
        txtSobra.setTextColor(ContextCompat.getColor(this,
                sobra < 0 ? R.color.pw_vermelho : R.color.pw_verde));
    }

    private void abrirOpcoes(DividaFixa d) {
        new PixelDialog(this)
                .titulo(d.nome)
                .mensagem(Dinheiro.formatar(d.valorCentavos) + " POR MES")
                .botao("ALTERAR", R.drawable.btn_primario, () -> editarDivida(d))
                .botao("APAGAR", R.drawable.btn_perigo, () -> {
                    repo.apagarDivida(d.id);
                    Chiptune.apagar(this);
                    carregarDividas();
                })
                .cancelar()
                .show();
    }

    /** Adiciona (d == null) ou altera uma dívida fixa. */
    private void editarDivida(DividaFixa d) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_divida, null, false);
        EditText campoNome = form.findViewById(R.id.campoNome);
        EditText campoValor = form.findViewById(R.id.campoValor);
        CampoDinheiro.aplicar(campoValor);
        if (d != null) {
            campoNome.setText(d.nome);
            CampoDinheiro.definir(campoValor, d.valorCentavos);
        }
        campoNome.requestFocus();

        new PixelDialog(this)
                .titulo(d == null ? "NOVA CONTA FIXA" : "ALTERAR CONTA")
                .conteudo(form)
                .botao("SALVAR", R.drawable.btn_primario, () -> {
                    String nome = campoNome.getText().toString().trim().toUpperCase();
                    long valor = CampoDinheiro.valor(campoValor);
                    if (nome.isEmpty()) {
                        Chiptune.erro(this);
                        Pixel.tremer(campoNome);
                        return false;
                    }
                    if (valor <= 0) {
                        Chiptune.erro(this);
                        Pixel.tremer(campoValor);
                        return false;
                    }
                    if (d == null) {
                        repo.adicionarDivida(nome, valor);
                    } else {
                        repo.alterarDivida(d.id, nome, valor);
                    }
                    Chiptune.confirmar(this);
                    carregarDividas();
                    return true;
                })
                .cancelar()
                .show();
    }
}
