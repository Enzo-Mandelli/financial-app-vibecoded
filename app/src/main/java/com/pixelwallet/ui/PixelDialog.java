package com.pixelwallet.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pixelwallet.R;

/** Caixa de diálogo no estilo RPG, com bordas duplas e botões pixelados. */
public final class PixelDialog {

    /** Ação de um botão. Retorne false para manter o diálogo aberto (ex.: valor inválido). */
    public interface Acao {
        boolean executar();
    }

    private final Context context;
    private final Dialog dialog;
    private final TextView titulo;
    private final TextView mensagem;
    private final FrameLayout conteudo;
    private final LinearLayout botoes;

    public PixelDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View raiz = LayoutInflater.from(context).inflate(R.layout.dialog_pixel, null);
        dialog.setContentView(raiz);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        titulo = raiz.findViewById(R.id.dialogTitulo);
        mensagem = raiz.findViewById(R.id.dialogMensagem);
        conteudo = raiz.findViewById(R.id.dialogConteudo);
        botoes = raiz.findViewById(R.id.dialogBotoes);
    }

    public PixelDialog titulo(String texto) {
        titulo.setText(texto);
        return this;
    }

    public PixelDialog mensagem(String texto) {
        mensagem.setText(texto);
        mensagem.setVisibility(View.VISIBLE);
        return this;
    }

    /** Conteúdo extra (campos de texto). Abre o teclado junto. */
    public PixelDialog conteudo(View view) {
        conteudo.addView(view);
        conteudo.setVisibility(View.VISIBLE);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        return this;
    }

    public PixelDialog botao(String texto, int fundo, Acao acao) {
        Button b = (Button) LayoutInflater.from(context)
                .inflate(R.layout.item_botao_dialog, botoes, false);
        b.setText(texto);
        b.setBackgroundResource(fundo);
        b.setOnClickListener(v -> {
            if (acao == null || acao.executar()) {
                dialog.dismiss();
            }
        });
        botoes.addView(b);
        return this;
    }

    public PixelDialog botao(String texto, int fundo, Runnable acao) {
        return botao(texto, fundo, () -> {
            if (acao != null) {
                acao.run();
            }
            return true;
        });
    }

    public PixelDialog cancelar() {
        return botao("CANCELAR", R.drawable.btn_neutro, (Acao) null);
    }

    public void show() {
        dialog.show();
        // Só depois do show() o tamanho da janela é respeitado em todos os aparelhos.
        Window w = dialog.getWindow();
        if (w != null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
