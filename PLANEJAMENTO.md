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
| **Por dia**       | **R$ 48,39** |

### Como calcular o "saldo do dia" (decidir — ver seção 7)

| Modo | Fórmula | Comportamento |
|------|---------|---------------|
| **A. Fixo** | `diária − gasto de hoje` | Todo dia zera. Sobra de ontem some, estouro de ontem é esquecido. ⚠️ Não garante fechar no positivo. |
| **B. Acumulado** ⭐ | `diária × dia de hoje − total gasto no mês` | Economizou ontem → sobra hoje. Estourou ontem → tem menos hoje. **Garante fechar no positivo** se o saldo nunca ficar negativo. |
| **C. Recalculado** | `(sobra do mês − gasto até ontem) ÷ dias restantes − gasto de hoje` | Redistribui sobra/estouro pelos dias que faltam. Mais "suave", mas o número diário muda todo dia. |

Exemplo do modo B: dia 3, diária R$ 48,39 → liberado R$ 145,17. Gastou R$ 100 até agora → **saldo hoje: R$ 45,17**.

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
│   DIÁRIA: R$ 48,39           │
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
| Plataforma | Android, minSdk 24 (Android 7+) |
| IDE / build | Android Studio + Gradle |
| Banco local | Room (SQLite) |
| Preferências (renda) | SharedPreferences ou tabela no Room |
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
- **Paleta** (estilo NES/Game Boy):
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
3. **Banco** — Room com Gasto, DividaFixa, Config.
4. **Tela de config** — renda + dívidas fixas.
5. **Tela principal** — saldo, campo com máscara, botão registrar.
6. **Tela de editar** — listar, alterar, apagar.
7. **Polimento 8 bits** — botões, sons, animações, ícone.
8. *(Extras)* widget na tela inicial, gráfico do mês em barras pixeladas, backup/exportação.

---

## 7. Decisões em aberto

- [ ] Modo de cálculo do saldo: **A**, **B** ou **C**? (sugestão: **B**)
- [ ] Renda e dívidas fixas iguais todo mês, ou dá pra mudar mês a mês?
- [ ] Só gastos, ou também registrar **entradas extras** (um freela, um Pix recebido)?
- [ ] Categorias nos gastos (comida, transporte…) ou só valor mesmo?
- [ ] Nome do app — "Pixel Wallet" é provisório.
