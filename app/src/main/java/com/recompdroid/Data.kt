package com.recompdroid

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.Normalizer
import java.util.Locale

data class RecompOption(
    val slug: String,
    val project: String?,
    val repo: String?,
    val version: String?,
)

data class Game(
    val slug: String,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val platform: String? = null,
    val coverUrl: String? = null,
    val author: String? = null,
    val status: String? = "Playable",
    val repo: String? = null,
    val version: String? = null,
    val recomps: List<RecompOption> = emptyList(),
)

data class Release(
    val tag: String,
    val name: String,
    val url: String,
    val size: Long,
    val sha256: String? = null,
)

private const val SITE = "https://decompbuddy.com"
private const val CATALOG_URL = "https://tanookistudios.com/decomp-buddy/catalog.json"

internal fun openConnection(url: String, token: String? = null): HttpURLConnection =
    (URL(url).openConnection() as HttpURLConnection).apply {
        setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) RecompDroid/0.1")
        if (url.contains("api.github.com")) {
            setRequestProperty("Accept", "application/vnd.github+json, application/json")
            if (!token.isNullOrBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
            }
        }
        connectTimeout = 20000
        readTimeout = 30000
    }

private fun get(url: String, token: String? = null) = openConnection(url, token).inputStream.bufferedReader().use { it.readText() }

/**
 * Registry mapping recompilation games and console ports to official Android GitHub repositories and original hero media.
 */
object PortRegistry {
    data class PortInfo(
        val repo: String,
        val coverUrl: String,
        val defaultPackageName: String? = null,
        val console: String = "Nintendo 64",
        val notes: String? = null,
    )

    private val knownPorts = mapOf(
        // Nintendo 64
        "shipwright" to PortInfo(
            repo = "Waterdish/Shipwright-Android",
            coverUrl = "https://recompstore.com/media/pc-shipwright-hero_web-a676439f.jpg",
            defaultPackageName = "com.shipwright.soh",
            console = "Nintendo 64",
            notes = "Ship of Harkinian (Ocarina of Time)",
        ),
        "2ship2harkinian" to PortInfo(
            repo = "Waterdish/2ship2harkinian-Android",
            coverUrl = "https://recompstore.com/media/pc-2ship2harkinian-hero_web-3da2845c.jpg",
            defaultPackageName = "com.shipwright.twoship",
            console = "Nintendo 64",
            notes = "2 Ship 2 Harkinian (Majora's Mask)",
        ),
        "ghostship" to PortInfo(
            repo = "retired64/sm64cdpy.releases",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co20au.jpg",
            defaultPackageName = "com.retired64.sm64cdpy",
            console = "Nintendo 64",
            notes = "Super Mario 64 Native Recomp",
        ),
        "banjorecomp" to PortInfo(
            repo = "Waterdish/Banjo-Kazooie-Recompiled-Android",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1x7c.jpg",
            defaultPackageName = "com.recomp.banjo",
            console = "Nintendo 64",
            notes = "Banjo-Kazooie: Recompiled",
        ),
        "spaghettikart" to PortInfo(
            repo = "Waterdish/mk64-android",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1x76.jpg",
            defaultPackageName = "com.spaghetti.kart64",
            console = "Nintendo 64",
            notes = "SpaghettiKart (Mario Kart 64)",
        ),
        "zelda64recomp" to PortInfo(
            repo = "Zelda64Recomp/Zelda64Recomp",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co5vmg.jpg",
            defaultPackageName = "com.zelda64.recomp",
            console = "Nintendo 64",
            notes = "Zelda 64 Recomp (OoT & MM)",
        ),

        // PlayStation (PS1)
        "yugioh-fm-recomp" to PortInfo(
            repo = "Unchiga/Yu-Gi-Oh-Forbidden-Memories-Recompiled",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1r75.jpg",
            defaultPackageName = "com.unchiga.yfm",
            console = "PlayStation",
            notes = "Yu-Gi-Oh! Forbidden Memories Recompiled",
        ),

        // PlayStation 2 (PS2)
        "dmc3-recomp" to PortInfo(
            repo = "sindufha/Devil-may-cry-3-recomp",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1r78.jpg",
            defaultPackageName = "local.dmc3.runtimeprobe",
            console = "PlayStation 2",
            notes = "Devil May Cry 3 Recompilation",
        ),

        // PlayStation Vita (PS Vita)
        "vita3k" to PortInfo(
            repo = "Vita3K/Vita3K-Android",
            coverUrl = "https://raw.githubusercontent.com/Vita3K/Vita3K-Android/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            defaultPackageName = "org.vita3k.emulator",
            console = "PS Vita",
            notes = "Vita3K Native Recomp Core",
        ),

        // Sega Dreamcast
        "flycast" to PortInfo(
            repo = "flyinghead/flycast",
            coverUrl = "https://raw.githubusercontent.com/flyinghead/flycast/master/shell/android-studio/flycast/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            defaultPackageName = "com.flyinghead.flycast",
            console = "Dreamcast",
            notes = "Flycast Native Dreamcast",
        ),

        // Nintendo DS
        "melonds" to PortInfo(
            repo = "rafaelvcaetano/melonDS-android",
            coverUrl = "https://raw.githubusercontent.com/rafaelvcaetano/melonDS-android/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            defaultPackageName = "me.magnum.melonds",
            console = "Nintendo DS",
            notes = "melonDS Native Core",
        ),

        // Original Xbox / PC
        "openmw" to PortInfo(
            repo = "xyzz/openmw-android",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1x74.jpg",
            defaultPackageName = "is.xyz.omw",
            console = "Original Xbox",
            notes = "The Elder Scrolls III: Morrowind (OpenMW)",
        ),

        // PC / Classic Engines
        "devilutionx" to PortInfo(
            repo = "diasurgical/devilutionX",
            coverUrl = "https://raw.githubusercontent.com/diasurgical/devilutionX/master/Packaging/android/res/mipmap-xxxhdpi/ic_launcher.png",
            defaultPackageName = "org.diasurgical.devilutionx",
            console = "PC / Consoles",
            notes = "DevilutionX (Diablo 1 + Hellfire)",
        ),
        "fallout1-ce" to PortInfo(
            repo = "alexbatalov/fallout1-ce",
            coverUrl = "https://raw.githubusercontent.com/alexbatalov/fallout1-ce/main/res/icon.png",
            defaultPackageName = "com.alexbatalov.fallout1ce",
            console = "PC / Consoles",
            notes = "Fallout 1 Community Edition",
        ),
        "fallout2-ce" to PortInfo(
            repo = "alexbatalov/fallout2-ce",
            coverUrl = "https://raw.githubusercontent.com/alexbatalov/fallout2-ce/main/res/icon.png",
            defaultPackageName = "com.alexbatalov.fallout2ce",
            console = "PC / Consoles",
            notes = "Fallout 2 Community Edition",
        ),
        "com-n0n3m4-diii4a" to PortInfo(
            repo = "glKarin/com.n0n3m4.diii4a",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1vba.jpg",
            defaultPackageName = "com.n0n3m4.diii4a",
            console = "PC / Consoles",
            notes = "idTech4A++ (DOOM 3 / Quake 4)",
        ),
    )

    fun findPort(slug: String, title: String): PortInfo? {
        val s = slug.lowercase().trim()
        val t = title.lowercase().trim()

        knownPorts[s]?.let { return it }

        for ((key, info) in knownPorts) {
            if (s.contains(key) || key.contains(s)) return info
        }

        return when {
            t.contains("ocarina") || t.contains("ship of harkinian") -> knownPorts["shipwright"]
            t.contains("majora") || t.contains("2 ship") -> knownPorts["2ship2harkinian"]
            t.contains("mario 64") || t.contains("sm64") || t.contains("ghostship") -> knownPorts["ghostship"]
            t.contains("banjo") -> knownPorts["banjorecomp"]
            t.contains("mario kart") || t.contains("spaghettikart") -> knownPorts["spaghettikart"]
            t.contains("yu-gi-oh") || t.contains("yugioh") || t.contains("forbidden memories") -> knownPorts["yugioh-fm-recomp"]
            t.contains("devil may cry") || t.contains("dmc") -> knownPorts["dmc3-recomp"]
            t.contains("morrowind") || t.contains("openmw") || t.contains("elder scrolls") -> knownPorts["openmw"]
            t.contains("vita3k") || t.contains("vita") -> knownPorts["vita3k"]
            t.contains("flycast") || t.contains("dreamcast") -> knownPorts["flycast"]
            t.contains("melonds") || t.contains("nintendo ds") -> knownPorts["melonds"]
            t.contains("diablo") || t.contains("devilution") -> knownPorts["devilutionx"]
            t.contains("fallout 2") -> knownPorts["fallout2-ce"]
            t.contains("fallout") -> knownPorts["fallout1-ce"]
            t.contains("doom 3") || t.contains("quake 4") || t.contains("idtech") -> knownPorts["com-n0n3m4-diii4a"]
            t.contains("zelda") -> knownPorts["zelda64recomp"]
            else -> null
        }
    }
}

object Catalog {
    private fun normalizeCategory(category: String): String =
        category.lowercase(Locale.ROOT)
            .replace(Regex("""[^a-z0-9]+"""), " ")
            .trim()
            .replace(Regex("""\s+"""), " ")

    private fun isExcludedCategory(category: String): Boolean {
        val normalized = normalizeCategory(category)
        return normalized.startsWith("sega chihiro") ||
            normalized.startsWith("apple ipod classic") ||
            normalized.startsWith("ibm pc") ||
            normalized.startsWith("apple macintosh") ||
            normalized.startsWith("commodore 64") ||
            normalized.contains("mobile game pc port") ||
            normalized == "amiga" ||
            normalized.startsWith("amiga ") ||
            normalized.startsWith("adobe flash")
    }

    private fun gameGroupKey(title: String): String {
        val baseTitle = title
            .replace(Regex("""\s*\([^)]*(?:android|recomp|decomp|port|native|windows|linux|macos)[^)]*\)\s*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*\[[^\]]*(?:android|recomp|decomp|port|native|windows|linux|macos)[^\]]*\]\s*$""", RegexOption.IGNORE_CASE), "")
        return Normalizer.normalize(baseTitle.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("""\p{M}+"""), "")
            .replace(Regex("""[^a-z0-9]+"""), " ")
            .trim()
    }

    suspend fun games(): List<Game> = withContext(Dispatchers.IO) {
        val catalog = JSONObject(get(CATALOG_URL))
        val entries = catalog.optJSONArray("games") ?: return@withContext emptyList()

        (0 until entries.length()).mapNotNull { index ->
            val entry = entries.optJSONObject(index) ?: return@mapNotNull null
            val console = entry.optString("console").takeIf { it.isNotBlank() && it != "null" }
            val title = entry.optString("title").trim()
            val platforms = entry.optJSONArray("platforms") ?: return@mapNotNull null
            val hasExcludedCategory = (listOfNotNull(console, title, entry.optString("category")) +
                (0 until platforms.length()).map { platforms.optString(it) })
                .any(::isExcludedCategory)
            if (hasExcludedCategory) return@mapNotNull null

            val supportsAndroid = (0 until platforms.length()).any {
                platforms.optString(it).equals("Android", ignoreCase = true)
            }
            val planTargets = entry.optJSONObject("plan")?.optJSONArray("targets") ?: return@mapNotNull null
            val hasAndroidPlan = (0 until planTargets.length()).any {
                planTargets.optString(it).equals("android", ignoreCase = true)
            }
            val scan = entry.optJSONObject("scan") ?: return@mapNotNull null
            if (!supportsAndroid || !hasAndroidPlan || scan.optInt("files") <= 0) return@mapNotNull null

            val slug = entry.optString("slug").trim()
            if (title.isBlank() || slug.isBlank()) return@mapNotNull null

            val repo = entry.optString("repo").takeIf { it.isNotBlank() && it != "null" }
            val project = entry.optString("project").takeIf { it.isNotBlank() && it != "null" }
            val version = entry.optString("version").takeIf { it.isNotBlank() && it != "null" }
            val cover = entry.optString("boxartUrl").takeIf { it.startsWith("https://") }
                ?: entry.optString("iconUrl").takeIf { it.startsWith("https://") }

            Game(
                slug = slug,
                title = title,
                subtitle = project,
                description = entry.optString("description").takeIf { it.isNotBlank() && it != "null" }
                    ?: project?.let { "Port Android: $it" },
                platform = console,
                coverUrl = cover,
                status = version,
                repo = repo,
                version = version,
                recomps = listOf(RecompOption(slug, project, repo, version)),
            )
        }.distinctBy { it.slug }.groupBy { gameGroupKey(it.title) }
            .values
            .map { gamesForTitle ->
                val first = gamesForTitle.first()
                val recomps = gamesForTitle.flatMap { it.recomps }.distinctBy { it.slug }
                first.copy(
                    subtitle = if (recomps.size > 1) null else first.subtitle,
                    recomps = recomps,
                )
            }
            .let(::sortGames)
    }

    fun sortGames(games: List<Game>): List<Game> = games.sortedWith(
        compareBy<Game>({ platformOrder(it.platform) }, { it.title.lowercase() }),
    )

    fun sortPlatforms(platforms: List<String>): List<String> = platforms.distinct().sortedWith(
        compareBy<String>({ platformOrder(it) }, { it.lowercase() }),
    )

    private fun platformOrder(platform: String?): Int {
        val name = platform.orEmpty().lowercase()
        return when {
            name.contains("pc") || name.contains("ms-dos") || name.contains("macintosh") ||
                name.contains("amiga") || name.contains("commodore") || name.contains("flash") -> 0
            name.contains("playstation 5") || name.contains("xbox series") -> 10
            name.contains("switch") -> 20
            name.contains("playstation 4") || name.contains("xbox one") -> 30
            name.contains("wii u") || name.contains("3ds") -> 40
            name.contains("playstation 3") || name.contains("xbox 360") -> 50
            name == "wii" || name.contains("nintendo wii") -> 55
            name.contains("nintendo ds") || name.contains("playstation portable") || name == "psp" -> 60
            name.contains("game boy advance") || name == "gba" -> 65
            name.contains("playstation 2") || name.contains("gamecube") || name == "xbox" -> 70
            name.contains("dreamcast") -> 80
            name.contains("game boy color") -> 82
            name.contains("nintendo 64") || name == "n64" -> 90
            name.contains("saturn") || name == "playstation" || name.contains("ps1") || name.contains("playstation 1") -> 100
            name.contains("mega drive") || name.contains("genesis") || name.contains("snes") -> 130
            name.contains("game boy") -> 135
            name == "nes" -> 140
            else -> 500
        }
    }

    private fun inferPlatform(text: String): String {
        val t = text.lowercase()
        return when {
            t.contains("n64") || t.contains("64") || t.contains("nintendo 64") -> "Nintendo 64"
            t.contains("xbox 360") || t.contains("x360") || t.contains("xbla") -> "Xbox 360"
            t.contains("xbox") || t.contains("xbe") -> "Original Xbox"
            t.contains("ps1") || t.contains("playstation 1") || t.contains("psx") -> "PlayStation"
            t.contains("ps2") || t.contains("playstation 2") -> "PlayStation 2"
            t.contains("ps3") || t.contains("playstation 3") -> "PlayStation 3"
            t.contains("vita") || t.contains("ps vita") -> "PS Vita"
            t.contains("dreamcast") || t.contains("sega") -> "Dreamcast"
            t.contains("ds") || t.contains("nintendo ds") -> "Nintendo DS"
            t.contains("gamecube") || t.contains("ngc") -> "GameCube"
            t.contains("wii u") -> "Wii U"
            t.contains("wii") -> "Nintendo Wii"
            t.contains("snes") || t.contains("super nintendo") -> "SNES"
            t.contains("gba") || t.contains("game boy") -> "GBA"
            t.contains("switch") -> "Nintendo Switch"
            else -> "PC / Consoles"
        }
    }

    fun fallbackGames(): List<Game> = listOf(
        // Nintendo 64
        Game(
            slug = "shipwright",
            title = "Ship of Harkinian",
            subtitle = "The Legend of Zelda: Ocarina of Time",
            description = "Ocarina of Time com alta resolução, 60fps, áudio restaurado e controles otimizados para Android.",
            platform = "Nintendo 64",
            coverUrl = "https://recompstore.com/media/pc-shipwright-hero_web-a676439f.jpg",
            repo = "Waterdish/Shipwright-Android",
        ),
        Game(
            slug = "2ship2harkinian",
            title = "2 Ship 2 Harkinian",
            subtitle = "The Legend of Zelda: Majora's Mask",
            description = "Majora's Mask recompilado nativamente com suporte widescreen, gyroscope e melhorias gráficas.",
            platform = "Nintendo 64",
            coverUrl = "https://recompstore.com/media/pc-2ship2harkinian-hero_web-3da2845c.jpg",
            repo = "Waterdish/2ship2harkinian-Android",
        ),
        Game(
            slug = "ghostship",
            title = "Super Mario 64 Native Recomp",
            subtitle = "Super Mario 64",
            description = "Super Mario 64 recompilado em C nativo com suporte a controles, texturas HD e 60 FPS estocástico.",
            platform = "Nintendo 64",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co20au.jpg",
            repo = "retired64/sm64cdpy.releases",
        ),
        Game(
            slug = "banjorecomp",
            title = "Banjo: Recompiled",
            subtitle = "Banjo-Kazooie",
            description = "Banjo-Kazooie recompilado com suporte a controles táteis, altas taxas de quadros e câmera livre.",
            platform = "Nintendo 64",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1x7c.jpg",
            repo = "Waterdish/Banjo-Kazooie-Recompiled-Android",
        ),
        Game(
            slug = "spaghettikart",
            title = "SpaghettiKart",
            subtitle = "Mario Kart 64",
            description = "Mario Kart 64 em C nativo para Android com multiplayer e telas panorâmicas.",
            platform = "Nintendo 64",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1x76.jpg",
            repo = "Waterdish/mk64-android",
        ),
        Game(
            slug = "zelda64recomp",
            title = "Zelda 64 Recomp",
            subtitle = "The Legend of Zelda: Ocarina & Majora",
            description = "Motor de recompilação estática de Zelda 64 com renderização nativa de alta precisão.",
            platform = "Nintendo 64",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co5vmg.jpg",
            repo = "Zelda64Recomp/Zelda64Recomp",
        ),

        // PlayStation (PS1)
        Game(
            slug = "yugioh-fm-recomp",
            title = "Yu-Gi-Oh! Forbidden Memories Recomp",
            subtitle = "Yu-Gi-Oh! Forbidden Memories",
            description = "Recompilação nativa de Yu-Gi-Oh! Forbidden Memories para Android com suporte a mods 3D e velocidade ajustável.",
            platform = "PlayStation",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1r75.jpg",
            repo = "Unchiga/Yu-Gi-Oh-Forbidden-Memories-Recompiled",
        ),

        // PlayStation 2 (PS2)
        Game(
            slug = "dmc3-recomp",
            title = "Devil May Cry 3 Special Edition",
            subtitle = "Devil May Cry 3 (PS2/PC Recomp)",
            description = "Port nativo em C++ do clássico Devil May Cry 3 com suporte a custom GPU drivers, 60 FPS e controles modernos.",
            platform = "PlayStation 2",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1r78.jpg",
            repo = "sindufha/Devil-may-cry-3-recomp",
        ),

        // PlayStation Vita (PS Vita)
        Game(
            slug = "vita3k",
            title = "Vita3K Native Recomp Core",
            subtitle = "PlayStation Vita Core",
            description = "Emulador e camada de compatibilidade nativa de código aberto para PlayStation Vita no Android.",
            platform = "PS Vita",
            coverUrl = "https://raw.githubusercontent.com/Vita3K/Vita3K-Android/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            repo = "Vita3K/Vita3K-Android",
        ),

        // Sega Dreamcast
        Game(
            slug = "flycast",
            title = "Flycast Native Core",
            subtitle = "Sega Dreamcast & NAOMI",
            description = "Reimplementação de alta velocidade de Sega Dreamcast, NAOMI e Atomiswave para Android com suporte a Vulkan.",
            platform = "Dreamcast",
            coverUrl = "https://raw.githubusercontent.com/flyinghead/flycast/master/shell/android-studio/flycast/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            repo = "flyinghead/flycast",
        ),

        // Nintendo DS
        Game(
            slug = "melonds",
            title = "melonDS Native Core",
            subtitle = "Nintendo DS",
            description = "Port nativo de alto desempenho do melonDS para Android com suporte a renderizador OpenGL e toque preciso.",
            platform = "Nintendo DS",
            coverUrl = "https://raw.githubusercontent.com/rafaelvcaetano/melonDS-android/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            repo = "rafaelvcaetano/melonDS-android",
        ),

        // Original Xbox / PC
        Game(
            slug = "openmw",
            title = "The Elder Scrolls III: Morrowind",
            subtitle = "OpenMW Android Port",
            description = "Recriação completa do motor de Morrowind em C++ com suporte a mods, telas panorâmicas e controles táteis.",
            platform = "Original Xbox",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1x74.jpg",
            repo = "xyzz/openmw-android",
        ),

        // PC / Classic Engines
        Game(
            slug = "devilutionx",
            title = "DevilutionX",
            subtitle = "Diablo 1 + Hellfire",
            description = "Port nativo de Diablo 1 com suporte a toque, controles Bluetooth, multiplayer TCP/IP e expansão Hellfire.",
            platform = "PC / Consoles",
            coverUrl = "https://raw.githubusercontent.com/diasurgical/devilutionX/master/Packaging/android/res/mipmap-xxxhdpi/ic_launcher.png",
            repo = "diasurgical/devilutionX",
        ),
        Game(
            slug = "fallout1-ce",
            title = "Fallout 1 Community Edition",
            subtitle = "Fallout (1997)",
            description = "Reimplementação de código aberto do motor clássico do Fallout para Android com resoluções adaptadas.",
            platform = "PC / Consoles",
            coverUrl = "https://raw.githubusercontent.com/alexbatalov/fallout1-ce/main/res/icon.png",
            repo = "alexbatalov/fallout1-ce",
        ),
        Game(
            slug = "fallout2-ce",
            title = "Fallout 2 Community Edition",
            subtitle = "Fallout 2",
            description = "Port nativo de Fallout 2 otimizado com suporte a controles táteis e telas de alta resolução.",
            platform = "PC / Consoles",
            coverUrl = "https://raw.githubusercontent.com/alexbatalov/fallout2-ce/main/res/icon.png",
            repo = "alexbatalov/fallout2-ce",
        ),
        Game(
            slug = "com-n0n3m4-diii4a",
            title = "idTech4A++",
            subtitle = "DOOM 3, Quake 4 and Prey",
            description = "Port nativo do motor id Tech 4 (DOOM 3, Quake 4, Prey) para Android com renderizador Vulkan e OpenGL ES.",
            platform = "PC / Consoles",
            coverUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1vba.jpg",
            repo = "glKarin/com.n0n3m4.diii4a",
        ),
    )

    suspend fun androidRelease(game: Game): Release? {
        val option = game.recomps.firstOrNull() ?: RecompOption(game.slug, game.subtitle, game.repo, game.version)
        return androidRelease(option)
    }

    private suspend fun getCancellable(
        url: String,
        onConnection: (HttpURLConnection?) -> Unit,
    ): String = withContext(Dispatchers.IO) {
        val connection = openConnection(url)
        onConnection(connection)
        try {
            currentCoroutineContext().ensureActive()
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
            onConnection(null)
        }
    }

    suspend fun androidRelease(
        option: RecompOption,
        onConnection: (HttpURLConnection?) -> Unit = {},
    ): Release? = withContext(Dispatchers.IO) {
        val html = try {
            getCancellable("$SITE/games/${option.slug}", onConnection)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            return@withContext null
        }
        val apkHref = Regex("""href\s*=\s*["']([^"']+\.apk(?:[?#][^"']*)?)["']""", RegexOption.IGNORE_CASE)
            .findAll(html)
            .map { it.groupValues[1].replace("&amp;", "&") }
            .firstOrNull()
            ?: return@withContext null
        val parsedUrl = runCatching { URL(URL(SITE), apkHref) }.getOrNull()
            ?: return@withContext null
        if (!parsedUrl.host.equals("files.decompbuddy.com", ignoreCase = true)) return@withContext null
        val downloadUrl = parsedUrl.toExternalForm()
        val fileName = runCatching { URL(downloadUrl).path.substringAfterLast('/') }
            .getOrNull()?.takeIf { it.endsWith(".apk", ignoreCase = true) }
            ?: "decompbuddy-${option.slug}.apk"
        Release(
            tag = option.version ?: "latest",
            name = fileName,
            url = downloadUrl,
            size = 0L,
        )
    }

    suspend fun fetchGameDetails(slug: String): Pair<String?, String?> = withContext(Dispatchers.IO) {
        val html = runCatching { get("$SITE/games/$slug") }.getOrNull() ?: return@withContext null to null
        val ogImage = Regex("""<meta\s+property="og:image"\s+content="([^"]+)"""").find(html)?.groupValues?.get(1)
        val ogDesc = Regex("""<meta\s+property="og:description"\s+content="([^"]+)"""").find(html)?.groupValues?.get(1)
            ?: Regex("""<meta\s+name="description"\s+content="([^"]+)"""").find(html)?.groupValues?.get(1)
        val cleanDesc = ogDesc?.let { cleanHtml(it) }
        ogImage to cleanDesc
    }

    suspend fun repositoriesOf(slug: String): List<String> = withContext(Dispatchers.IO) {
        val pageHtml = runCatching { get("$SITE/game/$slug") }.getOrNull().orEmpty()
        val listedRepos = Regex("""github\.com/([\w.-]+/[\w.-]+)""")
            .findAll(pageHtml)
            .map { it.groupValues[1].removeSuffix(".git").trimEnd('/') }
            .filter {
                !it.startsWith("recomplauncher", ignoreCase = true) &&
                        !it.startsWith("elripalda/", ignoreCase = true)
            }
            .distinctBy { it.lowercase() }
            .toList()
        val knownRepo = PortRegistry.findPort(slug, "")?.repo
        (listOfNotNull(knownRepo) + listedRepos).distinctBy { it.lowercase() }
    }

    suspend fun repoOf(slug: String): String? = repositoriesOf(slug).firstOrNull()

    suspend fun androidRelease(repo: String, token: String? = null): Release? = withContext(Dispatchers.IO) {
        // 1. First attempt: latest published release
        val latestRelease = runCatching {
            val j = JSONObject(get("https://api.github.com/repos/$repo/releases/latest", token))
            extractApkRelease(j)
        }.getOrNull()

        if (latestRelease != null) return@withContext latestRelease

        // 2. Second attempt: fetch up to 15 releases (including prereleases)
        val recentRelease = runCatching {
            val arr = JSONArray(get("https://api.github.com/repos/$repo/releases?per_page=15", token))
            (0 until arr.length()).asSequence().mapNotNull { idx ->
                extractApkRelease(arr.getJSONObject(idx))
            }.firstOrNull()
        }.getOrNull()

        recentRelease
    }

    private fun extractApkRelease(j: JSONObject): Release? {
        val assets: JSONArray = j.optJSONArray("assets") ?: return null
        val apks = (0 until assets.length())
            .asSequence()
            .map { assets.getJSONObject(it) }
            .filter { it.optString("name").endsWith(".apk", ignoreCase = true) }
            .toList()
        if (apks.isEmpty()) return null

        val a = apks.firstOrNull {
            val n = it.optString("name").lowercase()
            n.contains("arm64") || n.contains("universal") || n.contains("prod") || n.contains("android")
        } ?: apks.first()

        return Release(
            tag = j.optString("tag_name", "latest"),
            name = a.optString("name", "recomp.apk"),
            url = a.optString("browser_download_url"),
            size = a.optLong("size", 0L),
            sha256 = null,
        )
    }

    private fun cleanHtml(raw: String): String =
        raw.replace(Regex("<[^>]+>"), " ")
            .replace("&amp;", "&")
            .replace("&#39;", "'")
            .replace("&quot;", "\"")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\\s+"), " ")
            .trim()
}

object CoverFetcher {
    /** Searches for game cover image on Wikipedia/Wikimedia as secondary online source. */
    suspend fun searchCoverUrl(title: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val query = URLEncoder.encode(title, "UTF-8")
            val url = "https://en.wikipedia.org/w/api.php?action=query&titles=$query&prop=pageimages&format=json&pithumbsize=800"
            val jsonStr = openConnection(url).inputStream.bufferedReader().use { it.readText() }
            val pages = JSONObject(jsonStr).getJSONObject("query").getJSONObject("pages")
            val firstKey = pages.keys().next()
            if (firstKey != "-1") {
                val page = pages.getJSONObject(firstKey)
                if (page.has("thumbnail")) {
                    return@withContext page.getJSONObject("thumbnail").getString("source")
                }
            }
            null
        }.getOrNull()
    }
}

class Store(private val ctx: Context) {
    private val p = ctx.getSharedPreferences("recomp_droid_store", Context.MODE_PRIVATE)

    fun tag(slug: String): String? = p.getString("tag:$slug", null)
    fun pkg(slug: String): String? = p.getString("pkg:$slug", null)
    fun save(slug: String, tag: String, pkg: String) = p.edit { putString("tag:$slug", tag).putString("pkg:$slug", pkg) }
    fun installed(): List<String> = p.all.keys.asSequence().filter { it.startsWith("tag:") }.map { it.removePrefix("tag:") }.toList()

    fun isAppInstalled(slug: String): Boolean {
        val packageName = pkg(slug) ?: return false
        return runCatching {
            ctx.packageManager.getPackageInfo(packageName, 0)
            true
        }.getOrDefault(false)
    }

    // Favorites
    fun isFavorite(slug: String): Boolean = p.getBoolean("fav:$slug", false)
    fun toggleFavorite(slug: String): Boolean {
        val next = !isFavorite(slug)
        p.edit { putBoolean("fav:$slug", next) }
        return next
    }

    // Settings
    var language: Int
        get() = p.getInt("setting_language", 0) // 0: Português (Brasil), 1: English (US)
        set(value) = p.edit { putInt("setting_language", value) }

    var themeMode: Int
        get() = p.getInt("setting_theme_mode", 0) // 0: System, 1: Dark, 2: Light
        set(value) = p.edit { putInt("setting_theme_mode", value) }

    var colorAccent: Int
        get() = p.getInt("setting_color_accent", 0) // 0: Dynamic, 1: Blue, 2: Purple, 3: Emerald, 4: Crimson, 5: OLED Pitch Black
        set(value) = p.edit { putInt("setting_color_accent", value) }

    var cardCornerRadius: Int
        get() = p.getInt("setting_card_corner", 0) // 0: Rounded (16dp), 1: Very Rounded (24dp), 2: Square (6dp)
        set(value) = p.edit { putInt("setting_card_corner", value) }

    var dynamicColor: Boolean
        get() = p.getBoolean("setting_dynamic_color", true)
        set(value) = p.edit { putBoolean("setting_dynamic_color", value) }

    var isGridView: Boolean
        get() = p.getBoolean("setting_grid_view", false)
        set(value) = p.edit { putBoolean("setting_grid_view", value) }

    var isCompactCard: Boolean
        get() = p.getBoolean("setting_compact_card", false)
        set(value) = p.edit { putBoolean("setting_compact_card", value) }

    var autoSearchCovers: Boolean
        get() = p.getBoolean("setting_auto_covers", true)
        set(value) = p.edit { putBoolean("setting_auto_covers", value) }

    var githubToken: String
        get() = p.getString("setting_github_token", "") ?: ""
        set(value) = p.edit { putString("setting_github_token", value) }

    fun customCover(slug: String): String? = p.getString("custom_cover:$slug", null)
    fun setCustomCover(slug: String, url: String) = p.edit { putString("custom_cover:$slug", url) }

    fun customRepo(slug: String): String? = p.getString("custom_repo:$slug", null)
    fun setCustomRepo(slug: String, repo: String) = p.edit { putString("custom_repo:$slug", repo) }

    fun clearApkCache(ctx: Context): Boolean {
        val dir = File(ctx.cacheDir, "apks")
        return dir.deleteRecursively()
    }
}

object Installer {
    /** Downloads the APK following HTTP redirects across domains/protocols and returns File + pkg name. */
    suspend fun download(
        ctx: Context,
        r: Release,
        token: String? = null,
        onConnection: (HttpURLConnection?) -> Unit = {},
        onProgress: (Float) -> Unit,
    ): Pair<File, String> = withContext(Dispatchers.IO) {
        val dir = File(ctx.cacheDir, "apks").apply { mkdirs() }
        val f = File(dir, r.name)
        val coroutineContext = currentCoroutineContext()
        var currentUrl = r.url
        var conn: HttpURLConnection? = null
        var redirects = 0
        var completed = false

        try {
            while (true) {
                coroutineContext.ensureActive()
                val nextConnection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) RecompDroid/0.1")
                    if (!token.isNullOrBlank() && currentUrl.contains("api.github.com")) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    connectTimeout = 30000
                    readTimeout = 60000
                    instanceFollowRedirects = true
                }
                conn = nextConnection
                onConnection(nextConnection)
                coroutineContext.ensureActive()

                val status = nextConnection.responseCode
                if (status in listOf(HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP, 307, 308)) {
                    val newUrl = nextConnection.getHeaderField("Location")
                        ?: error("Download redirect is missing its destination")
                    nextConnection.disconnect()
                    onConnection(null)
                    redirects++
                    if (redirects > 10) error("Too many download redirects")
                    currentUrl = newUrl
                } else {
                    break
                }
            }

            val activeConnection = conn ?: error("Download connection was not created")
            val total = activeConnection.contentLengthLong.takeIf { it > 0 } ?: r.size
            activeConnection.inputStream.use { input ->
                f.outputStream().use { output ->
                    val buf = ByteArray(64 * 1024)
                    var done = 0L
                    while (true) {
                        coroutineContext.ensureActive()
                        val n = input.read(buf)
                        coroutineContext.ensureActive()
                        if (n < 0) break
                        output.write(buf, 0, n)
                        done += n
                        if (total > 0) onProgress((done.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
            coroutineContext.ensureActive()

            val pkg = ctx.packageManager.getPackageArchiveInfo(f.path, 0)?.packageName
                ?: "com.recomp.${r.name.removeSuffix(".apk").replace(Regex("[^a-zA-Z0-9_]"), "")}"
            completed = true
            f to pkg
        } finally {
            conn?.disconnect()
            onConnection(null)
            if (!completed) f.delete()
        }
    }
}
