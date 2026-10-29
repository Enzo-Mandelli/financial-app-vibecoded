package com.pixelwallet.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.pixelwallet.R;

/** Barra de progresso do mês: um bloquinho por dia, aceso até hoje. */
public class BarraDoMes extends View {

    private final Paint passado = new Paint();
    private final Paint hoje = new Paint();
    private final Paint futuro = new Paint();
    private int dias = 30;
    private int diaAtual = 1;

    public BarraDoMes(Context context, AttributeSet attrs) {
        super(context, attrs);
        passado.setColor(ContextCompat.getColor(context, R.color.pw_azul));
        hoje.setColor(ContextCompat.getColor(context, R.color.pw_amarelo));
        futuro.setColor(ContextCompat.getColor(context, R.color.pw_painel));
    }

    public void definir(int diaAtual, int dias) {
        this.diaAtual = diaAtual;
        this.dias = dias;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float gap = getResources().getDisplayMetrics().density * 2;
        float larguraBloco = (getWidth() - gap * (dias - 1)) / dias;
        float altura = getHeight();
        for (int i = 0; i < dias; i++) {
            int dia = i + 1;
            Paint p = dia < diaAtual ? passado : dia == diaAtual ? hoje : futuro;
            float x = i * (larguraBloco + gap);
            // Arredonda para pixels inteiros: nada de anti-aliasing.
            canvas.drawRect(Math.round(x), 0, Math.round(x + larguraBloco), altura, p);
        }
    }
}
