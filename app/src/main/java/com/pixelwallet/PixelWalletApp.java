package com.pixelwallet;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.pixelwallet.data.Repositorio;

public class PixelWalletApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        aplicarTema(Repositorio.get(this));
    }

    /** Escuro é o padrão; o claro só entra se a pessoa escolher em CONFIG. */
    public static void aplicarTema(Repositorio repo) {
        AppCompatDelegate.setDefaultNightMode(repo.temaEscuro()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
