# RecompDroid

Aplicativo Android para descobrir, organizar e instalar recompilações nativas e ports de jogos compatíveis com Android.

O catálogo é obtido do [DecompBuddy](https://decompbuddy.com/). As páginas, capas e arquivos dos projetos pertencem aos respectivos autores e são servidos por suas fontes.

## Recursos

- Busca jogos pelo nome e navega por console/plataforma.
- Marca jogos como favoritos e consulta os instalados.
- Agrupa várias recompilações do mesmo jogo e permite escolher qual instalar.
- Localiza e baixa o APK Android pelo DecompBuddy, com opção para cancelar o download.
- Oferece visualização em lista ou grade, capas, temas e idioma em português ou inglês.

## Requisitos

- Android 8.0 (API 26) ou superior.
- Conexão com a internet para consultar o catálogo e baixar APKs.
- Para compilar: Android Studio, JDK 17 e Android SDK 35.

Na primeira instalação de um APK baixado pelo app, o Android pode pedir que você permita a instalação de apps de fontes desconhecidas para o RecompDroid.

## Compilar

Abra o projeto no Android Studio e execute a configuração **app**; ou use o Gradle Wrapper na raiz do projeto:

**Windows (PowerShell):**

```powershell
.\gradlew.bat :app:assembleDebug
```

**Linux/macOS:**

```bash
./gradlew :app:assembleDebug
```

O APK de depuração é gerado em:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Fontes

- [DecompBuddy](https://decompbuddy.com/) — páginas dos projetos e downloads.
- [Catálogo DecompBuddy](https://tanookistudios.com/decomp-buddy/catalog.json) — dados usados para listar os jogos.
- [RecompDroid no GitHub](https://github.com/luizfelipesilvasantos57-cpu/RecompDroid)

Os projetos e jogos listados podem ter requisitos, licenças e direitos próprios. Consulte a página de cada projeto antes de usar ou redistribuir os arquivos.

## Licença

Consulte [LICENSE_NOTE.txt](LICENSE_NOTE.txt). O repositório ainda não contém um arquivo `LICENSE` com os termos completos de distribuição.
