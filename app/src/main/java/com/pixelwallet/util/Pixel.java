package com.pixelwallet.util;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.View;

/** Animações "em degraus", sem suavização, para combinar com o visual 8 bits. */
public final class Pixel {

    private Pixel() {
    }

    public static float dp(View v, float valor) {
        return valor * v.getResources().getDisplayMetrics().density;
    }

    /** Interpolador que anda em N passos secos em vez de deslizar. */
    public static TimeInterpolator degraus(int passos) {
        return t -> (float) Math.floor(t * passos) / passos;
    }

    /** Tremidinha de erro. */
    public static void tremer(View v) {
        float d = dp(v, 6);
        ObjectAnimator a = ObjectAnimator.ofFloat(v, View.TRANSLATION_X, 0, d, -d, d, -d, 0);
        a.setDuration(300);
        a.setInterpolator(degraus(6));
        a.start();
    }

    /** Pisca algumas vezes (usado quando o saldo muda). */
    public static void piscar(View v) {
        ObjectAnimator a = ObjectAnimator.ofFloat(v, View.ALPHA, 1, 0, 1, 0, 1);
        a.setDuration(400);
        a.setInterpolator(degraus(4));
        a.start();
    }
}
