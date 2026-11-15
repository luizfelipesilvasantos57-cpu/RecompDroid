package com.recompdroid

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.compose.AsyncImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicReference

object L10n {
    fun t(pt: String, en: String, lang: Int): String = if (lang == 1) en else pt
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val store = remember { Store(this) }
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
                            surface = if (isOled) Color(0xFF070B12) else Color(0xFF1E293B),
                            surfaceVariant = if (isOled) Color(0xFF131A27) else Color(0xFF283548),
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

        var tab by remember { mutableIntStateOf(0) } // 0: Discover, 1: Platforms, 2: Installed, 3: Settings
        var games by remember { mutableStateOf<List<Game>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var query by remember { mutableStateOf("") }
        var onlyFavorites by remember { mutableStateOf(false) }
        var selectedPlatformFilter by remember { mutableStateOf<String?>(null) }
        var selectedGame by remember { mutableStateOf<Game?>(null) }
        var pendingInstallGame by remember { mutableStateOf<Game?>(null) }
        var rev by remember { mutableIntStateOf(0) }

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Centered & Modernized App Icon Badge
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_bugdroid_gamepad),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.Unspecified,
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = when (tab) {
                                        1 -> L10n.t("Plataformas", "Platforms", lang)
                                        2 -> L10n.t("Instalados", "Installed", lang)
                                        3 -> L10n.t("Ajustes", "Settings", lang)
                                        else -> "RecompDroid"
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                )
                                if (tab == 0 && games.isNotEmpty()) {
                                    Text(
                                        text = "${games.size} ${L10n.t("recomps catalogados (A-Z)", "recomps cataloged (A-Z)", lang)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        }
                        if (tab != 3) {
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
                    3 -> SettingsScreen(store = store, onApplyChanges = onThemeApply)
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
                                val matchesPlat = if (selectedPlatformFilter != null) platform == selectedPlatformFilter else true
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
                                    placeholder = { Text(L10n.t("Buscar recompilações e jogos...", "Search recompilations & games...", lang)) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = {
                                        if (query.isNotEmpty()) {
                                            IconButton(onClick = { query = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Limpar")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(20.dp),
                                )

                                // Platform filter chips row with extended consoles
                                val platformOptions = remember(games) {
                                    Catalog.sortPlatforms(games.mapNotNull { it.platform })
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
                                        )
                                    }
                                    items(platformOptions) { plat ->
                                        FilterChip(
                                            selected = selectedPlatformFilter == plat,
                                            onClick = {
                                                selectedPlatformFilter = if (selectedPlatformFilter == plat) null else plat
                                            },
                                            label = { Text(plat) },
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
                                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    items(filteredList, key = { it.slug }) { g ->
                                        GameGridCard(
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
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
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
        val isPkgInstalled = remember(game.slug, installedTag) { store.isAppInstalled(game.slug) }

        ElevatedCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
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
                            width = if (store.isCompactCard) 68.dp else 84.dp,
                            height = if (store.isCompactCard) 92.dp else 114.dp,
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
                            text = game.title + if (game.recomps.size > 1) " (${game.recomps.size} recomps)" else "",
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
                                modifier = Modifier.size(18.dp),
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
                                            context.startActivity(it)
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

    /**
     * Modernized GameGridCard showing game cover at the top,
     * details, and "Instalar Recomp" button at the bottom.
     */
    @Composable
    fun GameGridCard(
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
        val isPkgInstalled = remember(game.slug, installedTag) { store.isAppInstalled(game.slug) }

        ElevatedCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
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
                        .aspectRatio(16f / 11f)
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
                            modifier = Modifier.size(16.dp),
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White,
                        )
                    }

                    // Platform tag overlay
                    game.platform?.let { plat ->
                        Surface(
                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            modifier = Modifier.align(Alignment.TopStart),
                        ) {
                            Text(
                                text = plat,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = game.title + if (game.recomps.size > 1) " (${game.recomps.size} recomps)" else "",
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
                    } else if (isPkgInstalled) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    val pkg = store.pkg(game.slug)
                                    if (pkg != null) {
                                        context.packageManager.getLaunchIntentForPackage(pkg)?.let {
                                            context.startActivity(it)
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth().height(32.dp),
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(L10n.t("Jogar", "Play", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onInstallClick,
                                modifier = Modifier.fillMaxWidth().height(30.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(L10n.t("Escolher recomp", "Choose recomp", lang), fontSize = 10.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = onInstallClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                        ) {
                            Text(L10n.t("Instalar Recomp", "Install Recomp", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    title = { Text(g.title + if (g.recomps.size > 1) " (${g.recomps.size} recomps)" else "", maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold) },
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

                Text(g.title + if (g.recomps.size > 1) " (${g.recomps.size} recomps)" else "", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)

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
                                onClick = { context.startActivity(intent) },
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
    fun SettingsScreen(store: Store, onApplyChanges: () -> Unit) {
        val context = LocalContext.current
        var language by remember { mutableIntStateOf(store.language) }
        var themeMode by remember { mutableIntStateOf(store.themeMode) }
        var colorAccent by remember { mutableIntStateOf(store.colorAccent) }
        var cardCornerRadius by remember { mutableIntStateOf(store.cardCornerRadius) }
        var dynamicColor by remember { mutableStateOf(store.dynamicColor) }
        var isGridView by remember { mutableStateOf(store.isGridView) }
        var isCompactCard by remember { mutableStateOf(store.isCompactCard) }
        var autoSearchCovers by remember { mutableStateOf(store.autoSearchCovers) }
        var githubToken by remember { mutableStateOf(store.githubToken) }

        val lang = language

        fun saveAndApply() {
            store.language = language
            store.themeMode = themeMode
            store.colorAccent = colorAccent
            store.cardCornerRadius = cardCornerRadius
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
                        Text("RecompDroid v0.1", fontWeight = FontWeight.Bold)
                        Text(
                            text = L10n.t("Launcher nativo para recompilações e ports de console no Android. Capas dos sites oficiais, repositórios de alta fidelidade e instalação direta de APKs do GitHub.", "Native launcher for console recompilations and ports on Android. Official covers, high-fidelity repositories, and one-tap APK installation from GitHub.", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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
