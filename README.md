# 👾 Pixel Wallet

App Android em **Java** de controle financeiro diário, com visual 8 bits.

Ele responde uma pergunta: **quanto eu posso gastar hoje e ainda fechar o mês no positivo?**

```
sobra do mês      = renda − contas fixas
liberado até hoje = sobra do mês × dia de hoje ÷ dias do mês
SALDO DE HOJE     = liberado até hoje − tudo que já gastei no mês
```

Gastou menos ontem? Sobra pra hoje. Estourou? Hoje tem menos. Se o saldo
nunca ficar negativo, o mês fecha no azul.

## Telas

| Tela | O que faz |
|------|-----------|
| **Principal** | Saldo de hoje (verde/vermelho), diária, barra do mês, campo de valor e botão **REGISTRAR** |
| **EDITAR** | Gastos do mês agrupados por dia, navega entre meses; toque num gasto para **alterar** ou **apagar** |
| **CONFIG** | Renda mensal, contas fixas (adicionar/alterar/apagar), resumo da diária, tema escuro/claro, som liga/desliga |

O campo de valor funciona como maquininha: digite `1250` e aparece `R$ 12,50`.

## Como rodar

1. Instale o [Android Studio](https://developer.android.com/studio).
2. **File → Open** e escolha a pasta deste repositório.
3. Espere o Gradle sincronizar (ele baixa o SDK 34 se precisar).
4. Conecte o celular com depuração USB ligada (ou crie um emulador) e clique em **Run ▶**.

Requisitos: Android 8.0+ (minSdk 26).

Testes da lógica de cálculo e formatação:

```
./gradlew test
```

## Estrutura

```
app/src/main/java/com/pixelwallet/
├── domain/CalculadoraSaldo.java   ← a conta do saldo (lógica pura, testada)
├── data/Repositorio.java          ← SQLite (gastos, contas fixas) + SharedPreferences (renda, som)
├── ui/MainActivity.java           ← tela principal
├── ui/RegistrosActivity.java      ← editar/apagar gastos
├── ui/ConfigActivity.java         ← renda e contas fixas
├── ui/PixelDialog.java            ← caixas de diálogo estilo RPG
├── ui/BarraDoMes.java             ← barra de progresso em blocos
└── util/                          ← máscara de dinheiro, sons chiptune, animações em degraus
```

Valores são guardados em **centavos (`long`)** para não ter erro de arredondamento.

## Visual 8 bits

- Fonte [Press Start 2P](https://fonts.google.com/specimen/Press+Start+2P) (SIL Open Font License, ver `FONT_LICENSE_OFL.txt`)
- Paleta estilo PICO-8/NES
- **Tema escuro por padrão** (mesmo se o celular estiver no claro); dá pra trocar para o claro em CONFIG
- Botões com sombra sólida que "afundam" ao tocar
- Caixas com borda dupla de RPG
- Cursor em bloco amarelo
- Sons de onda quadrada gerados na hora (moeda, erro, apagar)
- Animações em degraus, sem suavização; saldo negativo fica piscando
- Ícone: moeda em pixel art

Planejamento completo em [`PLANEJAMENTO.md`](PLANEJAMENTO.md).
