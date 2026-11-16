package com.recompdroid

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicReference

object L10n {
    fun t(pt: String, en: String, lang: Int): String = if (lang == 1) en else pt
}

private fun platformFilterGroup(platform: String): String {
    val name = platform.lowercase()
    return when {
        name.contains("pc") || name.contains("windows") || name.contains("linux") ||
            name.contains("macintosh") || name.contains("amiga") || name.contains("commodore") ||
            name.contains("flash") -> "PC"
        name.contains("nintendo") || name.contains("game boy") || name == "gba" ||
            name == "nes" || name == "snes" || name == "n64" -> "Nintendo"
        name.contains("playstation") || name.startsWith("ps ") || name == "psp" ||
            name.contains("ps vita") -> "PlayStation"
        name.contains("sega") || name.contains("dreamcast") || name.contains("mega drive") ||
            name.contains("genesis") || name.contains("saturn") -> "Sega"
        name.contains("xbox") -> "Xbox"
        else -> "Outros"
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var appStore: Store

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appStore = Store(this)
        enableEdgeToEdge()
        setContent {
            val store = remember { appStore }
            var themeKey by remember { mutableIntStateOf(0) }

            RecompDroidTheme(store = store, themeKey = themeKey) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    App(store = store) { themeKey++ }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::appStore.isInitialized) {
            val session = appStore.finishPendingPlay()
            if (session != null) {
                val elapsed = ((System.currentTimeMillis() - session.startedAt) / 1000L).coerceIn(1L, 86_400L)
                session.uid?.let { CloudProfileStore().recordPlay(it, session.slug, session.title, elapsed) }
            }
        }
    }

    private fun launchTrackedGame(context: android.content.Context, store: Store, slug: String, title: String, intent: Intent) {
        val uid = AuthService.create(context)?.currentUser?.uid
        store.beginPlay(uid, slug, title)
        context.startActivity(intent)
    }

    @Composable
    fun RecompDroidTheme(
        store: Store,
        themeKey: Int,
        content: @Composable () -> Unit,
    ) {
        val context = LocalContext.current
        val systemInDark = isSystemInDarkTheme()

        val colorScheme = remember(themeKey, store.themeMode, store.colorAccent, store.dynamicColor, systemInDark) {
            val darkTheme = when (store.themeMode) {
                1 -> true
                2 -> false
                else -> systemInDark
            }

            val primaryColor = when (store.colorAccent) {
                1 -> Color(0xFF3B82F6) // Royal Blue
                2 -> Color(0xFF8B5CF6) // Deep Purple
                3 -> Color(0xFF10B981) // Emerald
                4 -> Color(0xFFEF4444) // Crimson
                5 -> Color(0xFF00E676) // Android Green
                else -> null
            }

            val isOled = (store.colorAccent == 5) && darkTheme

            when {
                primaryColor != null -> {
                    if (darkTheme) {
                        darkColorScheme(
                            primary = primaryColor,
                            secondary = primaryColor.copy(alpha = 0.8f),
                            tertiary = Color(0xFF38BDF8),
                            background = if (isOled) Color.Black else Color(0xFF0F172A),
                            surface = if (isOled) Color(0xFF080809) else Color(0xFF1E293B),
                            surfaceVariant = if (isOled) Color(0xFF151517) else Color(0xFF283548),
                        )
                    } else {
                        lightColorScheme(
                            primary = primaryColor,
                            secondary = primaryColor.copy(alpha = 0.85f),
                            tertiary = Color(0xFF0284C7),
                            background = Color(0xFFF8FAFC),
                            surface = Color(0xFFFFFFFF),
                            surfaceVariant = Color(0xFFF1F5F9),
                        )
                    }
                }
                (store.dynamicColor) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }
                darkTheme -> darkColorScheme(
                    primary = Color(0xFF3DDC84),
                    secondary = Color(0xFF86EFAC),
                    tertiary = Color(0xFF38BDF8),
                    background = Color(0xFF0B1320),
                    surface = Color(0xFF131F33),
                    surfaceVariant = Color(0xFF1E2D48),
                )
                else -> lightColorScheme(
                    primary = Color(0xFF059669),
                    secondary = Color(0xFF0D9488),
                    tertiary = Color(0xFF0284C7),
                    background = Color(0xFFF8FAFC),
                    surface = Color(0xFFFFFFFF),
                    surfaceVariant = Color(0xFFF1F5F9),
                )
            }
        }

        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun App(store: Store, onThemeApply: () -> Unit) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val lang = store.language
        val accountAuth = remember(context) { AuthService.create(context) }
        var accountUser by remember(accountAuth) { mutableStateOf(accountAuth?.currentUser) }
        var profilePhotoBase64 by remember { mutableStateOf<String?>(null) }
        var accountActionRequest by remember { mutableIntStateOf(0) }
        var tab by remember { mutableIntStateOf(0) } // 0: Discover, 1: Platforms, 2: Installed, 3: Settings

        DisposableEffect(accountAuth) {
            val listener = accountAuth?.addAuthStateListener { accountUser = it }
            onDispose { if (listener != null) accountAuth?.removeAuthStateListener(listener) }
        }

        LaunchedEffect(tab, accountUser?.uid) {
            val uid = accountUser?.uid
            if (tab == 0 && uid != null) {
                CloudProfileStore().loadProfile(uid) { data, _ ->
                    profilePhotoBase64 = data?.get("photoBase64") as? String
                }
            } else if (uid == null) {
                profilePhotoBase64 = null
            }
        }

        var games by remember { mutableStateOf<List<Game>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var query by remember { mutableStateOf("") }
        var onlyFavorites by remember { mutableStateOf(false) }
        var selectedPlatformFilter by remember { mutableStateOf<String?>(null) }
        var selectedGame by remember { mutableStateOf<Game?>(null) }
        var pendingInstallGame by remember { mutableStateOf<Game?>(null) }
        var rev by remember { mutableIntStateOf(0) }
        val currentReleaseVersion = remember(context) {
            runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }.getOrNull()?.takeIf { it.isNotBlank() } ?: "0.0.2"
        }
        var showReleaseNotes by remember(store, currentReleaseVersion) {
            mutableStateOf(store.lastSeenReleaseNotesVersion != currentReleaseVersion)
        }

        fun dismissReleaseNotes() {
            store.lastSeenReleaseNotesVersion = currentReleaseVersion
            showReleaseNotes = false
        }

        // Download / Install state per game
        var activeDownloadSlug by remember { mutableStateOf<String?>(null) }
        var downloadProgress by remember { mutableFloatStateOf(0f) }
        var downloadStatusText by remember { mutableStateOf("") }
        var activeDownloadJob by remember { mutableStateOf<Job?>(null) }
        val activeDownloadConnection = remember { AtomicReference<HttpURLConnection?>(null) }

        fun loadCatalog() {
            isLoading = true
            error = null
            scope.launch {
                runCatching { Catalog.games() }
                    .onSuccess {
                        games = it
                        isLoading = false
                    }
                    .onFailure {
                        games = emptyList()
                        error = L10n.t("Não foi possível verificar jogos instaláveis.", "Could not verify installable games.", lang)
                        isLoading = false
                    }
            }
        }

        LaunchedEffect(Unit) {
            loadCatalog()
        }

        fun cancelDownload() {
            activeDownloadJob?.cancel()
            activeDownloadConnection.getAndSet(null)?.disconnect()
        }

        fun startInstallRecomp(game: Game, recomp: RecompOption) {
            pendingInstallGame = null
            if (!context.packageManager.canRequestPackageInstalls()) {
                Toast.makeText(context, L10n.t("Permita a instalação de fontes desconhecidas para o RecompDroid", "Allow unknown app installs for RecompDroid in system settings", lang), Toast.LENGTH_LONG).show()
                context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:${context.packageName}".toUri()))
                return
            }

            activeDownloadSlug = game.slug
            downloadProgress = 0f
            downloadStatusText = L10n.t("Buscando APK no DecompBuddy...", "Finding APK on DecompBuddy...", lang)

            activeDownloadJob = scope.launch {
                runCatching {
                    val rel = Catalog.androidRelease(recomp) { connection ->
                        activeDownloadConnection.set(connection)
                    }
                        ?: error(L10n.t("APK Android não encontrado no DecompBuddy.", "Android APK not found on DecompBuddy.", lang))

                    downloadStatusText = "${L10n.t("Baixando", "Downloading", lang)} ${recomp.project ?: recomp.repo ?: game.title} · ${rel.name} (${rel.tag})..."
                    val (file, pkg) = Installer.download(
                        context,
                        rel,
                        store.githubToken.ifBlank { null },
                        onConnection = { activeDownloadConnection.set(it) },
                    ) { pr ->
                        downloadProgress = pr
                    }

                    store.save(game.slug, rel.tag, pkg)
                    rev++

                    downloadStatusText = L10n.t("Iniciando instalador...", "Launching installer...", lang)
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW)
                            .setDataAndType(uri, "application/vnd.android.package-archive")
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }.onFailure { ex ->
                    if (ex is CancellationException) {
                        downloadStatusText = L10n.t("Download cancelado.", "Download cancelled.", lang)
                    } else {
                        downloadStatusText = "Erro: ${ex.message}"
                        Toast.makeText(context, "${L10n.t("Erro", "Error", lang)}: ${ex.message}", Toast.LENGTH_LONG).show()
                    }
                }
                activeDownloadConnection.set(null)
                activeDownloadJob = null
                activeDownloadSlug = null
            }
        }

        fun installRecomp(game: Game) {
            val choices = game.recomps.ifEmpty {
                listOf(RecompOption(game.slug, game.subtitle, game.repo, game.version))
            }
            if (choices.size > 1) {
                pendingInstallGame = game
            } else {
                startInstallRecomp(game, choices.first())
            }
        }

        if (showReleaseNotes) {
            AlertDialog(
                onDismissRequest = ::dismissReleaseNotes,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.padding(10.dp).size(24.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(L10n.t("Novidades", "What's new", lang), fontWeight = FontWeight.Bold)
                            Text(
                                L10n.t("RecompDroid 0.0.2", "RecompDroid 0.0.2", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            L10n.t("De 0.0.1 para 0.0.2: veja o que chegou nesta versão.", "From 0.0.1 to 0.0.2: here is what is new in this release.", lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        listOf(
                            L10n.t("Catálogo Android atualizado", "Updated Android catalog", lang) to
                                L10n.t("Jogos compatíveis com Android, capas e repositórios revisados com dados do DecompBuddy.", "Android-compatible games, covers, and repositories refreshed with DecompBuddy data.", lang),
                            L10n.t("Recomps agrupados por jogo", "Recomps grouped by game", lang) to
                                L10n.t("Cada jogo aparece em um único cartão. Quando há várias versões, o contador abre a escolha do recomp para instalar.", "Each game appears in one card. When several versions are available, the count opens a picker to choose which recomp to install.", lang),
                            L10n.t("Instalação com controle", "More installation control", lang) to
                                L10n.t("Fluxo de download e instalação de APK aprimorado, com progresso e opção de cancelar.", "Improved APK download and installation flow, with progress and a cancel option.", lang),
                            L10n.t("Busca e navegação melhores", "Improved search and browsing", lang) to
                                L10n.t("Ports de PC aparecem primeiro, consoles mais recentes vêm antes e as categorias solicitadas foram removidas.", "PC ports appear first, newer consoles come before older ones, and the requested categories were removed.", lang),
                            L10n.t("Visual renovado", "Refreshed look", lang) to
                                L10n.t("Ícone e identidade RecompDroid atualizados, com cabeçalho, cartões e telas de configurações mais limpos.", "Updated RecompDroid icon and identity, with a cleaner header, cards, and settings screens.", lang),
                            L10n.t("Conta e segurança", "Account and security", lang) to
                                L10n.t("Login e cadastro por e-mail, verificação de endereço, recuperação de senha e controle para mostrar ou ocultar a senha.", "Email sign-in and registration, address verification, password recovery, and show-or-hide password controls.", lang),
                            L10n.t("Relato de bugs", "Bug reports", lang) to
                                L10n.t("Atalho na tela inicial para enviar relatos diretamente ao GitHub.", "A home-screen shortcut opens GitHub to report bugs.", lang),
                        ).forEach { (heading, description) ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.padding(4.dp).size(16.dp),
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(heading, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = ::dismissReleaseNotes) {
                        Text(L10n.t("Entendi", "Got it", lang))
                    }
                },
            )
        }

        pendingInstallGame?.let { game ->
            RecompPickerDialog(
                game = game,
                lang = lang,
                onRecompSelected = { startInstallRecomp(game, it) },
                onDismiss = { pendingInstallGame = null },
            )
        }

        selectedGame?.let { g ->
            Detail(
                g = g,
                store = store,
                rev = rev,
                bump = { rev++ },
                onInstallRecomp = { installRecomp(g) },
                activeDownloadSlug = activeDownloadSlug,
                downloadProgress = downloadProgress,
                downloadStatusText = downloadStatusText,
                onCancelDownload = ::cancelDownload,
                back = { selectedGame = null },
            )
            return
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth().offset(x = (-12).dp),                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.recompdroid_logo),
                                contentDescription = null,
                                modifier = Modifier.size(88.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (tab == 0) {
                                    Row {
                                        Text("Recomp", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                                        Text("Droid", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                                    }
                                } else {
                                    Text(
                                        text = when (tab) {
                                            1 -> L10n.t("Plataformas", "Platforms", lang)
                                            2 -> L10n.t("Instalados", "Installed", lang)
                                            else -> L10n.t("Ajustes", "Settings", lang)
                                        },
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                    )
                                }
                                if (tab == 0 && games.isNotEmpty()) {
                                    Text(
                                        text = "${games.size} ${L10n.t("recomps catalogados (A-Z)", "recomps cataloged (A-Z)", lang)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        if (tab == 0) {
                            IconButton(onClick = { onlyFavorites = !onlyFavorites }) {
                                Icon(
                                    imageVector = if (onlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favoritos",
                                    tint = if (onlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            IconButton(onClick = { loadCatalog() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Recarregar")
                            }
                            IconButton(onClick = { tab = 3; accountActionRequest++ }) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                ) {
                                    val photo = profilePhotoBase64
                                    if (!photo.isNullOrBlank()) {
                                        AsyncImage(
                                            model = "data:image/jpeg;base64,$photo",
                                            contentDescription = L10n.t("Abrir perfil", "Open profile", lang),
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop,
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = L10n.t("Entrar ou abrir perfil", "Sign in or open profile", lang),
                                            modifier = Modifier.padding(8.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        } else if (tab != 3) {
                            IconButton(onClick = { tab = 3 }) {
                                Icon(Icons.Default.Settings, contentDescription = "Configurações")
                            }
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                ) {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(L10n.t("Discover", "Discover", lang)) },
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                        label = { Text(L10n.t("Plataformas", "Platforms", lang)) },
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        label = { Text(L10n.t("Instalados", "Installed", lang)) },
                    )
                    NavigationBarItem(
                        selected = tab == 3,
                        onClick = { tab = 3 },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(L10n.t("Ajustes", "Settings", lang)) },
                    )
                }
            },
        ) { pad ->
            Box(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize(),
            ) {
                when (tab) {
                    3 -> SettingsScreen(store = store, onApplyChanges = onThemeApply, accountActionRequest = accountActionRequest)
                    1 -> PlatformScreen(
                        games = games,
                        store = store,
                        rev = rev,
                        activeDownloadSlug = activeDownloadSlug,
                        downloadProgress = downloadProgress,
                        onCancelDownload = ::cancelDownload,
                        onInstallClick = { installRecomp(it) },
                        onGameClick = { selectedGame = it },
                        onFavoriteClick = { g ->
                            store.toggleFavorite(g.slug)
                            rev++
                        },
                    )
                    else -> {
                        val installedSet = remember(rev) { store.installed().toSet() }

                        // PC ports first, then newer consoles before older ones.
                        val filteredList = games
                            .filter { (slug, title, subtitle, _, platform) ->
                                val matchesTab = if (tab == 2) slug in installedSet else true
                                val matchesFav = if (onlyFavorites) store.isFavorite(slug) else true
                                val matchesPlat = selectedPlatformFilter == null ||
                                    platform?.let(::platformFilterGroup) == selectedPlatformFilter
                                val matchesQuery = query.isBlank() || title.contains(query, ignoreCase = true) ||
                                        (subtitle?.contains(query, ignoreCase = true) == true)

                                matchesTab && matchesFav && matchesPlat && matchesQuery
                            }
                            .let(Catalog::sortGames)

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                        ) {
                            error?.let {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                }
                            }

                            if (tab == 0) {
                                OutlinedTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 8.dp),
                                    placeholder = { Text(L10n.t("Buscar jogos...", "Search for games...", lang)) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = {
                                        if (query.isNotEmpty()) {
                                            IconButton(onClick = { query = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Limpar")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(28.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF19191B),
                                        unfocusedContainerColor = Color(0xFF19191B),
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                    ),
                                )

                                val platformOptions = remember(games) {
                                    val groups = games.mapNotNull { it.platform }
                                        .map(::platformFilterGroup)
                                        .toSet()
                                    listOf("PC", "Nintendo", "PlayStation", "Sega", "Xbox", "Outros")
                                        .filter { it in groups }
                                }
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                ) {
                                    item {
                                        FilterChip(
                                            selected = selectedPlatformFilter == null,
                                            onClick = { selectedPlatformFilter = null },
                                            label = { Text(L10n.t("Todos", "All", lang)) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color(0xFF111113),
                                                labelColor = Color.White,
                                                selectedContainerColor = Color(0xFF58E68A),
                                                selectedLabelColor = Color.Black,
                                            ),
                                        )
                                    }
                                    items(platformOptions) { platformGroup ->
                                        FilterChip(
                                            selected = selectedPlatformFilter == platformGroup,
                                            onClick = {
                                                selectedPlatformFilter =
                                                    if (selectedPlatformFilter == platformGroup) null else platformGroup
                                            },
                                            label = {
                                                Text(
                                                    when (platformGroup) {
                                                        "PC" -> L10n.t("PC", "PC", lang)
                                                        "Outros" -> L10n.t("Outros", "Other", lang)
                                                        else -> platformGroup
                                                    },
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color(0xFF111113),
                                                labelColor = Color.White,
                                                selectedContainerColor = Color(0xFF58E68A),
                                                selectedLabelColor = Color.Black,
                                            ),
                                        )
                                    }
                                }
                            }

                            if (isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(L10n.t("Carregando recomps...", "Loading recomps...", lang), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            } else if (filteredList.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(32.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_bugdroid_gamepad),
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = if (tab == 2) L10n.t("Nenhum recomp instalado ainda.", "No recomps installed yet.", lang) else L10n.t("Nenhum resultado encontrado.", "No results found.", lang),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (tab == 2) L10n.t("Navegue pelo Catálogo para instalar o APK dos ports disponíveis.", "Browse the Catalog to install APKs for available ports.", lang) else L10n.t("Tente buscar com outros termos ou filtros.", "Try searching with different terms or filters.", lang),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            } else if (store.isGridView) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(top = 4.dp, bottom = if (tab == 0) 88.dp else 24.dp),
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    items(filteredList, key = { it.slug }) { g ->
                                        GameGridCard(
                                            game = g,
                                            store = store,
                                            isDownloading = activeDownloadSlug == g.slug,
                                            downloadProgress = downloadProgress,
                                            onCancelDownload = ::cancelDownload,
                                            onInstallClick = { installRecomp(g) },
                                            onFavoriteClick = {
                                                store.toggleFavorite(g.slug)
                                                rev++
                                            },
                                            onClick = { selectedGame = g },
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(top = 4.dp, bottom = if (tab == 0) 88.dp else 24.dp),
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    items(filteredList, key = { it.slug }) { g ->
                                        GameListCard(
                                            game = g,
                                            store = store,
                                            installedTag = store.tag(g.slug),
                                            isDownloading = activeDownloadSlug == g.slug,
                                            downloadProgress = downloadProgress,
                                            onCancelDownload = ::cancelDownload,
                                            onInstallClick = { installRecomp(g) },
                                            onFavoriteClick = {
                                                store.toggleFavorite(g.slug)
                                                rev++
                                            },
                                            onClick = { selectedGame = g },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = tab == 0,
                    enter = if (store.animationsEnabled) fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 } else EnterTransition.None,
                    exit = if (store.animationsEnabled) fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 3 } else ExitTransition.None,
                    modifier = Modifier.align(Alignment.BottomEnd),
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        "https://github.com/luizfelipesilvasantos57-cpu/RecompDroid/issues/new".toUri(),
                                    ),
                                )
                            }.onFailure {
                                Toast.makeText(
                                    context,
                                    L10n.t("Não foi possível abrir o navegador.", "Could not open the browser.", lang),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                        text = { Text(L10n.t("Relatar bug", "Report bug", lang)) },
                    )
                }
            }
        }
    }

    @Composable
    fun PlatformScreen(
        games: List<Game>,
        store: Store,
        rev: Int,
        activeDownloadSlug: String?,
        downloadProgress: Float,
        onCancelDownload: () -> Unit,
        onInstallClick: (Game) -> Unit,
        onGameClick: (Game) -> Unit,
        onFavoriteClick: (Game) -> Unit,
    ) {
        val platforms = remember(games) {
            Catalog.sortPlatforms(games.mapNotNull { it.platform })
        }
        var selectedPlat by remember { mutableStateOf(platforms.firstOrNull()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
            ) {
                items(platforms) { plat ->
                    FilterChip(
                        selected = selectedPlat == plat,
                        onClick = { selectedPlat = plat },
                        label = { Text(plat, fontWeight = FontWeight.SemiBold) },
                    )
                }
            }

            // Alphabetical order for platform games
            val filtered = games
                .filter { it.platform == selectedPlat }
                .sortedBy { it.title.lowercase() }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(filtered, key = { "${it.slug}_$rev" }) { g ->
                    GameListCard(
                        game = g,
                        store = store,
                        installedTag = store.tag(g.slug),
                        isDownloading = activeDownloadSlug == g.slug,
                        downloadProgress = downloadProgress,
                        onCancelDownload = onCancelDownload,
                        onInstallClick = { onInstallClick(g) },
                        onFavoriteClick = { onFavoriteClick(g) },
                        onClick = { onGameClick(g) },
                    )
                }
            }
        }
    }

    /**
     * Modernized GameListCard showing original covers directly beside each game,
     * status badges, and direct "Instalar Recomp" button.
     */
    @Composable
    fun GameListCard(
        game: Game,
        store: Store,
        installedTag: String?,
        isDownloading: Boolean,
        downloadProgress: Float,
        onCancelDownload: () -> Unit,
        onInstallClick: () -> Unit,
        onFavoriteClick: () -> Unit,
        onClick: () -> Unit,
    ) {
        val context = LocalContext.current
        val lang = store.language

        var resolvedCoverUrl by remember(game.slug) {
            mutableStateOf(game.coverUrl ?: store.customCover(game.slug))
        }

        // Load artwork from the DecompBuddy game page if the catalog has no cover.
        LaunchedEffect(game.slug) {
            if (resolvedCoverUrl == null && store.autoSearchCovers) {
                val details = Catalog.fetchGameDetails(game.slug)
                val found = details.first
                if (found != null) {
                    resolvedCoverUrl = found
                    store.setCustomCover(game.slug, found)
                }
            }
        }

        val shapeRadius = getCardShape(store.cardCornerRadius)
        val isFavorite = store.isFavorite(game.slug)
        val favoriteScale by animateFloatAsState(
            targetValue = if (isFavorite) 1.2f else 1f,
            animationSpec = if (store.animationsEnabled) spring(dampingRatio = Spring.DampingRatioMediumBouncy) else snap(),
            label = "favorite-scale-${game.slug}",
        )
        val coverScale = when (store.coverSize) { 0 -> 0.82f; 2 -> 1.14f; else -> 1f }
        val isPkgInstalled = remember(game.slug, installedTag) { store.isAppInstalled(game.slug) }

        AnimatedVisibility(
            visible = true,
            enter = if (store.animationsEnabled) {
                fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 14 }
            } else EnterTransition.None,
        ) {
        ElevatedCard(
            onClick = onClick,
            modifier = if (store.animationsEnabled) Modifier.fillMaxWidth().animateContentSize(tween(220)) else Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(shapeRadius),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // GAME COVER POSTER (ALWAYS DISPLAYED AT THE LEFT OF THE GAME)
                Box(
                    modifier = Modifier
                        .size(
                            width = (if (store.isCompactCard) 68.dp else 84.dp) * coverScale,
                            height = (if (store.isCompactCard) 92.dp else 114.dp) * coverScale,
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (resolvedCoverUrl != null) {
                        AsyncImage(
                            model = resolvedCoverUrl,
                            contentDescription = game.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(6.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_bugdroid_gamepad),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = game.title.take(8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )
                        }
                    }

                    // Platform badge overlay at the bottom of the cover
                    game.platform?.let { plat ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.72f))
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = plat,
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // GAME DETAILS & CONTROLS
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = game.title,
                            style = if (store.isCompactCard) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )

                        IconButton(
                            onClick = onFavoriteClick,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                modifier = Modifier.size(18.dp).graphicsLayer { scaleX = favoriteScale; scaleY = favoriteScale },
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    game.subtitle?.let { sub ->
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    // Status / Platform chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AssistChip(
                            onClick = onClick,
                            label = { Text(L10n.t("Nativo / 60 FPS", "Native / 60 FPS", lang), fontSize = 10.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            border = null,
                            modifier = Modifier.height(24.dp),
                        )

                        if (game.recomps.size > 1) {
                            AssistChip(
                                onClick = onInstallClick,
                                label = { Text("${game.recomps.size} recomps", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                                border = null,
                                modifier = Modifier.height(24.dp),
                            )
                        }

                        if (isPkgInstalled) {
                            AssistChip(
                                onClick = onClick,
                                label = { Text(L10n.t("Instalado ✓", "Installed ✓", lang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                ),
                                border = null,
                                modifier = Modifier.height(24.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ACTION BUTTON: "INSTALAR RECOMP" or "JOGAR"
                    if (isDownloading) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = L10n.t("Baixando APK...", "Downloading APK...", lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = "${(downloadProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                            )
                            OutlinedButton(
                                onClick = onCancelDownload,
                                modifier = Modifier.fillMaxWidth().height(30.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(L10n.t("Cancelar download", "Cancel download", lang), fontSize = 10.sp)
                            }
                        }
                    } else if (isPkgInstalled) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(
                                onClick = {
                                    val pkg = store.pkg(game.slug)
                                    if (pkg != null) {
                                        context.packageManager.getLaunchIntentForPackage(pkg)?.let {
                                            launchTrackedGame(context, store, game.slug, game.title, it)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                                modifier = Modifier.height(34.dp),
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(L10n.t("Jogar", "Play", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onInstallClick,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp),
                            ) {
                                Text(L10n.t("Atualizar", "Update", lang), fontSize = 11.sp)
                            }
                        }
                    } else {
                        // "Instalar Recomp" Button
                        Button(
                            onClick = onInstallClick,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier.height(34.dp),
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(L10n.t("Instalar Recomp", "Install Recomp", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        }
    }

    /**
     * Modernized GameGridCard showing game cover at the top,
     * details, and "Instalar Recomp" button at the bottom.
     */
    @Composable
    fun GameGridCard(
        game: Game,
        store: Store,
        isDownloading: Boolean,
        downloadProgress: Float,
        onCancelDownload: () -> Unit,
        onInstallClick: () -> Unit,
        onFavoriteClick: () -> Unit,
        onClick: () -> Unit,
    ) {
        val lang = store.language

        var resolvedCoverUrl by remember(game.slug) {
            mutableStateOf(game.coverUrl ?: store.customCover(game.slug))
        }

        LaunchedEffect(game.slug) {
            if (resolvedCoverUrl == null && store.autoSearchCovers) {
                val details = Catalog.fetchGameDetails(game.slug)
                val found = details.first
                if (found != null) {
                    resolvedCoverUrl = found
                    store.setCustomCover(game.slug, found)
                }
            }
        }

        val shapeRadius = getCardShape(store.cardCornerRadius)
        val isFavorite = store.isFavorite(game.slug)
        val favoriteScale by animateFloatAsState(
            targetValue = if (isFavorite) 1.2f else 1f,
            animationSpec = if (store.animationsEnabled) spring(dampingRatio = Spring.DampingRatioMediumBouncy) else snap(),
            label = "favorite-scale-${game.slug}",
        )
        val gridCoverRatio = when (store.coverSize) { 0 -> 0.98f; 2 -> 0.74f; else -> 0.86f }

        AnimatedVisibility(
            visible = true,
            enter = if (store.animationsEnabled) {
                fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 14 }
            } else EnterTransition.None,
        ) {
        ElevatedCard(
            onClick = onClick,
            modifier = if (store.animationsEnabled) Modifier.fillMaxWidth().animateContentSize(tween(220)) else Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(shapeRadius),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            ),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(gridCoverRatio)
                        .clip(RoundedCornerShape(topStart = shapeRadius, topEnd = shapeRadius))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    if (resolvedCoverUrl != null) {
                        AsyncImage(
                            model = resolvedCoverUrl,
                            contentDescription = game.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bugdroid_gamepad),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    // Top favorite button
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            modifier = Modifier.size(16.dp).graphicsLayer { scaleX = favoriteScale; scaleY = favoriteScale },
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White,
                        )
                    }

                }

                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    game.subtitle?.let { sub ->
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AssistChip(
                            onClick = onClick,
                            label = { Text(L10n.t("Nativo / 60 FPS", "Native / 60 FPS", lang), fontSize = 9.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            border = null,
                            modifier = Modifier.height(24.dp),
                        )
                        if (game.recomps.size > 1) {
                            AssistChip(
                                onClick = onInstallClick,
                                label = { Text("${game.recomps.size} recomps", fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                                border = null,
                                modifier = Modifier.height(24.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isDownloading) {
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                        )
                        OutlinedButton(
                            onClick = onCancelDownload,
                            modifier = Modifier.fillMaxWidth().height(30.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(L10n.t("Cancelar download", "Cancel download", lang), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        }
    }

    @Composable
    fun RecompPickerDialog(
        game: Game,
        lang: Int,
        onRecompSelected: (RecompOption) -> Unit,
        onDismiss: () -> Unit,
    ) {
        val options = game.recomps.ifEmpty {
            listOf(RecompOption(game.slug, game.subtitle, game.repo, game.version))
        }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(L10n.t("Escolha qual recomp instalar", "Choose a recomp to install", lang)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    options.forEachIndexed { index, option ->
                        TextButton(
                            onClick = { onRecompSelected(option) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = option.project ?: option.repo ?: "Recomp ${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                option.repo?.let { repository ->
                                    Text(
                                        text = repository,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                option.version?.let { version ->
                                    Text(
                                        text = version,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(L10n.t("Cancelar", "Cancel", lang))
                }
            },
        )
    }

    /**
     * Detailed screen for a recomp game port with synopsis, original covers, and install/launch controls.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Detail(
        g: Game,
        store: Store,
        rev: Int,
        bump: () -> Unit,
        onInstallRecomp: () -> Unit,
        activeDownloadSlug: String?,
        downloadProgress: Float,
        downloadStatusText: String,
        onCancelDownload: () -> Unit,
        back: () -> Unit,
    ) {
        val context = LocalContext.current
        val lang = store.language

        var customCoverUrl by remember { mutableStateOf(store.customCover(g.slug)) }
        val currentRepo by remember {
            mutableStateOf(g.repo)
        }

        var rel by remember { mutableStateOf<Release?>(null) }
        var status by remember { mutableStateOf(L10n.t("Verificando download no DecompBuddy...", "Checking DecompBuddy download...", lang)) }
        var synopsis by remember { mutableStateOf(g.description) }

        var isFav by remember { mutableStateOf(store.isFavorite(g.slug)) }
        var showCoverSearchDialog by remember { mutableStateOf(false) }

        var activeCoverUrl by remember {
            mutableStateOf(g.coverUrl ?: customCoverUrl)
        }

        LaunchedEffect(g, currentRepo) {
            runCatching {
                val details = Catalog.fetchGameDetails(g.slug)
                if (activeCoverUrl == null && details.first != null) {
                    activeCoverUrl = details.first
                }
                if (synopsis == null || synopsis == "Recompilação nativa para Android.") {
                    synopsis = details.second ?: synopsis
                }

                rel = Catalog.androidRelease(g)
            }.onSuccess {
                status = if (rel == null) {
                    L10n.t("APK Android indisponível no DecompBuddy.", "Android APK unavailable on DecompBuddy.", lang)
                } else {
                    ""
                }
            }.onFailure {
                status = "Status: ${it.message}"
            }
        }

        val cur = remember(rev) { store.tag(g.slug) }
        val isPkgInstalled = remember(g.slug, rev) { store.isAppInstalled(g.slug) }
        val isThisDownloading = activeDownloadSlug == g.slug

        if (showCoverSearchDialog) {
            CoverSearchDialog(
                gameTitle = g.title,
                currentUrl = activeCoverUrl,
                onCoverSelected = { newUrl ->
                    store.setCustomCover(g.slug, newUrl)
                    customCoverUrl = newUrl
                    activeCoverUrl = newUrl
                    showCoverSearchDialog = false
                    bump()
                },
            ) { showCoverSearchDialog = false }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(g.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = back) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            isFav = store.toggleFavorite(g.slug)
                            bump()
                        }) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favoritar",
                                tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                )
            },
        ) { pad ->
            Column(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // HERO BANNER COVER FROM ORIGINAL / OFFICIAL SITE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                ) {
                    AsyncImage(
                        model = activeCoverUrl,
                        contentDescription = g.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    OutlinedButton(
                        onClick = { showCoverSearchDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        ),
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(L10n.t("Trocar Capa", "Change Cover", lang), fontSize = 12.sp)
                    }
                }

                Text(g.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)

                g.subtitle?.let {
                    Text(it, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    g.platform?.let { plat ->
                        AssistChip(
                            onClick = {},
                            label = { Text(plat, fontWeight = FontWeight.Medium) },
                        )
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text(L10n.t("Port Nativo C/C++", "Native Port C/C++", lang)) },
                    )
                    if (g.recomps.size > 1) {
                        AssistChip(
                            onClick = onInstallRecomp,
                            label = { Text("${g.recomps.size} recomps", fontWeight = FontWeight.SemiBold) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                    if (isPkgInstalled) {
                        AssistChip(
                            onClick = {},
                            label = { Text(L10n.t("Instalado ✓", "Installed ✓", lang), fontWeight = FontWeight.Bold) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }

                // Sinopse
                Text(
                    text = L10n.t("Sobre esta Recompilação", "About this Recompilation", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = synopsis ?: L10n.t("Recompilação nativa em C/C++ otimizada para arquitetura ARM64 no Android. Requer os arquivos originais da sua cópia legalmente adquirida do jogo para extração de assets.", "Native C/C++ recompilation optimized for ARM64 on Android. Requires the assets from your legally owned game copy.", lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )

                // Projeto e recomp disponível no DecompBuddy
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                    ) {
                        Text(
                            text = L10n.t("Projeto no DecompBuddy:", "DecompBuddy project:", lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = currentRepo ?: g.subtitle ?: "DecompBuddy",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                if (status.isNotEmpty()) {
                    Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }

                // DOWNLOAD PROGRESS CARD
                if (isThisDownloading) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = downloadStatusText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                            )
                            OutlinedButton(onClick = onCancelDownload) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(L10n.t("Cancelar download", "Cancel download", lang))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(downloadProgress * 100).toInt()}% ${L10n.t("concluído", "completed", lang)}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.End),
                            )
                        }
                    }
                }

                // BOTÃO DE INSTALAR RECOMP APK DIRETAMENTE
                rel?.let { r ->
                    Text(
                        text = "${L10n.t("Release disponível", "Available release", lang)}: ${r.tag}" + (cur?.let { " (${L10n.t("versão atual", "current version", lang)}: $it)" } ?: ""),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Button(
                        onClick = onInstallRecomp,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (cur == null) "${L10n.t("Instalar Recomp APK", "Install Recomp APK", lang)} (${r.tag})" else "${L10n.t("Atualizar Recomp APK", "Update Recomp APK", lang)} (${r.tag})",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } ?: run {
                    if (currentRepo != null) {
                        Button(
                            onClick = onInstallRecomp,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(L10n.t("Baixar APK do DecompBuddy", "Download APK from DecompBuddy", lang), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // BOTÃO PARA JOGAR SE ESTIVER INSTALADO
                if (isPkgInstalled) {
                    store.pkg(g.slug)?.let { pkg ->
                        context.packageManager.getLaunchIntentForPackage(pkg)?.let { intent ->
                            Button(
                                onClick = { launchTrackedGame(context, store, g.slug, g.title, intent) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(L10n.t("Jogar Recomp", "Play Recomp", lang), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Settings screen with Language, Visuals, and Fixed Apply Floating Action Button at Bottom-Right Corner.
     */
    @Composable
    fun SettingsScreen(store: Store, onApplyChanges: () -> Unit, accountActionRequest: Int = 0) {
        val context = LocalContext.current
        var language by remember { mutableIntStateOf(store.language) }
        var themeMode by remember { mutableIntStateOf(store.themeMode) }
        var colorAccent by remember { mutableIntStateOf(store.colorAccent) }
        var cardCornerRadius by remember { mutableIntStateOf(store.cardCornerRadius) }
        var coverSize by remember { mutableIntStateOf(store.coverSize) }
        var animationsEnabled by remember { mutableStateOf(store.animationsEnabled) }
        var dynamicColor by remember { mutableStateOf(store.dynamicColor) }
        var isGridView by remember { mutableStateOf(store.isGridView) }
        var isCompactCard by remember { mutableStateOf(store.isCompactCard) }
        var autoSearchCovers by remember { mutableStateOf(store.autoSearchCovers) }
        var githubToken by remember { mutableStateOf(store.githubToken) }
        var showLoginDialog by remember { mutableStateOf(false) }
        var showProfileDialog by remember { mutableStateOf(false) }
        var loginEmail by remember { mutableStateOf("") }
        var loginPassword by remember { mutableStateOf("") }
        var loginPasswordConfirm by remember { mutableStateOf("") }
        var showLoginPassword by remember { mutableStateOf(false) }
        var showConfirmPassword by remember { mutableStateOf(false) }
        var loginDisplayName by remember { mutableStateOf("") }
        var isSignUpMode by remember { mutableStateOf(false) }
        var isAuthLoading by remember { mutableStateOf(false) }
        var authMessage by remember { mutableStateOf<String?>(null) }
        val authService = remember(context) { AuthService.create(context) }
        var authUser by remember(authService) { mutableStateOf(authService?.currentUser) }

        DisposableEffect(authService) {
            val listener = authService?.addAuthStateListener { authUser = it }
            onDispose {
                if (listener != null) authService?.removeAuthStateListener(listener)
            }
        }

        LaunchedEffect(accountActionRequest, authUser?.uid) {
            if (accountActionRequest > 0) {
                if (authUser?.isEmailVerified == true) {
                    showLoginDialog = false
                    showProfileDialog = true
                } else {
                    showProfileDialog = false
                    showLoginDialog = true
                }
            }
        }

        val lang = language

        fun authErrorMessage(exception: Exception?): String {
            return when ((exception as? FirebaseAuthException)?.errorCode) {
                "ERROR_INVALID_EMAIL" -> L10n.t("Formato de e-mail inválido. Confira o endereço e tente novamente.", "Invalid email format. Check the address and try again.", lang)
                "ERROR_OPERATION_NOT_ALLOWED" -> L10n.t("O login por e-mail ainda não está habilitado no Firebase.", "Email sign-in is not enabled in Firebase yet.", lang)
                "ERROR_TOO_MANY_REQUESTS" -> L10n.t("Muitas tentativas. Aguarde um pouco e tente novamente.", "Too many attempts. Wait a little and try again.", lang)
                "ERROR_NETWORK_REQUEST_FAILED" -> L10n.t("Falha de conexão. Verifique sua internet e tente novamente.", "Connection failed. Check your internet and try again.", lang)
                "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
                    L10n.t("E-mail ou senha incorretos.", "Email or password is incorrect.", lang)
                "ERROR_EMAIL_ALREADY_IN_USE" ->
                    L10n.t("Já existe uma conta com esse e-mail.", "An account already exists for this email.", lang)
                "ERROR_WEAK_PASSWORD" ->
                    L10n.t("Use uma senha com pelo menos 8 caracteres.", "Use a password with at least 8 characters.", lang)
                else -> L10n.t("Não foi possível concluir. Verifique a conexão e tente novamente.", "Could not complete the request. Check your connection and try again.", lang)
            }
        }

        fun saveAndApply() {
            store.language = language
            store.themeMode = themeMode
            store.colorAccent = colorAccent
            store.cardCornerRadius = cardCornerRadius
            store.coverSize = coverSize
            store.animationsEnabled = animationsEnabled
            store.dynamicColor = dynamicColor
            store.isGridView = isGridView
            store.isCompactCard = isCompactCard
            store.autoSearchCovers = autoSearchCovers
            store.githubToken = githubToken.trim()

            onApplyChanges()
            Toast.makeText(context, L10n.t("Mudanças aplicadas com sucesso!", "Changes applied successfully!", lang), Toast.LENGTH_SHORT).show()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 100.dp), // Extra bottom padding so FAB never covers options
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = L10n.t("Conta", "Account", lang),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = if (authUser == null) {
                                L10n.t("Entre na sua conta", "Sign in to your account", lang)
                            } else {
                                L10n.t("Conta conectada", "Account connected", lang)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = authUser?.email ?: L10n.t(
                                "Use e-mail e senha, com verificação de e-mail e recuperação de senha.",
                                "Sign in with email and password, including email verification and password recovery.",
                                lang,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(onClick = { showLoginDialog = true }) {
                            Text(
                                if (authUser == null) L10n.t("Fazer login", "Sign in", lang)
                                else L10n.t("Gerenciar conta", "Manage account", lang),
                            )
                        }
                        if (authUser != null) {
                            OutlinedButton(onClick = { showProfileDialog = true; showLoginDialog = false }) {
                                Text(L10n.t("Perfil e progresso", "Profile & progress", lang))
                            }
                            OutlinedButton(onClick = { authService?.signOut() }) {
                                Text(L10n.t("Sair", "Sign out", lang))
                            }
                        }
                    }
                }

                // Section 0: Idioma / Language
                Text(
                    text = L10n.t("Idioma / Language", "Language / Idioma", lang),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = language == 0, onClick = { language = 0 })
                            Text("Português (Brasil)", fontWeight = if (language == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = language == 1, onClick = { language = 1 })
                            Text("English (US)", fontWeight = if (language == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                // Section 1: Tema & Aparência
                Text(
                    text = L10n.t("Aparência & Cores", "Appearance & Colors", lang),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(L10n.t("Modo do Tema", "Theme Mode", lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = themeMode == 0, onClick = { themeMode = 0 })
                            Text(L10n.t("Sistema", "System", lang))

                            Spacer(modifier = Modifier.width(8.dp))

                            RadioButton(selected = themeMode == 1, onClick = { themeMode = 1 })
                            Text(L10n.t("Escuro", "Dark", lang))

                            Spacer(modifier = Modifier.width(8.dp))

                            RadioButton(selected = themeMode == 2, onClick = { themeMode = 2 })
                            Text(L10n.t("Claro", "Light", lang))
                        }

                        HorizontalDivider()

                        Text(L10n.t("Cor de Destaque", "Accent Color", lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = colorAccent == 0, onClick = { colorAccent = 0 })
                                Text(L10n.t("Padrão (Material You / Verdoso)", "Default (Material You / Greenish)", lang))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = colorAccent == 1, onClick = { colorAccent = 1 })
                                Text(L10n.t("Azul Royal", "Royal Blue", lang))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = colorAccent == 2, onClick = { colorAccent = 2 })
                                Text(L10n.t("Roxo Deep", "Deep Purple", lang))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = colorAccent == 3, onClick = { colorAccent = 3 })
                                Text(L10n.t("Verde Esmeralda", "Emerald Green", lang))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = colorAccent == 4, onClick = { colorAccent = 4 })
                                Text(L10n.t("Vermelho Carmim", "Carmine Red", lang))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = colorAccent == 5, onClick = { colorAccent = 5 })
                                Text(L10n.t("Preto OLED + Bugdroid Green", "OLED Pitch Black + Bugdroid Green", lang))
                            }
                        }

                        HorizontalDivider()

                        Text(L10n.t("Formato dos Cards", "Card Shape", lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = cardCornerRadius == 0, onClick = { cardCornerRadius = 0 })
                            Text(L10n.t("Arredondado", "Rounded", lang))

                            Spacer(modifier = Modifier.width(8.dp))

                            RadioButton(selected = cardCornerRadius == 1, onClick = { cardCornerRadius = 1 })
                            Text(L10n.t("Muito Curvo", "Very Curved", lang))

                            Spacer(modifier = Modifier.width(8.dp))

                            RadioButton(selected = cardCornerRadius == 2, onClick = { cardCornerRadius = 2 })
                            Text(L10n.t("Reto", "Square", lang))
                        }

                        HorizontalDivider()

                        Text(L10n.t("Tamanho das capas", "Cover size", lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = coverSize == 0,
                                onClick = { coverSize = 0 },
                                label = { Text(L10n.t("Pequena", "Small", lang)) },
                            )
                            FilterChip(
                                selected = coverSize == 1,
                                onClick = { coverSize = 1 },
                                label = { Text(L10n.t("Média", "Medium", lang)) },
                            )
                            FilterChip(
                                selected = coverSize == 2,
                                onClick = { coverSize = 2 },
                                label = { Text(L10n.t("Grande", "Large", lang)) },
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(L10n.t("Animações da interface", "Interface animations", lang), fontWeight = FontWeight.SemiBold)
                                Text(
                                    L10n.t("Transições suaves e resposta animada dos favoritos", "Smooth transitions and animated favorite feedback", lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(checked = animationsEnabled, onCheckedChange = { animationsEnabled = it })
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(L10n.t("Cores Dinâmicas (Monet)", "Dynamic Colors (Monet)", lang), fontWeight = FontWeight.SemiBold)
                                    Text(L10n.t("Usa as cores do papel de parede do seu Android", "Uses colors from your Android wallpaper", lang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = dynamicColor, onCheckedChange = { dynamicColor = it })
                            }
                        }
                    }
                }

                // Section 2: Layout & Visualização
                Text(
                    text = L10n.t("Layout & Exibição", "Layout & Display", lang),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(L10n.t("Visualização em Grade (Grid)", "Grid View", lang), fontWeight = FontWeight.SemiBold)
                                Text(L10n.t("Exibe os jogos em 2 colunas com capas em destaque", "Displays games in 2 columns with prominent covers", lang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = isGridView, onCheckedChange = { isGridView = it })
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(L10n.t("Modo Card Compacto", "Compact Card Mode", lang), fontWeight = FontWeight.SemiBold)
                                Text(L10n.t("Reduz o tamanho dos cards na lista", "Reduces card height in the list", lang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = isCompactCard, onCheckedChange = { isCompactCard = it })
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(L10n.t("Buscar Capas dos Sites Originais", "Fetch Covers from Original Sources", lang), fontWeight = FontWeight.SemiBold)
                                Text(L10n.t("Sincroniza capas oficiais e banners dos projetos", "Syncs official project covers and banners", lang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = autoSearchCovers, onCheckedChange = { autoSearchCovers = it })
                        }
                    }
                }

                // Section 3: GitHub & APKs
                Text(
                    text = L10n.t("GitHub & Armazenamento", "GitHub & Storage", lang),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(L10n.t("GitHub Personal Access Token (Opcional)", "GitHub Personal Access Token (Optional)", lang), fontWeight = FontWeight.SemiBold)
                        Text(
                            text = L10n.t("Evita o limite de requisições da API pública do GitHub para downloads de releases.", "Prevents public GitHub API rate limiting during release downloads.", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        OutlinedTextField(
                            value = githubToken,
                            onValueChange = { githubToken = it },
                            label = { Text("ghp_xxxxxxxxxxxxxx") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        HorizontalDivider()

                        OutlinedButton(
                            onClick = {
                                val ok = store.clearApkCache(context)
                                Toast.makeText(context, if (ok) L10n.t("Cache de APKs limpo!", "APK cache cleared!", lang) else L10n.t("Nenhum arquivo no cache", "No cached files found", lang), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(L10n.t("Limpar Cache de APKs Baixados", "Clear Downloaded APK Cache", lang))
                        }
                    }
                }

                // Section 4: Sobre
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("RecompDroid v0.0.2", fontWeight = FontWeight.Bold)
                        Text(
                            text = L10n.t("Launcher nativo para recompilações e ports de console no Android. Capas dos sites oficiais, repositórios de alta fidelidade e instalação direta de APKs do GitHub.", "Native launcher for console recompilations and ports on Android. Official covers, high-fidelity repositories, and one-tap APK installation from GitHub.", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (showLoginDialog) {
                AlertDialog(
                    onDismissRequest = {
                        if (!isAuthLoading) showLoginDialog = false
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.padding(10.dp).size(24.dp),
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    if (authUser == null) {
                                        if (isSignUpMode) L10n.t("Criar conta", "Create account", lang)
                                        else L10n.t("Bem-vindo de volta", "Welcome back", lang)
                                    } else L10n.t("Sua conta", "Your account", lang),
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    if (isSignUpMode && authUser == null)
                                        L10n.t("Crie sua conta RecompDroid", "Create your RecompDroid account", lang)
                                    else L10n.t("Acesse sua conta RecompDroid", "Access your RecompDroid account", lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            when {
                                authService == null -> {
                                    Text(
                                        L10n.t(
                                            "O Firebase ainda não está conectado a este app. A configuração do projeto é necessária para habilitar o login.",
                                            "Firebase is not connected to this app yet. Project configuration is required to enable sign-in.",
                                            lang,
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }

                                authUser != null -> {
                                    val user = authUser!!
                                    Text(user.displayName ?: user.email.orEmpty(), fontWeight = FontWeight.SemiBold)
                                    OutlinedButton(
                                        onClick = { showProfileDialog = true; showLoginDialog = false },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) { Text(L10n.t("Editar perfil e progresso", "Edit profile & progress", lang)) }
                                    Text(
                                        if (user.isEmailVerified) {
                                            L10n.t("E-mail verificado", "Email verified", lang)
                                        } else {
                                            L10n.t("Confirme seu e-mail para concluir o cadastro.", "Verify your email to complete registration.", lang)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (!user.isEmailVerified) {
                                        OutlinedButton(
                                            enabled = !isAuthLoading,
                                            onClick = {
                                                isAuthLoading = true
                                                authService.sendVerificationEmail(user)
                                                    .addOnCompleteListener { task ->
                                                        isAuthLoading = false
                                                        authMessage = if (task.isSuccessful) {
                                                            L10n.t("Novo e-mail de verificação enviado.", "A new verification email was sent.", lang)
                                                        } else {
                                                            authErrorMessage(task.exception)
                                                        }
                                                    }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text(L10n.t("Reenviar verificação", "Resend verification", lang))
                                        }
                                        OutlinedButton(
                                            enabled = !isAuthLoading,
                                            onClick = {
                                                isAuthLoading = true
                                                authService.reload(user)
                                                    .addOnCompleteListener { task ->
                                                        isAuthLoading = false
                                                        val refreshedUser = authService.currentUser
                                                        authUser = refreshedUser
                                                        if (task.isSuccessful && refreshedUser?.isEmailVerified == true) {
                                                            authMessage = L10n.t("E-mail confirmado com sucesso.", "Email verified successfully.", lang)
                                                            showLoginDialog = false
                                                        } else {
                                                            authMessage = if (task.isSuccessful) {
                                                                L10n.t("Ainda não encontramos a confirmação. Tente novamente em instantes.", "We could not find the verification yet. Try again shortly.", lang)
                                                            } else {
                                                                authErrorMessage(task.exception)
                                                            }
                                                        }
                                                    }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text(L10n.t("Já confirmei meu e-mail", "I verified my email", lang))
                                        }
                                    }
                                    OutlinedButton(
                                        enabled = !isAuthLoading,
                                        onClick = {
                                            authService.signOut()
                                            showLoginDialog = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(L10n.t("Sair da conta", "Sign out", lang))
                                    }
                                }

                                else -> {
                                    if (isSignUpMode) {
                                        OutlinedTextField(
                                            value = loginDisplayName,
                                            onValueChange = { loginDisplayName = it },
                                            label = { Text(L10n.t("Nome", "Name", lang)) },
                                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !isAuthLoading,
                                        )
                                    }
                                    OutlinedTextField(
                                        value = loginEmail,
                                        onValueChange = { loginEmail = it.trimStart() },
                                        label = { Text(L10n.t("Endereço de e-mail", "Email address", lang)) },
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Email,
                                            capitalization = KeyboardCapitalization.None,
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isAuthLoading,
                                    )
                                    OutlinedTextField(
                                        value = loginPassword,
                                        onValueChange = { loginPassword = it },
                                        label = { Text(L10n.t("Senha", "Password", lang)) },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                        trailingIcon = {
                                            TextButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                                Text(
                                                    if (showLoginPassword) L10n.t("Ocultar", "Hide", lang) else L10n.t("Mostrar", "Show", lang),
                                                    style = MaterialTheme.typography.labelSmall,
                                                )
                                            }
                                        },
                                        visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isAuthLoading,
                                    )
                                    if (isSignUpMode) {
                                        OutlinedTextField(
                                            value = loginPasswordConfirm,
                                            onValueChange = { loginPasswordConfirm = it },
                                            label = { Text(L10n.t("Confirmar senha", "Confirm password", lang)) },
                                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                            trailingIcon = {
                                                TextButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                                    Text(
                                                        if (showConfirmPassword) L10n.t("Ocultar", "Hide", lang) else L10n.t("Mostrar", "Show", lang),
                                                        style = MaterialTheme.typography.labelSmall,
                                                    )
                                                }
                                            },
                                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !isAuthLoading,
                                        )
                                    }
                                    authMessage?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    if (!isSignUpMode) {
                                        TextButton(
                                            enabled = !isAuthLoading,
                                            onClick = {
                                                val email = loginEmail.trim()
                                                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                                    authMessage = L10n.t("Informe um e-mail válido.", "Enter a valid email address.", lang)
                                                } else {
                                                    isAuthLoading = true
                                                    authMessage = null
                                                    authService.sendPasswordReset(email)
                                                        .addOnCompleteListener { task ->
                                                            isAuthLoading = false
                                                            authMessage = if (task.isSuccessful) {
                                                                L10n.t(
                                                                    "Se houver uma conta para esse e-mail, você receberá um link para redefinir a senha.",
                                                                    "If an account exists for this email, you will receive a password reset link.",
                                                                    lang,
                                                                )
                                                            } else {
                                                                authErrorMessage(task.exception)
                                                            }
                                                        }
                                                }
                                            },
                                        ) {
                                            Text(L10n.t("Esqueci minha senha", "Forgot password", lang))
                                        }
                                    }
                                    TextButton(
                                        enabled = !isAuthLoading,
                                        onClick = {
                                            isSignUpMode = !isSignUpMode
                                            authMessage = null
                                        },
                                    ) {
                                        Text(
                                            if (isSignUpMode) {
                                                L10n.t("Já tenho uma conta", "I already have an account", lang)
                                            } else {
                                                L10n.t("Criar uma conta", "Create an account", lang)
                                            },
                                        )
                                    }
                                }
                            }

                            authMessage?.takeIf { authUser != null }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    },
                    confirmButton = {
                        if (authUser == null) {
                            Button(
                                enabled = authService != null && !isAuthLoading,
                                onClick = {
                                    val email = loginEmail.trim()
                                    when {
                                        authService == null -> Unit
                                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                            authMessage = L10n.t("Informe um e-mail válido.", "Enter a valid email address.", lang)
                                        }
                                        loginPassword.length < 8 -> {
                                            authMessage = L10n.t("Use uma senha com pelo menos 8 caracteres.", "Use a password with at least 8 characters.", lang)
                                        }
                                        isSignUpMode && loginDisplayName.isBlank() -> {
                                            authMessage = L10n.t("Informe seu nome.", "Enter your name.", lang)
                                        }
                                        isSignUpMode && loginPassword != loginPasswordConfirm -> {
                                            authMessage = L10n.t("As senhas não coincidem.", "Passwords do not match.", lang)
                                        }
                                        else -> {
                                            isAuthLoading = true
                                            authMessage = null
                                            val request = if (isSignUpMode) {
                                                authService.createAccount(email, loginPassword)
                                            } else {
                                                authService.signIn(email, loginPassword)
                                            }
                                            request.addOnCompleteListener { task ->
                                                isAuthLoading = false
                                                if (!task.isSuccessful) {
                                                    authMessage = authErrorMessage(task.exception)
                                                } else {
                                                    val user = authService.currentUser
                                                    if (user == null) {
                                                        authMessage = L10n.t("Não foi possível carregar a sessão.", "Could not load the account session.", lang)
                                                    } else if (isSignUpMode) {
                                                        authService.updateDisplayName(user, loginDisplayName.trim())
                                                            .addOnCompleteListener {
                                                                authService.sendVerificationEmail(user)
                                                                    .addOnCompleteListener { verification ->
                                                                        authMessage = if (verification.isSuccessful) {
                                                                            L10n.t("Conta criada. Confira seu e-mail para verificá-la.", "Account created. Check your email to verify it.", lang)
                                                                        } else {
                                                                            authErrorMessage(verification.exception)
                                                                        }
                                                                    }
                                                            }
                                                    } else if (user.isEmailVerified) {
                                                        showLoginDialog = false
                                                    } else {
                                                        authService.sendVerificationEmail(user)
                                                            .addOnCompleteListener {
                                                                authMessage = L10n.t(
                                                                    "Confirme seu e-mail. Enviamos um novo link de verificação.",
                                                                    "Verify your email. We sent a new verification link.",
                                                                    lang,
                                                                )
                                                            }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                            ) {
                                Text(
                                    if (isAuthLoading) L10n.t("Aguarde…", "Please wait…", lang)
                                    else if (isSignUpMode) L10n.t("Criar conta", "Create account", lang)
                                    else L10n.t("Entrar", "Sign in", lang),
                                )
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            enabled = !isAuthLoading,
                            onClick = {
                                showLoginDialog = false
                                authMessage = null
                            },
                        ) {
                            Text(L10n.t("Fechar", "Close", lang))
                        }
                    },
                )
            }

            if (showProfileDialog && authUser != null) {
                ProfileEditorDialog(
                    user = authUser!!,
                    lang = lang,
                    onDismiss = { showProfileDialog = false },
                )
            }

            // BOTÃO DE APLICAR MUDANÇAS FIXO AO CANTO INFERIOR DIREITO
            ExtendedFloatingActionButton(
                onClick = { saveAndApply() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                icon = { Icon(Icons.Default.Check, contentDescription = null) },
                text = { Text(L10n.t("Aplicar Mudanças", "Apply Changes", lang), fontWeight = FontWeight.Bold) },
            )
        }
    }

    @Composable
    fun ProfileEditorDialog(
        user: FirebaseUser,
        lang: Int,
        onDismiss: () -> Unit,
    ) {
        val context = LocalContext.current
        val cloud = remember { CloudProfileStore() }
        var displayName by remember(user.uid) { mutableStateOf(user.displayName.orEmpty()) }
        var username by remember(user.uid) { mutableStateOf("") }
        var photoBase64 by remember(user.uid) { mutableStateOf<String?>(null) }
        var progress by remember(user.uid) { mutableStateOf<List<CloudGameProgress>>(emptyList()) }
        var message by remember { mutableStateOf<String?>(null) }
        var loading by remember { mutableStateOf(true) }
        var saving by remember { mutableStateOf(false) }

        val photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.GetContent(),
        ) { uri ->
            if (uri != null) {
                runCatching {
                    val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri).use {
                        android.graphics.BitmapFactory.decodeStream(it, null, bounds)
                    }
                    val sample = maxOf(1, minOf(bounds.outWidth, bounds.outHeight) / 512)
                    val source = context.contentResolver.openInputStream(uri).use {
                        requireNotNull(it) { "Could not open selected image" }
                        android.graphics.BitmapFactory.decodeStream(
                            it,
                            null,
                            android.graphics.BitmapFactory.Options().apply { inSampleSize = sample },
                        )
                    } ?: error("Unsupported image")
                    val scaled = android.graphics.Bitmap.createScaledBitmap(source, 256, 256, true)
                    val bytes = java.io.ByteArrayOutputStream().use { output ->
                        scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 78, output)
                        output.toByteArray()
                    }
                    photoBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                }.onFailure { message = L10n.t("Não foi possível carregar essa imagem.", "Could not load that image.", lang) }
            }
        }

        LaunchedEffect(user.uid) {
            loading = true
            cloud.loadProfile(user.uid) { data, error ->
                if (data != null) {
                    displayName = (data["displayName"] as? String) ?: user.displayName.orEmpty()
                    username = data["username"] as? String ?: ""
                    photoBase64 = data["photoBase64"] as? String
                }
                if (error != null) {
                    message = L10n.t("Sincronização indisponível. Ative o Firestore no Firebase para sincronizar.", "Sync unavailable. Enable Firestore in Firebase to sync.", lang)
                }
                cloud.loadProgress(user.uid) { rows, progressError ->
                    progress = rows.sortedByDescending { it.playSeconds }
                    if (progressError != null) {
                        message = L10n.t("Não foi possível carregar o progresso da nuvem.", "Could not load cloud progress.", lang)
                    }
                    loading = false
                }
            }
        }

        AlertDialog(
            onDismissRequest = { if (!saving) onDismiss() },
            title = { Text(L10n.t("Perfil e progresso", "Profile & progress", lang), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val imageData = photoBase64
                    if (!imageData.isNullOrBlank()) {
                        AsyncImage(
                            model = "data:image/jpeg;base64,$imageData",
                            contentDescription = L10n.t("Foto do perfil", "Profile photo", lang),
                            modifier = Modifier.size(76.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(18.dp).size(40.dp))
                        }
                    }
                    OutlinedButton(onClick = { photoPicker.launch("image/*") }, enabled = !saving) {
                        Text(L10n.t("Escolher foto", "Choose photo", lang))
                    }
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it.take(60) },
                        label = { Text(L10n.t("Nome de exibição", "Display name", lang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.filter { c -> c.isLetterOrDigit() || c == '_' || c == '.' }.take(24) },
                        label = { Text("@username") },
                        supportingText = { Text(L10n.t("3 a 24 letras, números, ponto ou _", "3–24 letters, numbers, dot, or _", lang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(L10n.t("Progresso sincronizado", "Synced progress", lang), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    val totalSeconds = progress.sumOf { it.playSeconds }
                    Text(
                        L10n.t("Total: ${totalSeconds / 3600} h ${totalSeconds % 3600 / 60} min · ${progress.size} jogos", "Total: ${totalSeconds / 3600} h ${totalSeconds % 3600 / 60} min · ${progress.size} games", lang),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    progress.take(8).forEach { game ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(game.title, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${game.playSeconds / 3600}h ${game.playSeconds % 3600 / 60}m", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    message?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = {
                Button(
                    enabled = !saving && displayName.isNotBlank() && username.length in 3..24,
                    onClick = {
                        saving = true
                        message = null
                        val trimmedName = displayName.trim()
                        user.updateProfile(com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(trimmedName).build())
                            .addOnCompleteListener { authTask ->
                                if (!authTask.isSuccessful) {
                                    saving = false
                                    message = L10n.t("Não foi possível atualizar o nome da conta.", "Could not update account name.", lang)
                                } else {
                                    cloud.saveProfile(user.uid, trimmedName, username.trim(), photoBase64) { error ->
                                        saving = false
                                        if (error == null) onDismiss()
                                        else message = L10n.t("Falha ao salvar na nuvem. Ative o Firestore e tente novamente.", "Cloud save failed. Enable Firestore and try again.", lang)
                                    }
                                }
                            }
                    },
                ) { Text(if (saving) L10n.t("Salvando…", "Saving…", lang) else L10n.t("Salvar", "Save", lang)) }
            },
            dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text(L10n.t("Fechar", "Close", lang)) } },
        )
    }

    @Composable
    fun CoverSearchDialog(
        gameTitle: String,
        currentUrl: String?,
        onCoverSelected: (String) -> Unit,
        onDismiss: () -> Unit,
    ) {
        val scope = rememberCoroutineScope()
        var customUrlInput by remember { mutableStateOf(currentUrl ?: "") }
        var isSearching by remember { mutableStateOf(false) }
        var searchedUrl by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Buscar Capa na Internet", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Pesquise automaticamente pelo título ou insira a URL direta da imagem:")

                    Button(
                        onClick = {
                            isSearching = true
                            scope.launch {
                                val found = CoverFetcher.searchCoverUrl(gameTitle)
                                if (found != null) {
                                    searchedUrl = found
                                    customUrlInput = found
                                }
                                isSearching = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buscar no Wikimedia / Wikipedia")
                    }

                    if (isSearching) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    searchedUrl?.let { url ->
                        Text("Capa Encontrada:", style = MaterialTheme.typography.labelMedium)
                        AsyncImage(
                            model = url,
                            contentDescription = "Capa encontrada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }

                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        label = { Text("URL da Imagem de Capa") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customUrlInput.isNotBlank()) {
                            onCoverSelected(customUrlInput.trim())
                        }
                    },
                ) {
                    Text("Salvar Capa")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            },
        )
    }

    private fun getCardShape(cornerType: Int): Dp = when (cornerType) {
        1 -> 24.dp
        2 -> 6.dp
        else -> 16.dp
    }
}
