package com.crystal.launcher

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val CrystalAccents = listOf(
    Color(0xFFB9F56A), Color(0xFF91C7FF), Color(0xFFE7A4FF),
    Color(0xFFFFB86B), Color(0xFFFF91A4), Color(0xFF75E2D0)
)

private val CrystalWallpapers = listOf(
    listOf(Color(0xFF20283E), Color(0xFF63517E), Color(0xFFDB947E)),
    listOf(Color(0xFF0B1628), Color(0xFF155E75), Color(0xFF70D6E5)),
    listOf(Color(0xFF321A12), Color(0xFFB74728), Color(0xFFFFCF70)),
    listOf(Color(0xFF10271A), Color(0xFF237B53), Color(0xFFB7E879)),
    listOf(Color(0xFF211B50), Color(0xFF6D3DC1), Color(0xFFEBA8EF)),
    listOf(Color(0xFF131720), Color(0xFF4B576B), Color(0xFFE0E5EF))
)

private enum class LauncherScreen { Home, Drawer, Settings, HiddenApps }

private data class LauncherApp(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent { CrystalLauncherApp() }
    }
}

@Composable
private fun CrystalLauncherApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val prefs = remember { context.getSharedPreferences("crystal_launcher_prefs", Context.MODE_PRIVATE) }

    var themeMode by remember { mutableIntStateOf(prefs.getInt("theme_mode", 0).coerceIn(0, 2)) }
    var accentIndex by remember { mutableIntStateOf(prefs.getInt("accent_index", 0).coerceIn(CrystalAccents.indices)) }
    var wallpaperIndex by remember { mutableIntStateOf(prefs.getInt("wallpaper_index", 0).coerceIn(CrystalWallpapers.indices)) }
    var gridColumns by remember { mutableIntStateOf(prefs.getInt("grid_columns", 4).coerceIn(3, 6)) }
    var iconScale by remember { mutableStateOf(prefs.getFloat("icon_scale", 1.0f).coerceIn(0.75f, 1.25f)) }
    var showLabels by remember { mutableStateOf(prefs.getBoolean("show_labels", true)) }
    var iconShapeMode by remember { mutableIntStateOf(prefs.getInt("icon_shape", 0).coerceIn(0, 2)) }
    var reduceMotion by remember { mutableStateOf(prefs.getBoolean("reduce_motion", false)) }
    var hiddenPackages by remember {
        mutableStateOf(prefs.getStringSet("hidden_packages", mutableSetOf())?.toSet().orEmpty())
    }
    var favorites by remember {
        mutableStateOf(prefs.getStringSet("favorite_packages", null)?.toSet())
    }
    var installedApps by remember { mutableStateOf<List<LauncherApp>>(emptyList()) }
    var screen by remember { mutableStateOf(LauncherScreen.Home) }
    var searchText by remember { mutableStateOf("") }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    val isDark = when (themeMode) {
        1 -> true
        2 -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val accent = CrystalAccents[accentIndex]
    val colors = if (isDark) {
        darkColorScheme(
            primary = accent,
            secondary = accent,
            background = Color(0xFF10131B),
            surface = Color(0xFF191D27),
            surfaceVariant = Color(0xFF282D39),
            onPrimary = Color(0xFF10130D),
            onBackground = Color(0xFFF3F4F8),
            onSurface = Color(0xFFF3F4F8),
            onSurfaceVariant = Color(0xFFB9BECA)
        )
    } else {
        lightColorScheme(
            primary = accent,
            secondary = accent,
            background = Color(0xFFF4F5F8),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE8EAF0),
            onPrimary = Color(0xFF10130D),
            onBackground = Color(0xFF191A1F),
            onSurface = Color(0xFF191A1F),
            onSurfaceVariant = Color(0xFF555B67)
        )
    }

    LaunchedEffect(Unit) {
        installedApps = withContext(Dispatchers.IO) { queryLauncherApps(context) }
        if (favorites == null) {
            val defaultFavorites = selectDefaultHomeApps(installedApps).map { it.packageName }.toSet()
            favorites = defaultFavorites
            prefs.edit().putStringSet("favorite_packages", defaultFavorites.toMutableSet()).apply()
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(15_000)
        }
    }

    SideEffect {
        activity?.window?.let { window ->
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }

    fun saveInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    fun saveBool(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()

    fun toggleFavorite(app: LauncherApp) {
        val current = favorites.orEmpty()
        val updated = if (app.packageName in current) current - app.packageName else current + app.packageName
        favorites = updated
        prefs.edit().putStringSet("favorite_packages", updated.toMutableSet()).apply()
        val message = if (app.packageName in updated) "Added to Crystal home" else "Removed from Crystal home"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun setHidden(packageName: String, hide: Boolean) {
        val updated = if (hide) hiddenPackages + packageName else hiddenPackages - packageName
        hiddenPackages = updated
        prefs.edit().putStringSet("hidden_packages", updated.toMutableSet()).apply()
    }

    fun launchApp(app: LauncherApp) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName(app.packageName, app.activityName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "App is no longer available", Toast.LENGTH_SHORT).show()
            installedApps = queryLauncherApps(context)
        } catch (_: SecurityException) {
            Toast.makeText(context, "Android blocked this app launch", Toast.LENGTH_SHORT).show()
        }
    }

    fun openSystemHomeSettings() {
        try {
            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: Exception) {
                Toast.makeText(context, "Open Android Settings to choose the Home app", Toast.LENGTH_LONG).show()
            }
        }
    }

    BackHandler(enabled = screen != LauncherScreen.Home) {
        screen = when (screen) {
            LauncherScreen.HiddenApps -> LauncherScreen.Settings
            LauncherScreen.Settings, LauncherScreen.Drawer -> LauncherScreen.Home
            LauncherScreen.Home -> LauncherScreen.Home
        }
        searchText = ""
    }

    MaterialTheme(colorScheme = colors) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(CrystalWallpapers[wallpaperIndex]))
                .padding(WindowInsets.safeDrawing.asPaddingValues())
        ) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    if (reduceMotion) fadeIn() togetherWith fadeOut()
                    else (fadeIn() + slideInHorizontally { it / 12 }) togetherWith
                        (fadeOut() + slideOutHorizontally { -it / 18 })
                },
                label = "crystal-screen"
            ) { currentScreen ->
                when (currentScreen) {
                    LauncherScreen.Home -> {
                        val available = installedApps.filter {
                            it.packageName !in hiddenPackages && it.packageName != context.packageName
                        }
                        val favs = favorites.orEmpty()
                        val chosen = available.filter { it.packageName in favs }
                            .ifEmpty { selectDefaultHomeApps(available) }
                            .take((gridColumns * 2).coerceAtLeast(8))
                        val dockApps = selectDockApps(available, chosen)
                        HomeScreen(
                            now = now,
                            homeApps = chosen,
                            dockApps = dockApps,
                            columns = gridColumns,
                            iconScale = iconScale,
                            showLabels = showLabels,
                            iconShapeMode = iconShapeMode,
                            favorites = favs,
                            reduceMotion = reduceMotion,
                            onOpenDrawer = { searchText = ""; screen = LauncherScreen.Drawer },
                            onOpenSettings = { screen = LauncherScreen.Settings },
                            onLaunch = { app -> launchApp(app) },
                            onToggleFavorite = { app -> toggleFavorite(app) }
                        )
                    }
                    LauncherScreen.Drawer -> {
                        val available = installedApps.filter {
                            it.packageName !in hiddenPackages && it.packageName != context.packageName
                        }
                        AppDrawerScreen(
                            apps = available,
                            searchText = searchText,
                            onSearchChange = { searchText = it },
                            columns = gridColumns,
                            iconScale = iconScale,
                            showLabels = showLabels,
                            iconShapeMode = iconShapeMode,
                            favorites = favorites.orEmpty(),
                            onBack = { screen = LauncherScreen.Home; searchText = "" },
                            onOpenSettings = { screen = LauncherScreen.Settings; searchText = "" },
                            onLaunch = { app -> launchApp(app) },
                            onToggleFavorite = { app -> toggleFavorite(app) }
                        )
                    }
                    LauncherScreen.Settings -> SettingsScreen(
                        themeMode = themeMode,
                        onThemeMode = { themeMode = it; saveInt("theme_mode", it) },
                        accentIndex = accentIndex,
                        onAccentIndex = { accentIndex = it; saveInt("accent_index", it) },
                        wallpaperIndex = wallpaperIndex,
                        onWallpaperIndex = { wallpaperIndex = it; saveInt("wallpaper_index", it) },
                        gridColumns = gridColumns,
                        onGridColumns = { gridColumns = it.coerceIn(3, 6); saveInt("grid_columns", gridColumns) },
                        iconScale = iconScale,
                        onIconScale = { iconScale = it.coerceIn(0.75f, 1.25f); prefs.edit().putFloat("icon_scale", iconScale).apply() },
                        showLabels = showLabels,
                        onShowLabels = { showLabels = it; saveBool("show_labels", it) },
                        iconShapeMode = iconShapeMode,
                        onIconShapeMode = { iconShapeMode = it; saveInt("icon_shape", it) },
                        reduceMotion = reduceMotion,
                        onReduceMotion = { reduceMotion = it; saveBool("reduce_motion", it) },
                        appCount = installedApps.size,
                        onBack = { screen = LauncherScreen.Home },
                        onHiddenApps = { screen = LauncherScreen.HiddenApps },
                        onSetDefault = { openSystemHomeSettings() },
                        onReset = {
                            prefs.edit().clear().apply()
                            themeMode = 0
                            accentIndex = 0
                            wallpaperIndex = 0
                            gridColumns = 4
                            iconScale = 1f
                            showLabels = true
                            iconShapeMode = 0
                            reduceMotion = false
                            hiddenPackages = emptySet()
                            val defaults = selectDefaultHomeApps(installedApps).map { it.packageName }.toSet()
                            favorites = defaults
                            prefs.edit().putStringSet("favorite_packages", defaults.toMutableSet()).apply()
                            Toast.makeText(context, "Crystal Launcher settings reset", Toast.LENGTH_SHORT).show()
                        }
                    )
                    LauncherScreen.HiddenApps -> HiddenAppsScreen(
                        apps = installedApps.filter { it.packageName != context.packageName },
                        hiddenPackages = hiddenPackages,
                        onToggle = { packageName, hide -> setHidden(packageName, hide) },
                        onBack = { screen = LauncherScreen.Settings }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    now: Long,
    homeApps: List<LauncherApp>,
    dockApps: List<LauncherApp>,
    columns: Int,
    iconScale: Float,
    showLabels: Boolean,
    iconShapeMode: Int,
    favorites: Set<String>,
    reduceMotion: Boolean,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onLaunch: (LauncherApp) -> Unit,
    onToggleFavorite: (LauncherApp) -> Unit
) {
    val time = remember(now) { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now)) }
    val date = remember(now) { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date(now)) }
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("CRYSTAL", fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp, color = Color.White)
                Text("PERSONAL LAUNCHER", fontSize = 9.sp, letterSpacing = 1.5.sp, color = Color.White.copy(alpha = 0.72f))
            }
            Spacer(Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(CrystalAccents[0]))
                    Spacer(Modifier.width(7.dp))
                    Text("YOUR SPACE", fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Launcher settings", tint = Color.White)
            }
        }

        Spacer(Modifier.height(22.dp))
        Text(time, fontSize = 68.sp, lineHeight = 72.sp, fontWeight = FontWeight.Light, letterSpacing = (-3).sp, color = Color.White)
        Text(date, fontSize = 14.sp, color = Color.White.copy(alpha = 0.84f))
        Spacer(Modifier.height(18.dp))

        Surface(
            shape = RoundedCornerShape(25.dp),
            color = Color.White.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(43.dp).clip(RoundedCornerShape(16.dp)).background(CrystalAccents[0]),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFF192212), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(greetingFor(now), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Make today feel like yours.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.76f))
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(Modifier.height(19.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("FAVORITES", fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.82f))
            Spacer(Modifier.weight(1f))
            Text(
                "ALL APPS  ↗",
                modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onOpenDrawer).padding(horizontal = 10.dp, vertical = 7.dp),
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns.coerceIn(3, 6)),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            items(homeApps, key = { it.packageName + ":" + it.activityName }) { app ->
                AppTile(
                    app = app,
                    iconSize = (47f * iconScale).dp,
                    showLabel = showLabels,
                    shapeMode = iconShapeMode,
                    isFavorite = app.packageName in favorites,
                    onClick = { onLaunch(app) },
                    onLongClick = { onToggleFavorite(app) }
                )
            }
        }

        if (dockApps.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(29.dp),
                color = Color.White.copy(alpha = 0.20f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth().shadow(if (reduceMotion) 0.dp else 9.dp, RoundedCornerShape(29.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dockApps.take(5).forEach { app ->
                        DockIcon(app = app, shapeMode = iconShapeMode, onClick = { onLaunch(app) })
                    }
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.size(47.dp)) {
                        Icon(Icons.Filled.Apps, contentDescription = "All apps", tint = Color.White, modifier = Modifier.size(27.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.width(88.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.88f)))
        }
    }
}

@Composable
private fun DockIcon(app: LauncherApp, shapeMode: Int, onClick: () -> Unit) {
    val bitmap = remember(app.packageName, app.activityName) { drawableToBitmap(app.icon).asImageBitmap() }
    Box(
        modifier = Modifier.size(49.dp).clip(launcherShape(shapeMode)).background(Color.White.copy(alpha = 0.95f))
            .clickable(onClick = onClick).padding(7.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(bitmap = bitmap, contentDescription = app.label, modifier = Modifier.fillMaxSize())
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTile(
    app: LauncherApp,
    iconSize: Dp,
    showLabel: Boolean,
    shapeMode: Int,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bitmap = remember(app.packageName, app.activityName) { drawableToBitmap(app.icon).asImageBitmap() }
    Column(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier.size(iconSize + 14.dp).clip(launcherShape(shapeMode))
                    .background(Color.White.copy(alpha = 0.93f))
                    .border(1.dp, Color.White.copy(alpha = 0.65f), launcherShape(shapeMode))
                    .padding(7.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(bitmap = bitmap, contentDescription = app.label, modifier = Modifier.fillMaxSize())
            }
            if (isFavorite) {
                Box(
                    modifier = Modifier.size(17.dp).clip(CircleShape).background(CrystalAccents[0]),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Star, contentDescription = "Favorite", tint = Color(0xFF1A2112), modifier = Modifier.size(11.dp))
                }
            }
        }
        if (showLabel) {
            Spacer(Modifier.height(5.dp))
            Text(
                app.label,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
private fun AppDrawerScreen(
    apps: List<LauncherApp>,
    searchText: String,
    onSearchChange: (String) -> Unit,
    columns: Int,
    iconScale: Float,
    showLabels: Boolean,
    iconShapeMode: Int,
    favorites: Set<String>,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onLaunch: (LauncherApp) -> Unit,
    onToggleFavorite: (LauncherApp) -> Unit
) {
    val filtered = remember(apps, searchText) {
        apps.filter { appMatchesSearch(it.label, it.packageName, searchText) }
    }
    Column(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.background).padding(horizontal = 17.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Column(Modifier.weight(1f)) {
                Text("Your apps", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("${filtered.size} apps · long-press to favorite", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
        }
        Spacer(Modifier.height(8.dp))
        TextField(
            value = searchText,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search apps or package names") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (searchText.isNotEmpty()) IconButton(onClick = { onSearchChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                }
            },
            shape = RoundedCornerShape(22.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        if (filtered.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Text("No apps found", fontWeight = FontWeight.SemiBold)
                Text("Try a different search", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns.coerceIn(3, 6)),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(filtered, key = { it.packageName + ":" + it.activityName }) { app ->
                    AppTile(
                        app = app,
                        iconSize = (47f * iconScale).dp,
                        showLabel = showLabels,
                        shapeMode = iconShapeMode,
                        isFavorite = app.packageName in favorites,
                        onClick = { onLaunch(app) },
                        onLongClick = { onToggleFavorite(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    themeMode: Int,
    onThemeMode: (Int) -> Unit,
    accentIndex: Int,
    onAccentIndex: (Int) -> Unit,
    wallpaperIndex: Int,
    onWallpaperIndex: (Int) -> Unit,
    gridColumns: Int,
    onGridColumns: (Int) -> Unit,
    iconScale: Float,
    onIconScale: (Float) -> Unit,
    showLabels: Boolean,
    onShowLabels: (Boolean) -> Unit,
    iconShapeMode: Int,
    onIconShapeMode: (Int) -> Unit,
    reduceMotion: Boolean,
    onReduceMotion: (Boolean) -> Unit,
    appCount: Int,
    onBack: () -> Unit,
    onHiddenApps: () -> Unit,
    onSetDefault: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 18.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Column(Modifier.weight(1f)) {
                Text("Personalization", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Make Crystal feel like yours", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        androidx.compose.foundation.lazy.LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SettingsCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF10131B)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = CrystalAccents[0], modifier = Modifier.size(29.dp))
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Crystal Launcher", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Version 1.0.0 · Material 3", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text("$appCount launchable activities detected", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }
            }
            item {
                SettingsCard {
                    SettingsSectionTitle("Theme", Icons.Filled.Palette)
                    Text("Choose your interface", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf("System", "Dark", "Light").forEachIndexed { index, label ->
                            FilterChip(selected = themeMode == index, onClick = { onThemeMode(index) }, label = { Text(label) })
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Accent colour", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        CrystalAccents.forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier.size(35.dp).clip(CircleShape).background(color)
                                    .border(if (accentIndex == index) 3.dp else 1.dp, if (accentIndex == index) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.8f), CircleShape)
                                    .clickable { onAccentIndex(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (accentIndex == index) Icon(Icons.Filled.Check, contentDescription = "Selected accent", tint = Color(0xFF16200F), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            item {
                SettingsCard {
                    SettingsSectionTitle("Wallpaper", Icons.Filled.Wallpaper)
                    Text("Pick a gradient background", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        CrystalWallpapers.forEachIndexed { index, colors ->
                            Box(
                                modifier = Modifier.weight(1f).height(55.dp).clip(RoundedCornerShape(15.dp))
                                    .background(Brush.linearGradient(colors))
                                    .border(if (wallpaperIndex == index) 3.dp else 1.dp, if (wallpaperIndex == index) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.45f), RoundedCornerShape(15.dp))
                                    .clickable { onWallpaperIndex(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (wallpaperIndex == index) Icon(Icons.Filled.Check, contentDescription = "Selected wallpaper", tint = Color.White)
                            }
                        }
                    }
                }
            }
            item {
                SettingsCard {
                    SettingsSectionTitle("Home screen", Icons.Filled.Home)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("App grid", fontWeight = FontWeight.SemiBold)
                            Text("$gridColumns columns", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Text("3", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Slider(value = gridColumns.toFloat(), onValueChange = { onGridColumns(it.roundToInt()) }, valueRange = 3f..6f, steps = 2, modifier = Modifier.weight(2f))
                        Text("6", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Icon size", fontWeight = FontWeight.SemiBold)
                            Text("${(iconScale * 100).roundToInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Slider(value = iconScale, onValueChange = onIconScale, valueRange = 0.75f..1.25f, steps = 4, modifier = Modifier.weight(2f))
                    }
                    SettingsSwitchRow("Show app labels", "Display names beneath icons", showLabels, onShowLabels)
                    Spacer(Modifier.height(7.dp))
                    Text("Icon shape", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf("Squircle", "Circle", "Square").forEachIndexed { index, label ->
                            FilterChip(selected = iconShapeMode == index, onClick = { onIconShapeMode(index) }, label = { Text(label, fontSize = 12.sp) })
                        }
                    }
                    SettingsSwitchRow("Reduce motion", "Use simpler screen transitions", reduceMotion, onReduceMotion)
                }
            }
            item {
                SettingsCard {
                    SettingsSectionTitle("Apps & privacy", Icons.Filled.VisibilityOff)
                    SettingsActionRow("Hide apps", "Keep selected apps out of the drawer", Icons.Filled.VisibilityOff, onHiddenApps)
                    SettingsActionRow("Set default launcher", "Choose Crystal when Android asks for Home", Icons.Filled.Home, onSetDefault)
                }
            }
            item {
                SettingsCard {
                    SettingsSectionTitle("About", Icons.Filled.Info)
                    Text("Crystal Launcher is a standalone home-screen app. It does not replace Android, alter system settings, or require root access.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                    Spacer(Modifier.height(13.dp))
                    Button(onClick = onReset, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)) {
                        Text("Reset customization")
                    }
                }
            }
        }
    }
}

@Composable
private fun HiddenAppsScreen(
    apps: List<LauncherApp>,
    hiddenPackages: Set<String>,
    onToggle: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 18.dp, top = 8.dp, bottom = 12.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Column(Modifier.weight(1f)) {
                Text("Hide apps", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Hidden apps stay installed on Android", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items(apps, key = { it.packageName + ":" + it.activityName }) { app ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(MaterialTheme.colorScheme.surface)
                        .clickable { onToggle(app.packageName, app.packageName !in hiddenPackages) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val bitmap = remember(app.packageName, app.activityName) { drawableToBitmap(app.icon).asImageBitmap() }
                    Image(bitmap = bitmap, contentDescription = app.label, modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)))
                    Spacer(Modifier.width(11.dp))
                    Text(app.label, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                    Switch(checked = app.packageName in hiddenPackages, onCheckedChange = { onToggle(app.packageName, it) })
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(17.dp)) { content() }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 9.dp)) {
        Box(Modifier.size(33.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingsSwitchRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 9.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun SettingsActionRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun queryLauncherApps(context: Context): List<LauncherApp> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return try {
        pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { resolveInfo ->
                val info = resolveInfo.activityInfo ?: return@mapNotNull null
                val label = runCatching { resolveInfo.loadLabel(pm).toString() }.getOrDefault(info.packageName)
                val icon = runCatching { resolveInfo.loadIcon(pm) }.getOrNull() ?: return@mapNotNull null
                LauncherApp(label = label, packageName = info.packageName, activityName = info.name, icon = icon)
            }
            .distinctBy { it.packageName + ":" + it.activityName }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    } catch (_: RuntimeException) {
        emptyList()
    }
}

private fun selectDefaultHomeApps(apps: List<LauncherApp>): List<LauncherApp> {
    if (apps.isEmpty()) return emptyList()
    val hints = listOf("phone", "dialer", "camera", "chrome", "browser", "message", "whatsapp", "photo", "gallery", "youtube", "music")
    val selected = mutableListOf<LauncherApp>()
    hints.forEach { hint ->
        apps.firstOrNull { app -> (app.label + " " + app.packageName).contains(hint, ignoreCase = true) }
            ?.let { if (selected.none { chosen -> chosen.packageName == it.packageName }) selected.add(it) }
    }
    apps.forEach { if (selected.none { chosen -> chosen.packageName == it.packageName }) selected.add(it) }
    return selected.take(8)
}

private fun selectDockApps(apps: List<LauncherApp>, fallback: List<LauncherApp>): List<LauncherApp> {
    val hints = listOf("phone", "dialer", "message", "whatsapp", "camera", "chrome", "browser")
    val selected = mutableListOf<LauncherApp>()
    hints.forEach { hint ->
        apps.firstOrNull { app -> (app.label + " " + app.packageName).contains(hint, ignoreCase = true) }
            ?.let { if (selected.none { chosen -> chosen.packageName == it.packageName }) selected.add(it) }
    }
    fallback.forEach { if (selected.none { chosen -> chosen.packageName == it.packageName }) selected.add(it) }
    return selected.take(4)
}

internal fun appMatchesSearch(label: String, packageName: String, query: String): Boolean {
    val normalized = query.trim()
    return normalized.isEmpty() || label.contains(normalized, ignoreCase = true) || packageName.contains(normalized, ignoreCase = true)
}

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    val originalBounds = Rect(drawable.bounds)
    val width = drawable.intrinsicWidth.takeIf { it > 0 }?.coerceAtMost(192) ?: 96
    val height = drawable.intrinsicHeight.takeIf { it > 0 }?.coerceAtMost(192) ?: 96
    val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    drawable.bounds = originalBounds
    return bitmap
}

private fun launcherShape(mode: Int): Shape = when (mode) {
    1 -> CircleShape
    2 -> RoundedCornerShape(9.dp)
    else -> RoundedCornerShape(18.dp)
}

private fun greetingFor(now: Long): String {
    val hour = java.util.Calendar.getInstance().apply { timeInMillis = now }.get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Welcome back"
    }
}
