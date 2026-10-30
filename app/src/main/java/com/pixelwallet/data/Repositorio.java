package com.pixelwallet.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso aos dados do app: gastos e dívidas fixas no SQLite,
 * renda mensal no SharedPreferences.
 */
public final class Repositorio {

    private static final String PREFS = "pixel_wallet";
    private static final String KEY_RENDA = "renda_centavos";
    private static final String KEY_SOM = "som_ligado";
    private static final String KEY_TEMA_ESCURO = "tema_escuro";

    private static Repositorio instancia;

    private final Banco banco;
    private final SharedPreferences prefs;

    private Repositorio(Context context) {
        Context app = context.getApplicationContext();
        banco = new Banco(app);
        prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized Repositorio get(Context context) {
        if (instancia == null) {
            instancia = new Repositorio(context);
        }
        return instancia;
    }

    // ---- Renda ----

    public boolean configurado() {
        return prefs.contains(KEY_RENDA);
    }

    public long renda() {
        return prefs.getLong(KEY_RENDA, 0);
    }

    public void salvarRenda(long centavos) {
        prefs.edit().putLong(KEY_RENDA, centavos).apply();
    }

    public boolean somLigado() {
        return prefs.getBoolean(KEY_SOM, true);
    }

    public void definirSom(boolean ligado) {
        prefs.edit().putBoolean(KEY_SOM, ligado).apply();
    }

    /** O app começa no tema escuro, independente do tema do celular. */
    public boolean temaEscuro() {
        return prefs.getBoolean(KEY_TEMA_ESCURO, true);
    }

    public void definirTemaEscuro(boolean escuro) {
        prefs.edit().putBoolean(KEY_TEMA_ESCURO, escuro).apply();
    }

    // ---- Gastos e ganhos extras ----

    public void registrar(long valorCentavos, boolean ganho) {
        ContentValues cv = new ContentValues();
        cv.put("valor_centavos", valorCentavos);
        cv.put("data_hora", System.currentTimeMillis());
        cv.put("ganho", ganho ? 1 : 0);
        banco.getWritableDatabase().insert("gasto", null, cv);
    }

    public void alterarRegistro(long id, long valorCentavos) {
        ContentValues cv = new ContentValues();
        cv.put("valor_centavos", valorCentavos);
        banco.getWritableDatabase().update("gasto", cv, "id = ?", args(id));
    }

    public void apagarRegistro(long id) {
        banco.getWritableDatabase().delete("gasto", "id = ?", args(id));
    }

    /** Gastos e ganhos do mês, do mais recente para o mais antigo. */
    public List<Registro> registrosDoMes(YearMonth mes) {
        return registrosEntre(inicio(mes.atDay(1)), inicio(mes.plusMonths(1).atDay(1)));
    }

    public List<Registro> registrosDoDia(LocalDate dia) {
        return registrosEntre(inicio(dia), inicio(dia.plusDays(1)));
    }

    public long totalGastoNoMes(YearMonth mes) {
        return somaEntre(inicio(mes.atDay(1)), inicio(mes.plusMonths(1).atDay(1)), false);
    }

    public long totalGastoNoDia(LocalDate dia) {
        return somaEntre(inicio(dia), inicio(dia.plusDays(1)), false);
    }

    public long totalGanhoNoMes(YearMonth mes) {
        return somaEntre(inicio(mes.atDay(1)), inicio(mes.plusMonths(1).atDay(1)), true);
    }

    private List<Registro> registrosEntre(long inicio, long fim) {
        List<Registro> lista = new ArrayList<>();
        try (Cursor c = banco.getReadableDatabase().query("gasto",
                new String[]{"id", "valor_centavos", "data_hora", "ganho"},
                "data_hora >= ? AND data_hora < ?", args(inicio, fim),
                null, null, "data_hora DESC")) {
            while (c.moveToNext()) {
                lista.add(new Registro(c.getLong(0), c.getLong(1), c.getLong(2), c.getInt(3) == 1));
            }
        }
        return lista;
    }

    private long somaEntre(long inicio, long fim, boolean ganho) {
        try (Cursor c = banco.getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(valor_centavos), 0) FROM gasto"
                        + " WHERE data_hora >= ? AND data_hora < ? AND ganho = ?",
                args(inicio, fim, ganho ? 1 : 0))) {
            return c.moveToFirst() ? c.getLong(0) : 0;
        }
    }

    // ---- Dívidas fixas ----

    public List<DividaFixa> dividasFixas() {
        List<DividaFixa> lista = new ArrayList<>();
        try (Cursor c = banco.getReadableDatabase().query("divida_fixa",
                new String[]{"id", "nome", "valor_centavos"},
                null, null, null, null, "id")) {
            while (c.moveToNext()) {
                lista.add(new DividaFixa(c.getLong(0), c.getString(1), c.getLong(2)));
            }
        }
        return lista;
    }

    public long totalFixas() {
        try (Cursor c = banco.getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(valor_centavos), 0) FROM divida_fixa", null)) {
            return c.moveToFirst() ? c.getLong(0) : 0;
        }
    }

    public void adicionarDivida(String nome, long valorCentavos) {
        ContentValues cv = new ContentValues();
        cv.put("nome", nome);
        cv.put("valor_centavos", valorCentavos);
        banco.getWritableDatabase().insert("divida_fixa", null, cv);
    }

    public void alterarDivida(long id, String nome, long valorCentavos) {
        ContentValues cv = new ContentValues();
        cv.put("nome", nome);
        cv.put("valor_centavos", valorCentavos);
        banco.getWritableDatabase().update("divida_fixa", cv, "id = ?", args(id));
    }

    public void apagarDivida(long id) {
        banco.getWritableDatabase().delete("divida_fixa", "id = ?", args(id));
    }

    // ---- Utilitários ----

    private static long inicio(LocalDate dia) {
        return dia.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private static String[] args(long... valores) {
        String[] s = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            s[i] = Long.toString(valores[i]);
        }
        return s;
    }

    private static final class Banco extends SQLiteOpenHelper {
        Banco(Context context) {
            super(context, "pixel_wallet.db", null, 2);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // A tabela se chama "gasto" por histórico, mas guarda gastos e ganhos extras.
            db.execSQL("CREATE TABLE gasto ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "valor_centavos INTEGER NOT NULL,"
                    + "data_hora INTEGER NOT NULL,"
                    + "ganho INTEGER NOT NULL DEFAULT 0)");
            db.execSQL("CREATE INDEX idx_gasto_data ON gasto(data_hora)");
            db.execSQL("CREATE TABLE divida_fixa ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "nome TEXT NOT NULL,"
                    + "valor_centavos INTEGER NOT NULL)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                // v2: ganhos extras. Tudo que já existia era gasto.
                db.execSQL("ALTER TABLE gasto ADD COLUMN ganho INTEGER NOT NULL DEFAULT 0");
            }
        }
    }
}
