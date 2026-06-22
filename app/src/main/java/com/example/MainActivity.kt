package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data model representing our certified geospatial inspection records
data class InspectionRecord(
    val id: String,
    val address: String,
    val date: String,
    val time: String,
    val integrityScore: Float,
    val imageUrl: String,
    val category: String,
    val isFlagged: Boolean = false,
    val latitude: String = "34.0522",
    val longitude: String = "-118.2437",
    val altitude: String = "124m MSL",
    val heading: String = "145° SE",
    val device: String = "Android Pro 14",
    val sensor: String = "IMX989 1-inch",
    val shutter: String = "1/250s",
    val iso: String = "100"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainApp() {
    // App Lifecycles & Navigation States
    var currentScreen by rememberSaveable { mutableStateOf("splash") }
    var onboardingPage by rememberSaveable { mutableIntStateOf(0) }
    var selectedTab by rememberSaveable { mutableStateOf("camera") }
    
    // Toggleable settings
    var isGridEnabled by rememberSaveable { mutableStateOf(true) }
    var isWatermarkEnabled by rememberSaveable { mutableStateOf(true) }
    var isDarkModeEnabled by rememberSaveable { mutableStateOf(true) }
    var isBiometricEnabled by rememberSaveable { mutableStateOf(false) }
    var gpsThreshold by rememberSaveable { mutableFloatStateOf(5.0f) }
    var isGeoTaggedEnabled by rememberSaveable { mutableStateOf(true) }
    var isHdrEnabled by rememberSaveable { mutableStateOf(false) }
    var isGoldenRatioEnabled by rememberSaveable { mutableStateOf(false) }
    var selectedResolution by rememberSaveable { mutableStateOf("48MP") }
    var selectedAspectRatio by rememberSaveable { mutableStateOf("4:3") }

    // Selected inspection record for details overlay
    var selectedRecordDetail by remember { mutableStateOf<InspectionRecord?>(null) }
    var showActiveAnalyticsDashboard by rememberSaveable { mutableStateOf(false) }
    var showMapDetailPreview by rememberSaveable { mutableStateOf(false) }

    // Camera Capture States
    var cameraFlashActive by rememberSaveable { mutableStateOf(false) }
    var isCapturedSuccessFlowActive by rememberSaveable { mutableStateOf(false) }
    
    // Grid toggle filter for gallery
    var isGalleryGridView by rememberSaveable { mutableStateOf(true) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var activeDateFilter by rememberSaveable { mutableStateOf("Today") }

    // Local stateful db of records
    val recordsList = remember {
        mutableStateListOf(
            InspectionRecord(
                id = "ASSET #44192",
                address = "242 Terminal Dr, Houston, TX",
                date = "Oct 24, 2026",
                time = "14:20",
                integrityScore = 0.984f,
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDY0JIl6dvHesIvVnNJHUFs420pTI6kH02TJNB9jv6AQa8c5Pm5VOlw5RBus5bmfexCppT8TbMVKprGQAB-j5SlISnBIK15HcGRuYFU2ocEw4x0Q9JniSxCtoWsS62jgGYNu46itay3C4Sb4Ct8TD-EMZDXjfMeR9d61ieGGH1kJH-sZOBe3_u7FxSemxM36PxtHbKMY2VqIthqdxgg7B7J0qza9GCGOtYwV5yODQHPsOP364qWq7cKadTHku61dyOmy9fS7DbXtBLn",
                category = "Pressure Pipeline"
            ),
            InspectionRecord(
                id = "ASSET #11204",
                address = "West Solar Field, Block C",
                date = "Oct 23, 2026",
                time = "09:15",
                integrityScore = 0.991f,
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBxdQa9m8Kv6AEN1z48X21AkT3iOrsR_nctxCr9n_aPCxPvxS5n_Y8ROSChcvEH494KhYG4Rvz0is9pZsfmBAVX7LYlynO7aCCoED5B7BrI8KeCR2C64YbPqU2zN9gr3G8v8C1OvhMhiJ0BU3bLBrLlkm3GozbuE_9vjIBcObm-XPzPFtZSGO2saVRjDHa6pGtwWpNbMDIrKb3N6zYng8Kt8b92G6ug-espMsNekLUwMlE_0zqlbvh5i_zOUY5Hs7mPyyTBKIzU-768",
                category = "Solar Cell Matrix"
            ),
            InspectionRecord(
                id = "ASSET #9982",
                address = "Main Substation, Transformer 4",
                date = "Oct 22, 2026",
                time = "16:45",
                integrityScore = 0.725f,
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAETtIBvQHaXr28pzJZnd7HBqgEPcctwddYiw_ETE_en2RwfCWEz6DlD1eJRZFvp0_4dvN9auBiRxT3K7Vp-Hs3jTCzhE3MdJqa6Id1TQI-YwzxZ35b68hsBDt9cQjVvTZaTvZ3_Xu6iu5MLdXywmkvnAwhjucpcmIZWfGm3GIeN5RU4vzBMOOvRCY0WwHRsnTam4LT1Wn-oD-3aqOnUOa3NxcsTCSPEQmJskFto4xB2hzqhwlnBOPLTGXnjlQU9au3P-JtxwQ4PZXf",
                category = "Substation Core",
                isFlagged = true
            ),
            InspectionRecord(
                id = "ASSET #6627",
                address = "Offshore Site, Basin 2",
                date = "Oct 21, 2026",
                time = "11:30",
                integrityScore = 0.950f,
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAqGqC2z24l26D_t-abhnNg6P_Wa5zX3Zb8qwIeGMp5ro0LFNZozAWnGNIodG-_ZvtC5pZLggbkwFQlvtqJPRqMFLMzownY_LAmg8poqi_12VLifQomnAIAx1iC2BaU42GGkGhTv3nIhvdcYfZRPUaFhithRhFt3gIUUQyYMFxnkUpYuWX_nUug29bNpobdCw-f9HwLIn3DX_Whr_ShmGqDPV_Lfeb4PI3GONKoVUSxg4kr0YHxBRJGebe_G97wBswFoa0qLlqRNoz6",
                category = "Offshore Array"
            ),
            InspectionRecord(
                id = "ASSET #3301",
                address = "Northside Treatment Plant",
                date = "Oct 20, 2026",
                time = "13:10",
                integrityScore = 0.978f,
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDr3njlafgR7QpNMfgWxA0gsKUwUX-f7Voqm2HuKd_gTz4Zd7nweoHLsu2hP6S170QrZVbaqrKOZJGD5hFlkay2wZsfA-PbOzaMhuWGOQUOFK_TWHNyOskV6S9ocOzfLtz0DRAMsmSrKxhwps6srI7M0PrQqhckEy2x4_ovMbfyhcw5hAh3NDMjeFoJNmSpXxhkBl52icLOXinJ9iISOCPdNGosxr-X9bhnm9DMW2tCG-TpKumE6qzqmVq4CEG5-_dPMWKfyPFjFc0D",
                category = "Infrastructure Filter"
            ),
            InspectionRecord(
                id = "ASSET #7742",
                address = "Data Hub Alpha, Rack 12",
                date = "Oct 19, 2026",
                time = "08:50",
                integrityScore = 0.999f,
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDh0ZVF0Eepv4pKq9HIagD73Lbt2m0FogTHkFzZZFTdlTQNTu5Jx6fsp-f73CHgVBW850h_gCwJZN-fOo09ZsFQcmBJdhiNteWc7DYkqh4JII2VGB2AuafmHivGo7Rh8mRZEpXjzLu6Ds73AsvPgR7RUP6iCTz9CEhHMSEUdQ9Bv0fC-8vhl_LOfTG23rV6G2d5a8MjyRAxXrQFkWkbtPTYL_IYH5IiKn1iOKIlZ_T71ZrrKjzRwOuD8iwPaJrwrQ5FAk3qnOHo5flS",
                category = "Optical Node Server"
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF101415))) {
        when (currentScreen) {
            "splash" -> SplashScreen(onFinished = { currentScreen = "onboarding" })
            "onboarding" -> OnboardingScreen(
                currentPage = onboardingPage,
                onPageChanged = { onboardingPage = it },
                onGetStarted = { currentScreen = "permissions" }
            )
            "permissions" -> PermissionsScreen(
                onNext = { currentScreen = "app" }
            )
            "app" -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF0A192F).copy(alpha = 0.9f),
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == "camera",
                                onClick = { 
                                    selectedTab = "camera"
                                    isCapturedSuccessFlowActive = false
                                },
                                icon = { Icon(Icons.Default.PhotoCamera, contentDescription = "Camera") },
                                label = { Text("Camera", fontFamily = FontFamily.SansSerif) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == "gallery",
                                onClick = { selectedTab = "gallery" },
                                icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery") },
                                label = { Text("Gallery", fontFamily = FontFamily.SansSerif) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == "map",
                                onClick = { selectedTab = "map" },
                                icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                                label = { Text("Map", fontFamily = FontFamily.SansSerif) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == "settings",
                                onClick = { selectedTab = "settings" },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings", fontFamily = FontFamily.SansSerif) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (selectedTab) {
                            "camera" -> {
                                if (isCapturedSuccessFlowActive) {
                                    SuccessScreen(
                                        onRetake = { isCapturedSuccessFlowActive = false },
                                        onSaveToVault = {
                                            // Add 7th success record!
                                            val alreadyExists = recordsList.any { it.id == "ASSET #88423" }
                                            if (!alreadyExists) {
                                                recordsList.add(
                                                    0,
                                                    InspectionRecord(
                                                        id = "ASSET #88423",
                                                        address = "Port Sector 7B, Industrial Way",
                                                        date = "Oct 24, 2026",
                                                        time = "14:32",
                                                        integrityScore = 0.980f,
                                                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDom0Q0aLbJglQ1el3k6aWjnio_MdnieMTW-mM7DzuhEyi2AqUpvllRDupby6Ti4GqxmuaQtZmLOxMDHIq6Y0ciiH0g_eWPcbNzCeuMykfoempPN1hMDJAdvUfFrSog7zfQ9sw86A6Sv-RB0kmPhIPgpHkjX15Dr2AAdDUzadQp1v-3rYNfY6_9219Sd4NOXlUxN7FH4FwEc5G5O0HWks5Z6mDm5swzhasOi_QNQEp_D_yVvVkpX4CIdcwLvrWTrP1gfOlvYU5wUgUG",
                                                        category = "Substation Grid Unit"
                                                    )
                                                )
                                            }
                                            isCapturedSuccessFlowActive = false
                                            selectedTab = "gallery"
                                        }
                                    )
                                } else {
                                    CameraScreen(
                                        flashActive = cameraFlashActive,
                                        gridEnabled = isGridEnabled,
                                        onFlashToggle = { cameraFlashActive = !cameraFlashActive },
                                        onGridToggle = { isGridEnabled = !isGridEnabled },
                                        onCapture = { isCapturedSuccessFlowActive = true },
                                        geoTaggedEnabled = isGeoTaggedEnabled,
                                        onGeoTaggedToggle = { isGeoTaggedEnabled = !isGeoTaggedEnabled },
                                        hdrEnabled = isHdrEnabled,
                                        onHdrToggle = { isHdrEnabled = !isHdrEnabled },
                                        goldenRatioEnabled = isGoldenRatioEnabled,
                                        onGoldenRatioToggle = { isGoldenRatioEnabled = !isGoldenRatioEnabled },
                                        selectedResolution = selectedResolution,
                                        onResolutionChanged = { selectedResolution = it },
                                        selectedAspectRatio = selectedAspectRatio,
                                        onAspectRatioChanged = { selectedAspectRatio = it },
                                        onGalleryShortcutClick = { selectedTab = "gallery" }
                                    )
                                }
                            }
                            "gallery" -> {
                                GalleryScreen(
                                    records = recordsList,
                                    isGridView = isGalleryGridView,
                                    onViewToggle = { isGalleryGridView = it },
                                    searchQuery = searchQuery,
                                    onSearchChanged = { searchQuery = it },
                                    activeFilter = activeDateFilter,
                                    onFilterChanged = { activeDateFilter = it },
                                    onRecordSelected = { selectedRecordDetail = it }
                                )
                            }
                            "map" -> {
                                MapViewScreen(
                                    showDetail = showMapDetailPreview,
                                    onToggleDetail = { showMapDetailPreview = !showMapDetailPreview },
                                    onViewDetailsClick = {
                                        // Auto select pipeline record inside map view details
                                        selectedRecordDetail = recordsList.firstOrNull { it.id == "ASSET #44192" }
                                        selectedTab = "gallery"
                                    }
                                )
                            }
                            "settings" -> {
                                if (showActiveAnalyticsDashboard) {
                                    AnalyticsDashboard(
                                        onBack = { showActiveAnalyticsDashboard = false }
                                    )
                                } else {
                                    SettingsScreen(
                                        gridOverlay = isGridEnabled,
                                        onGridToggle = { isGridEnabled = it },
                                        watermark = isWatermarkEnabled,
                                        onWatermarkToggle = { isWatermarkEnabled = it },
                                        darkMode = isDarkModeEnabled,
                                        onDarkModeToggle = { isDarkModeEnabled = it },
                                        biometric = isBiometricEnabled,
                                        onBiometricToggle = { isBiometricEnabled = it },
                                        accuracyVal = gpsThreshold,
                                        onAccuracyChanged = { gpsThreshold = it },
                                        onOpenAnalytics = { showActiveAnalyticsDashboard = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Details Overlay View for Gallery Items
        selectedRecordDetail?.let { record ->
            DetailScreen(
                record = record,
                onDismiss = { selectedRecordDetail = null },
                onDelete = {
                    recordsList.remove(record)
                    selectedRecordDetail = null
                }
            )
        }
    }
}

// ==========================================
// 1. SPLASH / BOOT SCREEN
// ==========================================
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    var bootStatusText by remember { mutableStateOf("Initializing Precision Systems...") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            delay(800)
            bootStatusText = "Establishing Satellite Safe Link..."
            progress = 0.35f
            delay(800)
            bootStatusText = "Syncing Certified Asset Database..."
            progress = 0.70f
            delay(1000)
            bootStatusText = "Secure Protocol Verification Success!"
            progress = 1.0f
            delay(600)
            onFinished()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF101415)),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic Grid Background Layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val steps = 11
            val stepWidth = size.width / steps
            val stepHeight = size.height / steps
            for (i in 0..steps) {
                drawLine(
                    color = Color(0xFFB9C7E4).copy(alpha = 0.04f),
                    start = androidx.compose.ui.geometry.Offset(x = i * stepWidth, y = 0f),
                    end = androidx.compose.ui.geometry.Offset(x = i * stepWidth, y = size.height),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFFB9C7E4).copy(alpha = 0.04f),
                    start = androidx.compose.ui.geometry.Offset(x = 0f, y = i * stepHeight),
                    end = androidx.compose.ui.geometry.Offset(x = size.width, y = i * stepHeight),
                    strokeWidth = 1f
                )
            }
        }

        // Glow Behind
        Box(
            modifier = Modifier
                .size(360.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF76D6D5).copy(alpha = 0.07f), Color.Transparent)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Stylized Compass Radar Rings
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val radarPulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                // Outer radar ring
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(radarPulseScale)
                        .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.12f), CircleShape)
                )
                // Inner radar ring
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(radarPulseScale * 0.9f)
                        .border(1.dp, Color(0xFF76D6D5).copy(alpha = 0.25f), CircleShape)
                )

                // Central Tech Scope
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFF0A192F).copy(alpha = 0.85f), CircleShape)
                        .border(1.5.dp, Color(0xFF4EDEA3).copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterCenterFocus,
                        contentDescription = "Radar Crosshair",
                        tint = Color(0xFF4EDEA3),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Brand Header Texts
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "GeoCapture ",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color.White
                )
                Text(
                    text = "Pro",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFF4EDEA3)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ENTERPRISE GEOSPATIAL INTELLIGENCE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Color(0xFFC5C6CD).copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading HUD Box
            Box(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .background(Color(0xFF0A192F).copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "Target",
                            tint = Color(0xFF4EDEA3),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = bootStatusText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Loader Bar
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
                        label = "progress"
                    )
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF76D6D5),
                        trackColor = Color(0xFF323537)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dummy coordinates changing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "LAT: -17.0779°",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFFC5C6CD).copy(alpha = 0.4f)
                        )
                        Text(
                            text = "SYS_READY",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFFC5C6CD).copy(alpha = 0.4f)
                        )
                        Text(
                            text = "ALT: 122.0M",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFFC5C6CD).copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. ONBOARDING / CAROUSEL SCREEN
// ==========================================
@Composable
fun OnboardingScreen(
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    onGetStarted: () -> Unit
) {
    val onboardingData = listOf(
        Triple(
            "Secure Geo-Tagged Capture",
            "Harness the power of cryptographically signed metadata. Every pixel captured is automatically linked to verified spatial data.",
            "https://lh3.googleusercontent.com/aida-public/AB6AXuDH9qQQ1Y_R6lth_EXwxoRqZEFv-lKMEClSWSwE_9WYXZ9HsBm6j2vGiL3I1j_aBQ9N5GhEYTCgXoUKsDsIMlAV6-raaSaROVfPKjMSqQO-Laect9fIvwydX64UnSfa3p3-BX7Oga7pETR8wZhIS2Zos1PBz5_s16u5goWDGbCsW8tuiZvp_TIFwr9md9V_d9EDq2jJyg-GbTTNj-rcZGH7aAB73PUdLbjO-_5-hqV-XEAL11u21rUzlf-qGexJuW0Z2b_EdHoBNj7f"
        ),
        Triple(
            "Location Verification & Proof",
            "Multi-source verification protocols ensure Proof-of-Presence for every asset inspection, preventing fraudulent data injection.",
            "https://lh3.googleusercontent.com/aida-public/AB6AXuCkUKuLul5CsIpOFpVFSPILN3UzYhHllh58LDVup8YevQ111sXgbgGBzMy4drmv1fJj_x3DcNtqAXjLTxgUWl_ZeVNFIBvPQftfHwjP0r1tcA03JECEoz_6fCi1IgtobylTuTUgWiDhIyxwinV892RZbILCkqkoD9pHNq1KW99LOPZvfffKyrdkErJSjS9NVgA1Eu1P-yhcDReL0925WQT17AG4ouWHqlE9H0J9tQwcj-bKmg1EdCZVUXN2y874Iyj4fwCqTMa9kUZm"
        ),
        Triple(
            "Interactive Intelligence",
            "Review historical records on an interactive GIS canvas. Track asset degradation over time with time-stamped visual audits.",
            "https://lh3.googleusercontent.com/aida-public/AB6AXuBJRiz1yCBMHy21vg4Nm_54ajT19meH5W2RU2oFSBm21ljoklcj_rf9gR2OsGQXbvBBghudo5NBX-19IWAYTEbFS5_QWbD7ZxUPGi1O0vSFVXNolqqRRp7XqYH_eV42qvzkhWNlHliWO3ZRi-vjqS6t8B7sMLyYbyxuJlcnglKWW3vGfeQeeVbsPJVdIu-5CU5r0pp7AA0I97Hab2R07YMVTjrSoCfJv1sO9ZSd4kPoNtsR71I5ikcb9hvtU-pdBn7KAbXC8Bscltkr"
        )
    )

    val currentData = onboardingData[currentPage]

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF101415)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enterprise Setup",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB9C7E4),
                    fontSize = 18.sp
                )
                TextButton(onClick = onGetStarted) {
                    Text(text = "Skip", color = Color(0xFF76D6D5))
                }
            }

            // Beautiful Card Illustration Block with Gaps
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .background(Color(0xFF0A192F).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = currentData.third,
                        contentDescription = "Onboarding Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Dark fade overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFF101415).copy(alpha = 0.8f))
                                )
                            )
                    )
                }
            }

            // Explaining Typography and Text Information
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Text(
                    text = currentData.first,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentData.second,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    color = Color(0xFFC5C6CD),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Row: Dots + Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    onboardingData.forEachIndexed { index, _ ->
                        val isSelected = index == currentPage
                        val width by animateDpAsState(if (isSelected) 24.dp else 8.dp, label = "dot")
                        val color = if (isSelected) Color(0xFF76D6D5) else Color(0xFF323537)
                        Box(
                            modifier = Modifier
                                .size(height = 8.dp, width = width)
                                .background(color, RoundedCornerShape(4.dp))
                        )
                    }
                }

                // Action button next or start
                if (currentPage == onboardingData.lastIndex) {
                    Button(
                        onClick = onGetStarted,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4EDEA3)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = "Get Started",
                            color = Color(0xFF003824),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = Color(0xFF003824)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { onPageChanged(currentPage + 1) },
                        modifier = Modifier
                            .background(Color(0xFF76D6D5), CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = Color(0xFF003737)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2B. DEVICE INTEGRATIONS / PERMISSION FLOW
// ==========================================
@Composable
fun PermissionsScreen(onNext: () -> Unit) {
    var hasCamera by rememberSaveable { mutableStateOf(false) }
    var hasLocation by rememberSaveable { mutableStateOf(false) }
    var hasNotifications by rememberSaveable { mutableStateOf(false) }
    var hasStorage by rememberSaveable { mutableStateOf(false) }

    val grantedCount = listOf(hasCamera, hasLocation, hasNotifications, hasStorage).count { it }
    val progressFraction = grantedCount.toFloat() / 4.0f

    var showDialogFor by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101415))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Animated circular badge showing authorization level
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val sweepAngle by animateFloatAsState(
                        targetValue = progressFraction * 360f,
                        animationSpec = tween(600, easing = FastOutSlowInEasing),
                        label = "progressWheel"
                    )
                    // Static path behind track
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(6.dp, Color(0xFF323537), CircleShape)
                    )
                    // Animated path
                    Canvas(modifier = Modifier.size(88.dp)) {
                        drawArc(
                            color = Color(0xFF4EDEA3),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 16f, cap = StrokeCap.Round)
                        )
                    }

                    // Numeric Badge
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = "AUTHORIZED",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF74829D),
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "System Integration Requirements",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "GeoCapture Pro operates at high precision and requires authentic hardware clearances to verify photo evidence cryptographically.",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    color = Color(0xFFC5C6CD),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Integration Cards Column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Camera
                PermissionCard(
                    title = "Camera Sensor Calibration",
                    description = "Required to capture RAW pixel feed, check optical focus, and record image telemetry.",
                    icon = Icons.Default.Camera,
                    isGranted = hasCamera,
                    onRequest = { showDialogFor = "Camera" }
                )

                // Card 2: Precise Location
                PermissionCard(
                    title = "Precise Satellite Location",
                    description = "Acquires multi-constellation GPS/GLONASS signals to stamp geosecure location coordinate signatures.",
                    icon = Icons.Default.GpsFixed,
                    isGranted = hasLocation,
                    onRequest = { showDialogFor = "Location" }
                )

                // Card 3: Storage Access
                PermissionCard(
                    title = "Secure Vault Storage",
                    description = "Grants local file directory access to commit certified inspection reports to offline storage.",
                    icon = Icons.Default.Storage,
                    isGranted = hasStorage,
                    onRequest = { showDialogFor = "Storage" }
                )

                // Card 4: Push Notification Telemetry
                PermissionCard(
                    title = "Live Sensor Notifications",
                    description = "Required to push telemetry notifications, satellite loss warnings, and integrity background alerts.",
                    icon = Icons.Default.NotificationsActive,
                    isGranted = hasNotifications,
                    onRequest = { showDialogFor = "Notifications" }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Continue Button (Only enabled when all 4 are authorized)
            Button(
                onClick = {
                    if (grantedCount == 4) {
                        onNext()
                    } else {
                        // For a smoother demo flow, we allow bypassing or we can auto-grant other ones
                        hasCamera = true
                        hasLocation = true
                        hasNotifications = true
                        hasStorage = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (grantedCount == 4) Color(0xFF4EDEA3) else Color(0xFF0A192F),
                    contentColor = if (grantedCount == 4) Color(0xFF003824) else Color(0xFF74829D)
                ),
                shape = RoundedCornerShape(24.dp),
                border = if (grantedCount == 4) null else BorderStroke(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (grantedCount == 4) "Proceed to Mission Dashboard" else "Auto-Grant System Access",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }

    // Interactive simulated system requests dialog
    showDialogFor?.let { permissionType ->
        AlertDialog(
            onDismissRequest = { showDialogFor = null },
            containerColor = Color(0xFF1D2022),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFC5C6CD),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield",
                        tint = Color(0xFF76D6D5)
                    )
                    Text(
                        text = "System Authorization Request",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "Allow GeoCapture Pro to access your device's $permissionType system API? This is required for secure high-integrity field inspections.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (permissionType) {
                            "Camera" -> hasCamera = true
                            "Location" -> hasLocation = true
                            "Storage" -> hasStorage = true
                            "Notifications" -> hasNotifications = true
                        }
                        showDialogFor = null
                    }
                ) {
                    Text("ALLOW CAPTURE SECURE", color = Color(0xFF4EDEA3), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogFor = null }) {
                    Text("DENY ACCESS", color = Color(0xFFFFB4AB))
                }
            }
        )
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isGranted) Color(0xFF4EDEA3).copy(alpha = 0.3f) else Color(0xFFB9C7E4).copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFF001E11).copy(alpha = 0.4f) else Color(0xFF0A192F).copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isGranted) Color(0xFF003824) else Color(0xFF323537),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isGranted) Color(0xFF4EDEA3) else Color(0xFFC5C6CD),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Description Label
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isGranted) Color(0xFF4EDEA3) else Color.White
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color(0xFFC5C6CD).copy(alpha = 0.8f),
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Authorization Action/State Button
            Button(
                onClick = { if (!isGranted) onRequest() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) Color(0xFF009466).copy(alpha = 0.15f) else Color(0xFF76D6D5),
                    contentColor = if (isGranted) Color(0xFF4EDEA3) else Color(0xFF003737)
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(32.dp)
                    .align(Alignment.CenterVertically)
            ) {
                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ACTIVE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "GRANT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. TAB 1: CAMERA VIEW FINDER SCREEN
// ==========================================
@Composable
fun CameraScreen(
    flashActive: Boolean,
    gridEnabled: Boolean,
    onFlashToggle: () -> Unit,
    onGridToggle: () -> Unit,
    onCapture: () -> Unit,
    geoTaggedEnabled: Boolean,
    onGeoTaggedToggle: () -> Unit,
    hdrEnabled: Boolean,
    onHdrToggle: () -> Unit,
    goldenRatioEnabled: Boolean,
    onGoldenRatioToggle: () -> Unit,
    selectedResolution: String,
    onResolutionChanged: (String) -> Unit,
    selectedAspectRatio: String,
    onAspectRatioChanged: (String) -> Unit,
    onGalleryShortcutClick: () -> Unit
) {
    var showResolutionDropdown by remember { mutableStateOf(false) }
    var showAspectRatioDropdown by remember { mutableStateOf(false) }
    var isFrontCameraActive by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mock Real Camera Feed Background (substation infrastructure site / front camera view)
        val cameraFeedUrl = if (isFrontCameraActive) {
            "https://lh3.googleusercontent.com/aida-public/AB6AXuBxdQa9m8Kv6AEN1z48X21AkT3iOrsR_nctxCr9n_aPCxPvxS5n_Y8ROSChcvEH494KhYG4Rvz0is9pZsfmBAVX7LYlynO7aCCoED5B7BrI8KeCR2C64YbPqU2zN9gr3G8v8C1OvhMhiJ0BU3bLBrLlkm3GozbuE_9vjIBcObm-XPzPFtZSGO2saVRjDHa6pGtwWpNbMDIrKb3N6zYng8Kt8b92G6ug-espMsNekLUwMlE_0zqlbvh5i_zOUY5Hs7mPyyTBKIzU-768"
        } else {
            "https://lh3.googleusercontent.com/aida-public/AB6AXuDiH4JQsiHafLIZDfxRcuINfSxu4vQ7EgCk-DqTNEGbsYYpCF13rW0pymglJJWUUGDTLizLkmaN0Vs07FlwPQA2WrRh9RNLBkSLSHF2Brgu_3KKa6qImmYeeJpFmIqILqBanQnN7Ma0Raqkl4ruUyDmf64fkdZls0cTbStbG-m3Ohg8AEObuZLHKjKyA1iBgHXAAIm-D3ZUNpAjhKD1GAJjleqSU2oID5XWuXwhmk4AfmcyHw4pR0FVdaS1DJtp6Y_5vtDl751jMi2h"
        }
        AsyncImage(
            model = cameraFeedUrl,
            contentDescription = "Camera Viewfinder Feed",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Cam Grid Lines Overlay
        if (gridEnabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val horizontalThirdOne = size.height / 3
                val horizontalThirdTwo = (size.height / 3) * 2
                val verticalThirdOne = size.width / 3
                val verticalThirdTwo = (size.width / 3) * 2

                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = androidx.compose.ui.geometry.Offset(0f, horizontalThirdOne),
                    end = androidx.compose.ui.geometry.Offset(size.width, horizontalThirdOne),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = androidx.compose.ui.geometry.Offset(0f, horizontalThirdTwo),
                    end = androidx.compose.ui.geometry.Offset(size.width, horizontalThirdTwo),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = androidx.compose.ui.geometry.Offset(verticalThirdOne, 0f),
                    end = androidx.compose.ui.geometry.Offset(verticalThirdOne, size.height),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = androidx.compose.ui.geometry.Offset(verticalThirdTwo, 0f),
                    end = androidx.compose.ui.geometry.Offset(verticalThirdTwo, size.height),
                    strokeWidth = 1f
                )
            }
        }

        // Golden Ratio Grid Overlay
        if (goldenRatioEnabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val horizontalOne = size.height * 0.382f
                val horizontalTwo = size.height * 0.618f
                val verticalOne = size.width * 0.382f
                val verticalTwo = size.width * 0.618f

                drawLine(
                    color = Color(0xFFFFEB3B).copy(alpha = 0.35f),
                    start = androidx.compose.ui.geometry.Offset(0f, horizontalOne),
                    end = androidx.compose.ui.geometry.Offset(size.width, horizontalOne),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFFFFEB3B).copy(alpha = 0.35f),
                    start = androidx.compose.ui.geometry.Offset(0f, horizontalTwo),
                    end = androidx.compose.ui.geometry.Offset(size.width, horizontalTwo),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFFFFEB3B).copy(alpha = 0.35f),
                    start = androidx.compose.ui.geometry.Offset(verticalOne, 0f),
                    end = androidx.compose.ui.geometry.Offset(verticalOne, size.height),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFFFFEB3B).copy(alpha = 0.35f),
                    start = androidx.compose.ui.geometry.Offset(verticalTwo, 0f),
                    end = androidx.compose.ui.geometry.Offset(verticalTwo, size.height),
                    strokeWidth = 1f
                )
            }
        }

        // Camera Center Focus Frame/Reticle
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
                .border(1.dp, Color(0xFF76D6D5).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
        ) {
            // Tiny Center cross hair lines
            Box(
                modifier = Modifier
                    .size(16.dp, 1.dp)
                    .background(Color(0xFF76D6D5))
                    .align(Alignment.Center)
            )
            Box(
                modifier = Modifier
                    .size(1.dp, 16.dp)
                    .background(Color(0xFF76D6D5))
                    .align(Alignment.Center)
            )
        }

        // Top HUD Information Bar & Metadata Overlays
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header Details
                Column {
                    Text(
                        text = "GeoCapture Pro Inspector",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (geoTaggedEnabled) Color(0xFF4EDEA3) else Color(0xFFFF6B6B),
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (geoTaggedEnabled) "GPS: ACTIVE (±3.2m)" else "GPS: SECURE DISABLED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (geoTaggedEnabled) Color(0xFF4EDEA3) else Color(0xFFFF6B6B)
                        )
                    }
                }

                // Status Pill Indicators (GPS Status, WiFi Status, Battery, Time)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Battery
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery",
                            tint = Color(0xFF4EDEA3),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "87%",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    }

                    // Network
                    Icon(
                        imageVector = if (geoTaggedEnabled) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "Network Status",
                        tint = if (geoTaggedEnabled) Color(0xFF4EDEA3) else Color(0xFFFF6B6B),
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(4.dp)
                            .size(14.dp)
                    )

                    // Current Time
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "14:32",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF76D6D5),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transparent Telemetry Data Overlay (Live metadata)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A192F).copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("LIVE GEOPHOTO TELEMETRY", color = Color(0xFF74829D), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Text(
                                text = "LAT: " + (if (geoTaggedEnabled) "34.0522 N" else "SENSORS MASKED"),
                                color = Color(0xFF76D6D5),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "LONG: " + (if (geoTaggedEnabled) "-118.2437 W" else "SENSORS MASKED"),
                                color = Color(0xFF76D6D5),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (geoTaggedEnabled) Color(0xFF009466).copy(alpha = 0.2f) else Color(0xFFFF6B6B).copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    if (geoTaggedEnabled) Color(0xFF009466) else Color(0xFFFF6B6B),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (geoTaggedEnabled) "99/100 INTEGRITY" else "INTEGRITY COMPROMISED",
                                color = if (geoTaggedEnabled) Color(0xFF4EDEA3) else Color(0xFFFF6B6B),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ALTITUDE", color = Color(0xFFC5C6CD), fontSize = 9.sp)
                            Text(
                                text = if (geoTaggedEnabled) "124m MSL" else "--",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("HEADING / PITCH", color = Color(0xFFC5C6CD), fontSize = 9.sp)
                            Text(
                                text = if (geoTaggedEnabled) "145° SE / +4.2°" else "--",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(modifier = Modifier.weight(0.8f)) {
                            Text("SPEED / ROLL", color = Color(0xFFC5C6CD), fontSize = 9.sp)
                            Text(
                                text = if (geoTaggedEnabled) "0 km/h / -1.5°" else "--",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Pin Icon",
                            tint = if (geoTaggedEnabled) Color(0xFF76D6D5) else Color(0xFFFF6B6B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (geoTaggedEnabled) "123 Enterprise Way, Los Angeles, CA 90012, USA" else "Geofenced: Out of secured range",
                            color = Color(0xFFC5C6CD),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Dynamic Live Verification Checklist for Geo Tagged Mode
                    if (geoTaggedEnabled) {
                        Divider(color = Color(0xFF74829D).copy(alpha = 0.2f), thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                LivePill(tag = "✓ GPS Connected", color = Color(0xFF4EDEA3))
                                LivePill(tag = "✓ Internet Connected", color = Color(0xFF4EDEA3))
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LivePill(tag = "✓ High Accuracy", color = Color(0xFF76D6D5))
                                LivePill(tag = "✓ Verified", color = Color(0xFF4EDEA3))
                            }
                        }
                    }
                }
            }
        }

        // Side Vertical Zoom Control Slider (Right side of screen)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .background(Color(0xFF101415).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFFC5C6CD).copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(20.dp))
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(100.dp)
                    .background(Color(0xFF323537), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(y = 40.dp)
                        .background(Color(0xFF76D6D5), CircleShape)
                )
            }
            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(20.dp))
        }

        // Bottom Bar Area containing ready status + secondary row + capture control
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Secure Metadata Toggle & Capture Readiness Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary control: Geo Tagged Mode big toggle
                Card(
                    onClick = onGeoTaggedToggle,
                    colors = CardDefaults.cardColors(
                        containerColor = if (geoTaggedEnabled) Color(0xFF003824).copy(alpha = 0.85f) else Color(0xFF2B1010).copy(alpha = 0.85f)
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .border(
                            1.dp,
                            if (geoTaggedEnabled) Color(0xFF4EDEA3).copy(alpha = 0.4f) else Color(0xFFFF6B6B).copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (geoTaggedEnabled) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                            contentDescription = "Geo Tagged",
                            tint = if (geoTaggedEnabled) Color(0xFF4EDEA3) else Color(0xFFFF6B6B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (geoTaggedEnabled) "GEO MODE: SECURE" else "GEO MODE: DISABLED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = if (geoTaggedEnabled) Color(0xFF4EDEA3) else Color(0xFFFF6B6B)
                        )
                    }
                }

                // Ready Pill status or non-secure pill (Capture Readiness Panel)
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0B0F10).copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                        .border(
                            1.dp,
                            if (geoTaggedEnabled) Color(0xFF4EDEA3).copy(alpha = 0.4f) else Color(0xFFFFB4AB).copy(alpha = 0.3f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (geoTaggedEnabled) Color(0xFF4EDEA3) else Color(0xFFFFB4AB),
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (geoTaggedEnabled) "READY • ACCURACY: ±3.2m" else "ALERT • SECURE SIG DEACTIVATED",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (geoTaggedEnabled) Color(0xFF4EDEA3) else Color(0xFFFFB4AB),
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Professional secondary options bar (Resolution, Aspect Ratio, HDR, Golden Ratio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0F10).copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // HDR secondary switch
                    IconButton(
                        onClick = onHdrToggle,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (hdrEnabled) Color(0xFF003737) else Color.Transparent
                            )
                            .size(36.dp)
                    ) {
                        Text(
                            text = "HDR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (hdrEnabled) Color(0xFF76D6D5) else Color.White
                        )
                    }

                    // Golden Ratio grid toggle
                    IconButton(
                        onClick = onGoldenRatioToggle,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (goldenRatioEnabled) Color(0xFF003737) else Color.Transparent
                            )
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterCenterFocus,
                            contentDescription = "Golden Ratio",
                            tint = if (goldenRatioEnabled) Color(0xFFFFEB3B) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Resolution selector button
                    Box {
                        Button(
                            onClick = { showResolutionDropdown = !showResolutionDropdown },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.08f),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(text = selectedResolution, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", modifier = Modifier.size(12.dp))
                        }
                        DropdownMenu(
                            expanded = showResolutionDropdown,
                            onDismissRequest = { showResolutionDropdown = false },
                            containerColor = Color(0xFF1D2022)
                        ) {
                            listOf("12MP", "24MP", "48MP", "108MP").forEach { resVal ->
                                DropdownMenuItem(
                                    text = { Text(resVal, color = Color.White, fontSize = 12.sp) },
                                    onClick = {
                                        onResolutionChanged(resVal)
                                        showResolutionDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Aspect Ratio selector button
                    Box {
                        Button(
                            onClick = { showAspectRatioDropdown = !showAspectRatioDropdown },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.08f),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(text = selectedAspectRatio, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", modifier = Modifier.size(12.dp))
                        }
                        DropdownMenu(
                            expanded = showAspectRatioDropdown,
                            onDismissRequest = { showAspectRatioDropdown = false },
                            containerColor = Color(0xFF1D2022)
                        ) {
                            listOf("4:3", "16:9", "1:1", "Full").forEach { aspectVal ->
                                DropdownMenuItem(
                                    text = { Text(aspectVal, color = Color.White, fontSize = 12.sp) },
                                    onClick = {
                                        onAspectRatioChanged(aspectVal)
                                        showAspectRatioDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary control actions row (Gallery Shortcut, Grid, Shutter, Switch Cam, Flash)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Gallery Shortcut
                IconButton(
                    onClick = onGalleryShortcutClick,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery Shortcut",
                        tint = Color.White
                    )
                }

                // 2. Regular Grid Toggle Icon Button
                IconButton(
                    onClick = onGridToggle,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = if (gridEnabled) Icons.Default.Grid4x4 else Icons.Default.BorderClear,
                        contentDescription = "Grid Control",
                        tint = if (gridEnabled) Color(0xFF76D6D5) else Color.White
                    )
                }

                // 3. Gigantic Capture Shutter Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .border(4.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { onCapture() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(Color.White, CircleShape)
                    )
                }

                // 4. Switch Camera (Front / Back toggle)
                IconButton(
                    onClick = { isFrontCameraActive = !isFrontCameraActive },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Switch Camera Sensor",
                        tint = if (isFrontCameraActive) Color(0xFF76D6D5) else Color.White
                    )
                }

                // 5. Flash Control Icon Button
                IconButton(
                    onClick = onFlashToggle,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = if (flashActive) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Flash Control",
                        tint = if (flashActive) Color(0xFF76D6D5) else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun LivePill(tag: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = tag,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ==========================================
// 4. CAPTURE SUCCESS INSPECTION SCREEN
// ==========================================
@Composable
fun SuccessScreen(
    onRetake: () -> Unit,
    onSaveToVault: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101415))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header success info
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Capture Success!",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4EDEA3),
                    fontSize = 20.sp
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF4EDEA3).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("VERIFIED BY SIGNATURE", color = Color(0xFF4EDEA3), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Image Preview box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f)
                    .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDom0Q0aLbJglQ1el3k6aWjnio_MdnieMTW-mM7DzuhEyi2AqUpvllRDupby6Ti4GqxmuaQtZmLOxMDHIq6Y0ciiH0g_eWPcbNzCeuMykfoempPN1hMDJAdvUfFrSog7zfQ9sw86A6Sv-RB0kmPhIPgpHkjX15Dr2AAdDUzadQp1v-3rYNfY6_9219Sd4NOXlUxN7FH4FwEc5G5O0HWks5Z6mDm5swzhasOi_QNQEp_D_yVvVkpX4CIdcwLvrWTrP1gfOlvYU5wUgUG",
                    contentDescription = "Cap Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top Exif Indicator Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color(0xFF101415).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF76D6D5), CircleShape))
                        Text("RAW DATA COLLECTED", color = Color.White, fontSize = 9.sp)
                    }
                }

                // bottom SHA tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color(0xFF101415).copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SHA-256: 8F3A2C1B...9C4B",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFC5C6CD),
                        fontSize = 9.sp
                    )
                }
            }

            // Trust / Integrity Card with Progress meter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("INTEGRITY SCORE CERTIFICATE", color = Color(0xFF74829D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Verified, contentDescription = "Secure Verify", tint = Color(0xFF4EDEA3), modifier = Modifier.size(18.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("98", color = Color(0xFF4EDEA3), fontSize = 42.sp, fontWeight = FontWeight.Bold)
                        Text("/ 100", color = Color(0xFFC5C6CD), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    LinearProgressIndicator(
                        progress = 0.98f,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF4EDEA3),
                        trackColor = Color(0xFF323537)
                    )

                    Text(
                        text = "GEOSPATIAL ALIGNMENT CONFIDENCE: 100% OPTIMAL",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFC5C6CD).copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }

            // Location Context Information Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("LOCATION EXIF METADATA", color = Color(0xFF74829D), fontSize = 10.sp, fontWeight = FontWeight.Bold)

                    // Dummy Map Graphic Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color(0xFF323537), RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCk3gHkYjjCx-1ouD03WUfstJSjMc6ray-FaO3M2p5LbSPeWH4J5kqsvEtakvjjistC1eTyGTOfBxiCDLIpASclr7yN_pPJ7pNKTLFDlGrFF1LPlzYQO7J2zYZYBviPNFC2Wox5fmzU0FQA7VtPHUtCT7OpR4laLXzPYCr4ZeWNP1OT2sBofJI8jB9l6lXfu3P26W-JUG_lHQDkdCUcnLq-7iOcD3tcnIV0PSIjZ3CpZ4lUd0WSNkzKj85ncFMzDWTvSwcYkHG7NF0y",
                            contentDescription = "Map Snap",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Port Sector 7B, Industrial Way",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "37.7749° N, 122.4194° W",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF76D6D5),
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ELEVATION", color = Color(0xFFC5C6CD), fontSize = 10.sp)
                            Text("14.2m AMSL", color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("TIMESTAMP", color = Color(0xFFC5C6CD), fontSize = 10.sp)
                            Text("Oct 24, 14:32:05", color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // EXIF details columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Signal Tag
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("SIGNAL", color = Color(0xFF74829D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("5G • -84 dBm", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
                // Precision Tag
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("ACCURACY", color = Color(0xFF74829D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("±0.4m (RTK)", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // Buttons: Retake and Save to Vault
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRetake,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF272A2C)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Retake", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retake", color = Color.White)
                }

                Button(
                    onClick = onSaveToVault,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4EDEA3)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1.5f).height(48.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = "Vault", tint = Color(0xFF003824))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save to Vault", color = Color(0xFF003824), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==========================================
// 5. TAB 2: PORTFOLIO GALLERY SCREEN
// ==========================================
@Composable
fun GalleryScreen(
    records: List<InspectionRecord>,
    isGridView: Boolean,
    onViewToggle: (Boolean) -> Unit,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    activeFilter: String,
    onFilterChanged: (String) -> Unit,
    onRecordSelected: (InspectionRecord) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101415))
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Search & Layout Toggle row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Input box filled style
            TextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                placeholder = { Text("Search by address, id, or category...", color = Color(0xFFC5C6CD).copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFFC5C6CD)) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF191C1E),
                    unfocusedContainerColor = Color(0xFF191C1E),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Grid VS List View Selector Button
            IconButton(
                onClick = { onViewToggle(!isGridView) },
                modifier = Modifier
                    .background(Color(0xFF1D2022), RoundedCornerShape(12.dp))
                    .size(52.dp)
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.FormatListBulleted else Icons.Default.GridView,
                    contentDescription = "Layout View Switcher",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Period Filtering Tab chips
        val filterChipsList = listOf("Today", "Week", "Month", "Custom")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterChipsList) { chipName ->
                val isActive = chipName == activeFilter
                val chipBkColor = if (isActive) Color(0xFF007F7F) else Color(0xFF1D2022)
                val chipTxColor = if (isActive) Color(0xFFDDFFFE) else Color(0xFFC5C6CD)
                val chipBorder = if (isActive) BorderStroke(1.dp, Color(0xFF76D6D5)) else BorderStroke(1.dp, Color(0xFF44474D))

                Box(
                    modifier = Modifier
                        .background(chipBkColor, RoundedCornerShape(20.dp))
                        .border(chipBorder, RoundedCornerShape(20.dp))
                        .clickable { onFilterChanged(chipName) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chipName,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = chipTxColor,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Filtering records list depending on search query
        val queryLower = searchQuery.lowercase()
        val filteredRecords = records.filter { record ->
            record.id.lowercase().contains(queryLower) ||
            record.address.lowercase().contains(queryLower) ||
            record.category.lowercase().contains(queryLower)
        }

        // Gallery Items Grid / List
        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFF74829D), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Matching Survey Records", color = Color(0xFFC5C6CD))
                }
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredRecords) { record ->
                        GalleryGridItem(record = record, onClick = { onRecordSelected(record) })
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredRecords) { record ->
                        GalleryListItem(record = record, onClick = { onRecordSelected(record) })
                    }
                }
            }
        }
    }
}

// ==========================================
// 5A. GALLERY GRID GRID-ITEM COMPONENT
// ==========================================
@Composable
fun GalleryGridItem(record: InspectionRecord, onClick: () -> Unit) {
    val scorePercentage = (record.integrityScore * 100).toInt()
    val integrityColor = if (record.isFlagged) Color(0xFFFFB4AB) else Color(0xFF4EDEA3)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1D2022).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .border(
                1.dp, 
                if (record.isFlagged) Color(0xFFFFB4AB).copy(alpha = 0.3f) else Color(0xFFB9C7E4).copy(alpha = 0.15f), 
                RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
            ) {
                AsyncImage(
                    model = record.imageUrl,
                    contentDescription = "Thumbnail",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Integrity Label Pill Top Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color(0xFF101415).copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(5.dp).background(integrityColor, CircleShape))
                        Text(
                            text = "$scorePercentage%",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = integrityColor,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = record.id,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF74829D),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = record.address,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.date,
                        color = Color(0xFFC5C6CD),
                        fontSize = 11.sp
                    )
                    Text(
                        text = record.time,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFC5C6CD),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 5B. GALLERY LIST LIST-ITEM COMPONENT
// ==========================================
@Composable
fun GalleryListItem(record: InspectionRecord, onClick: () -> Unit) {
    val scorePercentage = (record.integrityScore * 100).toInt()
    val integrityColor = if (record.isFlagged) Color(0xFFFFB4AB) else Color(0xFF4EDEA3)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1D2022).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .border(
                1.dp, 
                if (record.isFlagged) Color(0xFFFFB4AB).copy(alpha = 0.3f) else Color(0xFFB9C7E4).copy(alpha = 0.15f), 
                RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = record.imageUrl,
                contentDescription = "Thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = record.id,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF74829D),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(modifier = Modifier.size(4.dp).background(integrityColor, CircleShape))
                Text(
                    text = "$scorePercentage% Integrity",
                    fontFamily = FontFamily.Monospace,
                    color = integrityColor,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = record.address,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${record.date}  •  ${record.time}",
                color = Color(0xFFC5C6CD),
                fontSize = 11.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Explore Item",
            tint = Color(0xFF8F9097)
        )
    }
}

// ==========================================
// 6. DETAILED SPECIFICATION VIEW / DETAIL SCREEN
// ==========================================
@Composable
fun DetailScreen(
    record: InspectionRecord,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val scorePercentage = (record.integrityScore * 100).toInt()
    val integrityColor = if (record.isFlagged) Color(0xFFFFB4AB) else Color(0xFF4EDEA3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101415).copy(alpha = 0.98f))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back Gallery", tint = Color.White)
                }
                Text(
                    text = record.id,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF76D6D5),
                    fontSize = 16.sp
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Record", tint = Color(0xFFFFB4AB))
                }
            }

            // High Resolution image preview with metadata tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = record.imageUrl,
                    contentDescription = "Exif Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(Color(0xFF101415).copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "GPS ELEVATION: ${record.altitude}",
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
            }

            // Exif Share Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { /* Simulated Share */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007F7F)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Info", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { /* Simulated PDF */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF272A2C)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Export", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF", color = Color.White, fontSize = 12.sp)
                }
            }

            // Spatial Exif Telemetry Details Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("GEOSPATIAL TELEMETRY", color = Color(0xFF74829D), fontSize = 10.sp, fontWeight = FontWeight.Bold)

                    DetailRow(label = "GPS Coordinates", value = "${record.latitude}° N, ${record.longitude}° W")
                    DetailRow(label = "Altitude (MSL)", value = record.altitude)
                    DetailRow(label = "Compass Heading", value = record.heading)
                    DetailRow(label = "Sensor Model", value = record.sensor)
                    DetailRow(label = "ISO Speed / Shutter", value = "${record.iso} / ${record.shutter}")
                }
            }

            // Local GIS Map snapshot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("LOCATION GIS MAP", color = Color(0xFF74829D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Accuracy: ±0.35m", color = Color(0xFF76D6D5), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCk3gHkYjjCx-1ouD03WUfstJSjMc6ray-FaO3M2p5LbSPeWH4J5kqsvEtakvjjistC1eTyGTOfBxiCDLIpASclr7yN_pPJ7pNKTLFDlGrFF1LPlzYQO7J2zYZYBviPNFC2Wox5fmzU0FQA7VtPHUtCT7OpR4laLXzPYCr4ZeWNP1OT2sBofJI8jB9l6lXfu3P26W-JUG_lHQDkdCUcnLq-7iOcD3tcnIV0PSIjZ3CpZ4lUd0WSNkzKj85ncFMzDWTvSwcYkHG7NF0y",
                            contentDescription = "Map Snap Detail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = record.address,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Validation indicators
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("VALIDATION RESULTS", color = Color(0xFF74829D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Satellite Precision Gauge", color = Color.White, fontSize = 13.sp)
                        Text("$scorePercentage%", color = integrityColor, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("E2E Cryptographic Certificate", color = Color.White, fontSize = 13.sp)
                        Text("PASSED", color = Color(0xFF4EDEA3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFFC5C6CD), fontSize = 13.sp)
        Text(text = value, color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
    }
}

// ==========================================
// 7. TAB 3: SATELLITE/MAP GIS SCREEN VIEW
// ==========================================
@Composable
fun MapViewScreen(
    showDetail: Boolean,
    onToggleDetail: () -> Unit,
    onViewDetailsClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // High Contrast Satellite Map Background View
        AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCk3gHkYjjCx-1ouD03WUfstJSjMc6ray-FaO3M2p5LbSPeWH4J5kqsvEtakvjjistC1eTyGTOfBxiCDLIpASclr7yN_pPJ7pNKTLFDlGrFF1LPlzYQO7J2zYZYBviPNFC2Wox5fmzU0FQA7VtPHUtCT7OpR4laLXzPYCr4ZeWNP1OT2sBofJI8jB9l6lXfu3P26W-JUG_lHQDkdCUcnLq-7iOcD3tcnIV0PSIjZ3CpZ4lUd0WSNkzKj85ncFMzDWTvSwcYkHG7NF0y",
            contentDescription = "Satellite High Resolution Map",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Maps Overlay Glowing Heat Map Effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF4EDEA3).copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        // Map Float Top HUD Toggle Filter options
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF0A192F).copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Heat map active tab selector
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF007F7F), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Layers, contentDescription = "Layers", tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Heat Map", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Timeline passive filter
                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = Color(0xFFC5C6CD), modifier = Modifier.size(16.dp))
                        Text("Timeline Filter", color = Color(0xFFC5C6CD), fontSize = 11.sp)
                    }
                }
            }
        }

        // Point Cluster Markers (Pins with counts)
        // Pin 1: size 24 (North East)
        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = 200.dp)
                .background(Color(0xFF007F7F).copy(alpha = 0.85f), CircleShape)
                .border(2.dp, Color(0xFF76D6D5), CircleShape)
                .size(44.dp)
                .clickable { onToggleDetail() },
            contentAlignment = Alignment.Center
        ) {
            Text("24", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
        }

        // Pin 2: size 142 (South West)
        Box(
            modifier = Modifier
                .offset(x = 180.dp, y = 420.dp)
                .background(Color(0xFF0F1415).copy(alpha = 0.9f), CircleShape)
                .border(2.dp, Color(0xFF4EDEA3), CircleShape)
                .size(56.dp)
                .clickable { onToggleDetail() },
            contentAlignment = Alignment.Center
        ) {
            Text("142", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF4EDEA3), fontSize = 15.sp)
        }

        // Detail Popup Overlay Slide In Card (Active state)
        AnimatedVisibility(
            visible = showDetail,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp)
                    .background(Color(0xFF0A192F).copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                // Cross dismiss button
                IconButton(
                    onClick = { onToggleDetail() },
                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Popup", tint = Color(0xFF8F9097))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Small thumbnail photo
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF4EDEA3).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDom0Q0aLbJglQ1el3k6aWjnio_MdnieMTW-mM7DzuhEyi2AqUpvllRDupby6Ti4GqxmuaQtZmLOxMDHIq6Y0ciiH0g_eWPcbNzCeuMykfoempPN1hMDJAdvUfFrSog7zfQ9sw86A6Sv-RB0kmPhIPgpHkjX15Dr2AAdDUzadQp1v-3rYNfY6_9219Sd4NOXlUxN7FH4FwEc5G5O0HWks5Z6mDm5swzhasOi_QNQEp_D_yVvVkpX4CIdcwLvrWTrP1gfOlvYU5wUgUG",
                            contentDescription = "Point Thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SITE: SF-772", color = Color(0xFF4EDEA3), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("±0.24m Accuracy", color = Color(0xFFC5C6CD), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "242 Terminal Dr, Houston, TX",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Water Filtration Sub-Assembly System", color = Color(0xFFC5C6CD), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onViewDetailsClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF76D6D5)),
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("View Asset Details", color = Color(0xFF003737), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Float right side mapping controls Zoom and Re-center
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier.background(Color(0xFF101415).copy(alpha = 0.7f), CircleShape).size(48.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Get Position", tint = Color(0xFF76D6D5))
            }
            IconButton(
                onClick = {},
                modifier = Modifier.background(Color(0xFF101415).copy(alpha = 0.7f), CircleShape).size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom map in", tint = Color.White)
            }
            IconButton(
                onClick = {},
                modifier = Modifier.background(Color(0xFF101415).copy(alpha = 0.7f), CircleShape).size(48.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom map out", tint = Color.White)
            }
        }
    }
}

// ==========================================
// 8. TAB 4: SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(
    gridOverlay: Boolean,
    onGridToggle: (Boolean) -> Unit,
    watermark: Boolean,
    onWatermarkToggle: (Boolean) -> Unit,
    darkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    biometric: Boolean,
    onBiometricToggle: (Boolean) -> Unit,
    accuracyVal: Float,
    onAccuracyChanged: (Float) -> Unit,
    onOpenAnalytics: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101415))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Profile Card Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1D2022).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .border(2.dp, Color(0xFF76D6D5).copy(alpha = 0.4f), CircleShape)
                            .clip(CircleShape)
                    ) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDoDCCOf0Kbzo0KwRjll6VH65VuoNYAU8UI7X4k6j-tK2frv2euwu4c75-G_DeaacbsA2O1GAY2C58kU2PRvwZmAKlRB_q4Fj1D-p4WOOI5UGWOmAkTAWke6Tdzrf4YSGX0f3DmEVu8kC8b59281nAMSX4UKp2UQY6D31pTC4J5pAMfJM5bsB8UVKR0LPibJ1Ocdm5dB4I6Igq7VyUmIhwTQinW6W_t0_9TUmviZtZX2MU_KbE29Y7bQSYshgISnPNco1FdnCRWTjn8",
                            contentDescription = "User Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column {
                        Text(text = "Alex Chen", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(text = "Senior Field Inspector  •  ID: 8842", color = Color(0xFFC5C6CD), fontSize = 12.sp)
                    }
                }
            }

            // Group: Capture & Field Controls
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CAPTURE & FIELD PREFERENCES",
                    color = Color(0xFF74829D),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column {
                        // Analytics Overview Link button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenAnalytics() }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.BarChart, contentDescription = "Chart", tint = Color(0xFF76D6D5))
                                Column {
                                    Text("Analytics Dashboard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Review total captures & index health stats", color = Color(0xFFC5C6CD), fontSize = 11.sp)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Right", tint = Color(0xFF8F9097))
                        }

                        Divider(color = Color(0xFF44474D).copy(alpha = 0.3f))

                        // Grid Overlay parameter
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Cam", tint = Color(0xFF76D6D5))
                                Text("Camera Grid Lines", color = Color.White, fontSize = 14.sp)
                            }
                            Switch(
                                checked = gridOverlay,
                                onCheckedChange = onGridToggle,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF76D6D5), checkedTrackColor = Color(0xFF007F7F))
                            )
                        }

                        Divider(color = Color(0xFF44474D).copy(alpha = 0.3f))

                        // Dynamic Watermark parameter
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.BrandingWatermark, contentDescription = "Watermerk", tint = Color(0xFF76D6D5))
                                Text("Dynamic Watermark", color = Color.White, fontSize = 14.sp)
                            }
                            Switch(
                                checked = watermark,
                                onCheckedChange = onWatermarkToggle,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF76D6D5), checkedTrackColor = Color(0xFF007F7F))
                            )
                        }

                        Divider(color = Color(0xFF44474D).copy(alpha = 0.3f))

                        // GPS accuracy slider control
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Pin Icon", tint = Color(0xFF76D6D5))
                                    Text("GPS Accuracy Limit", color = Color.White, fontSize = 14.sp)
                                }
                                val formatLimit = String.format("%.1fm", accuracyVal)
                                Text(formatLimit, color = Color(0xFF76D6D5), fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Slider(
                                value = accuracyVal,
                                onValueChange = onAccuracyChanged,
                                valueRange = 1f..50f,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFF76D6D5), activeTrackColor = Color(0xFF007F7F))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("1m (HIGH)", color = Color(0xFFC5C6CD).copy(alpha = 0.5f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("50m (LOW)", color = Color(0xFFC5C6CD).copy(alpha = 0.5f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            // Group: System & Security
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SYSTEM & SECURITY",
                    color = Color(0xFF74829D),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column {
                        // Biometric unlock
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.Fingerprint, contentDescription = "Fng", tint = Color(0xFF76D6D5))
                                Text("Biometric FaceID Unlock", color = Color.White, fontSize = 14.sp)
                            }
                            Switch(
                                checked = biometric,
                                onCheckedChange = onBiometricToggle,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF76D6D5), checkedTrackColor = Color(0xFF007F7F))
                            )
                        }

                        Divider(color = Color(0xFF44474D).copy(alpha = 0.3f))

                        // Theme Dark Mode
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.DarkMode, contentDescription = "Dark", tint = Color(0xFF76D6D5))
                                Text("Full Dark Theme", color = Color.White, fontSize = 14.sp)
                            }
                            Switch(
                                checked = darkMode,
                                onCheckedChange = onDarkModeToggle,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF76D6D5), checkedTrackColor = Color(0xFF007F7F))
                            )
                        }
                    }
                }
            }

            // Metadata info
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ENTERPRISE INSPECTIONS V4.2.0-STABLE",
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFC5C6CD).copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "E2E ENCRYPTED • GIS MAP VERIFIED",
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFC5C6CD).copy(alpha = 0.3f),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ==========================================
// 9. SYSTEM HEALTH ANALYTICS DASHBOARD
// ==========================================
@Composable
fun AnalyticsDashboard(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101415))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App bar back linking
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back Settings", tint = Color.White)
            }
            Text(
                text = "Analytics Overview",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp
            )
        }

        // Timeline Filter togglable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF191C1E), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF272A2C), RoundedCornerShape(8.dp))
                    .weight(1f)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("7D", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("30D", color = Color(0xFFC5C6CD), fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("90D", color = Color(0xFFC5C6CD), fontSize = 11.sp)
            }
        }

        // Total Photos block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTAL PICTURE DATABASE", color = Color(0xFF74829D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("1,240 Assets", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
                Text("+12% This Week", color = Color(0xFF4EDEA3), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }

        // Weekly Peak Rounded Corner Bar Graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DAILY INSPECTIONS PEAK", color = Color(0xFF74829D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF76D6D5), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Current", color = Color(0xFFC5C6CD), fontSize = 10.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF272A2C), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous", color = Color(0xFFC5C6CD), fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Custom charts layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val peaksDataHeight = listOf(0.4f, 0.7f, 0.35f, 0.9f, 0.6f, 0.25f, 0.15f)
                    val daysOfWeekStr = listOf("M", "T", "W", "T", "F", "S", "S")

                    peaksDataHeight.forEachIndexed { index, heightFactor ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .fillMaxHeight(heightFactor)
                                    .background(Color(0xFF76D6D5), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(daysOfWeekStr[index], color = Color(0xFFC5C6CD), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Global Integrity Index Circular Gauge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A192F).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFB9C7E4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("GLOBAL SECURITY INDEX", color = Color(0xFF74829D), fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("High Integrity: 88%", color = Color(0xFF4EDEA3), fontSize = 13.sp)
                        Text("Validated Nodes: 10%", color = Color(0xFF76D6D5), fontSize = 13.sp)
                        Text("Mismatched/Flagged: 2%", color = Color(0xFFFFB4AB), fontSize = 13.sp)
                    }

                    // Progress circle custom drawings via Canvas
                    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(90.dp)) {
                            // Background track
                            drawArc(
                                color = Color(0xFF323537),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Clean verified progress
                            drawArc(
                                color = Color(0xFF4EDEA3),
                                startAngle = -90f,
                                sweepAngle = 338.4f, // 94.2%
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("94.2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("INDEX", color = Color(0xFFC5C6CD).copy(alpha = 0.7f), fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
