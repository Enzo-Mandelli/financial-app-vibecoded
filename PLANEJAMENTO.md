# 💾 PIXEL WALLET — Planejamento

App Android (Java) de controle financeiro diário com visual 8 bits.

---

## 1. Ideia central

O app responde uma pergunta só: **"Quanto eu posso gastar HOJE e ainda fechar o mês no positivo?"**

```
orçamento diário = (renda mensal − dívidas fixas) ÷ dias do mês
```

Exemplo (outubro, 31 dias):

| Item              | Valor        |
|-------------------|--------------|
| Renda             | R$ 3.000,00  |
| Dívidas fixas     | R$ 1.500,00  |
| Sobra p/ o mês    | R$ 1.500,00  |
| **Por dia**       | **R$ 48,38** |

### Como calcular o "saldo do dia" — ✅ escolhido: **modo B (acumulado)**

| Modo | Fórmula | Comportamento |
|------|---------|---------------|
| **A. Fixo** | `diária − gasto de hoje` | Todo dia zera. Sobra de ontem some, estouro de ontem é esquecido. ⚠️ Não garante fechar no positivo. |
| **B. Acumulado** ⭐ | `diária × dia de hoje − total gasto no mês` | Economizou ontem → sobra hoje. Estourou ontem → tem menos hoje. **Garante fechar no positivo** se o saldo nunca ficar negativo. |
| **C. Recalculado** | `(sobra do mês − gasto até ontem) ÷ dias restantes − gasto de hoje` | Redistribui sobra/estouro pelos dias que faltam. Mais "suave", mas o número diário muda todo dia. |

Exemplo do modo B: dia 3 → liberado = R$ 1.500 × 3 ÷ 31 = R$ 145,16. Gastou R$ 100 até agora → **saldo hoje: R$ 45,16**.

> Na implementação o liberado é `sobra × dia ÷ dias` (multiplica antes de dividir), assim no último dia do mês o liberado é exatamente a sobra, sem perder centavos no arredondamento.

---

## 2. Telas

### 🏠 Tela principal
```
┌──────────────────────────────┐
│ [⚙]                  [✎ EDIT]│  ← config | editar registros
│                              │
│      SALDO DE HOJE           │
│     ╔════════════════╗       │
│     ║   R$ 45,17     ║       │  ← verde se ≥ 0, vermelho se < 0
│     ╚════════════════╝       │
│   DIÁRIA: R$ 48,38           │
│   DIA 03/31  ▓▓░░░░░░░░      │  ← barra de progresso do mês
│                              │
│   ┌────────────────────┐     │
│   │ R$ 0,00            │     │  ← campo com máscara monetária
│   └────────────────────┘     │
│   [   ► REGISTRAR   ]        │
│                              │
│   HOJE:                      │
│   - R$ 12,50   14:32         │
│   - R$ 30,00   09:10         │
└──────────────────────────────┘
```

- **Campo de valor formatado**: digita só números e ele preenche da direita (`1` → `R$ 0,01`, `1250` → `R$ 12,50`). Teclado numérico.
- **Registrar**: salva o gasto com data/hora, limpa o campo, atualiza o saldo (com "bip" e animação de moeda, opcional).

### ✎ Tela de editar registros
- Lista de gastos do mês, agrupada por dia.
- Toque no item → diálogo pixelado: **[ALTERAR VALOR]** / **[APAGAR]** / **[CANCELAR]**.
- Apagar pede confirmação ("TEM CERTEZA? S/N").

### ⚙ Tela de configuração
- Renda mensal.
- Lista de dívidas fixas (nome + valor: aluguel, internet, cartão…) com adicionar/editar/remover.
- Mostra o total e a diária resultante.
- Abre automaticamente na primeira vez que o app é aberto.

---

## 3. Regras de negócio

1. Dinheiro guardado em **centavos (`long`)** — nunca `double` (evita erro de arredondamento).
2. Dias do mês vêm do calendário real (28/29/30/31).
3. Virada de mês: gastos antigos ficam no histórico, o cálculo considera só o mês atual.
4. Diária arredondada para baixo no centavo (sempre a favor de sobrar).
5. Valor zero não pode ser registrado.

---

## 4. Stack técnica

| Item | Escolha |
|------|---------|
| Linguagem | Java 17 |
| Plataforma | Android, minSdk 26 (Android 8+) |
| IDE / build | Android Studio + Gradle |
| Banco local | SQLite (`SQLiteOpenHelper`, sem bibliotecas extras) |
| Preferências (renda, som) | SharedPreferences |
| UI | XML layouts + Views (nada de Compose, que é Kotlin) |
| Fonte | [Press Start 2P](https://fonts.google.com/specimen/Press+Start+2P) (licença OFL, livre) |

### Modelo de dados
```
Gasto        { id, valorCentavos, dataHora }
DividaFixa   { id, nome, valorCentavos }
Config       { rendaCentavos }
```

### Estrutura de pacotes
```
com.pixelwallet
├── data/        → entidades, DAOs, AppDatabase, Repository
├── domain/      → CalculadoraSaldo (lógica pura, testável)
├── ui/
│   ├── main/      → MainActivity
│   ├── registros/ → RegistrosActivity + Adapter
│   └── config/    → ConfigActivity
└── util/        → MoneyTextWatcher (máscara), Formatador
```

---

## 5. Design 8 bits 👾

- **Fonte**: Press Start 2P em tudo.
- **Tema escuro por padrão**, com opção de tema claro em CONFIG (cores em `values/` = claro, `values-night/` = escuro).
- **Paleta escura** (estilo NES/PICO-8):
  - Fundo `#0F0F1B` · Painel `#1D2B53` · Texto `#FFF1E8`
  - Positivo `#00E436` · Negativo `#FF004D` · Destaque `#FFEC27`
- **Botões**: retângulos sem cantos arredondados, borda grossa de 4px e "sombra" sólida deslocada; ao apertar, o botão "afunda" (sombra some).
- **Caixas**: bordas duplas estilo diálogo de RPG.
- **Detalhes**: cursor piscando `▌` no campo, saldo "pulsando" quando negativo, sons chiptune curtos (registrar / apagar / erro), ícone do app em pixel art (moeda ou porquinho).
- Sem gradientes, sem sombras borradas, sem animações suaves — tudo em "passos".

---

## 6. Roteiro de desenvolvimento

1. **Setup** — projeto Android, fonte, cores, tema.
2. **Lógica** — `CalculadoraSaldo` + testes unitários.
3. **Banco** — SQLite com Gasto e DividaFixa; renda no SharedPreferences.
4. **Tela de config** — renda + dívidas fixas.
5. **Tela principal** — saldo, campo com máscara, botão registrar.
6. **Tela de editar** — listar, alterar, apagar.
7. **Polimento 8 bits** — botões, sons, animações, ícone.
8. *(Extras)* widget na tela inicial, gráfico do mês em barras pixeladas, backup/exportação.

---

## 7. Decisões

- [x] Modo de cálculo do saldo: **B (acumulado)**
- [x] Renda e dívidas fixas: valem para todo mês; dá pra mudar a qualquer momento em CONFIG
- [x] Ganhos extras (Pix recebido, freela...): botão **+ GANHO EXTRA**; o valor entra inteiro no saldo do dia e a sobra acumula
- [x] Sem categorias, só o valor
- [x] Nome provisório: **Pixel Wallet**
- [x] Dark mode como padrão, com opção de tema claro
