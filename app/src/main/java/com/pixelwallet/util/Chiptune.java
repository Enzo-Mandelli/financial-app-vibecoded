package com.pixelwallet.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import com.pixelwallet.data.Repositorio;

/**
 * Efeitos sonoros 8 bits gerados na hora com onda quadrada
 * (sem arquivos de áudio no app).
 */
public final class Chiptune {

    private static final int TAXA = 22050;
    private static final double VOLUME = 0.18;

    private Chiptune() {
    }

    /** "Bling" de moeda: registrou um gasto. */
    public static void moeda(Context ctx) {
        tocar(ctx, new int[]{988, 1319}, new int[]{70, 260});
    }

    /** Tom descendo: apagou algo. */
    public static void apagar(Context ctx) {
        tocar(ctx, new int[]{523, 392, 262}, new int[]{70, 70, 160});
    }

    /** Tom subindo: salvou/alterou algo. */
    public static void confirmar(Context ctx) {
        tocar(ctx, new int[]{523, 659, 784}, new int[]{60, 60, 120});
    }

    /** Buzz grave: ação inválida. */
    public static void erro(Context ctx) {
        tocar(ctx, new int[]{147, 110}, new int[]{110, 220});
    }

    private static void tocar(Context ctx, int[] freqs, int[] duracoesMs) {
        if (!Repositorio.get(ctx).somLigado()) {
            return;
        }
        new Thread(() -> {
            short[] amostras = gerar(freqs, duracoesMs);
            AudioTrack track = null;
            try {
                track = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(TAXA)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build())
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .setBufferSizeInBytes(amostras.length * 2)
                        .build();
                track.write(amostras, 0, amostras.length);
                track.play();
                Thread.sleep(amostras.length * 1000L / TAXA + 50);
            } catch (Exception ignorado) {
                // Som é enfeite: se falhar, o app segue normal.
            } finally {
                if (track != null) {
                    track.release();
                }
            }
        }).start();
    }

    private static short[] gerar(int[] freqs, int[] duracoesMs) {
        int total = 0;
        for (int ms : duracoesMs) {
            total += TAXA * ms / 1000;
        }
        short[] out = new short[total];
        int pos = 0;
        short amp = (short) (Short.MAX_VALUE * VOLUME);
        for (int n = 0; n < freqs.length; n++) {
            int len = TAXA * duracoesMs[n] / 1000;
            double periodo = (double) TAXA / freqs[n];
            for (int i = 0; i < len; i++) {
                // Decaimento em degraus no fim da nota, bem "NES".
                double env = i < len * 3 / 4 ? 1.0 : 0.5;
                boolean alto = (i % periodo) < periodo / 2;
                out[pos++] = (short) ((alto ? amp : -amp) * env);
            }
        }
        return out;
    }
}
