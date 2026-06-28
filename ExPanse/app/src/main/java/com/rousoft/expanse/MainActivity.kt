package com.rousoft.expanse

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler // TAMBAHAN PENTING UNTUK TOMBOL BACK
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rousoft.expanse.database.syncOfflineDataToFirebase
import kotlinx.coroutines.launch

// IMPORT UNTUK DATABASE
import com.rousoft.expanse.database.AppDatabase
import java.text.NumberFormat
import java.util.Locale

// Deklarasi Font Global ExPanse
val montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedDrawerItem by remember { mutableIntStateOf(0) }

    // STATE UNTUK POP-UP LOGOUT DAN LAYAR SCAN
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showScanScreen by remember { mutableStateOf(false) }

    // ==========================================
    // MENU DRAWER (LACI SAMPING)
    // ==========================================
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                Column(modifier = Modifier.fillMaxHeight().padding(24.dp)) {
                    Text(stringResource(R.string.app_name), fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = montserrat, color = Color.Black)
                    Spacer(modifier = Modifier.height(32.dp))

                    DrawerMenuItem(
                        icon = Icons.Outlined.Dashboard, title = stringResource(R.string.drawer_main_menu),
                        isSelected = selectedDrawerItem == 0,
                        onClick = { selectedDrawerItem = 0; scope.launch { drawerState.close() } }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DrawerMenuItem(
                        icon = Icons.Outlined.ReceiptLong, title = stringResource(R.string.drawer_all_expenses),
                        isSelected = selectedDrawerItem == 1,
                        onClick = { selectedDrawerItem = 1; scope.launch { drawerState.close() } }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DrawerMenuItem(
                        icon = Icons.Outlined.Settings, title = stringResource(R.string.drawer_setting),
                        isSelected = selectedDrawerItem == 2,
                        onClick = { selectedDrawerItem = 2; scope.launch { drawerState.close() } }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DrawerMenuItem(
                        icon = Icons.Outlined.ChatBubbleOutline, title = stringResource(R.string.drawer_feedback),
                        isSelected = selectedDrawerItem == 3,
                        onClick = { selectedDrawerItem = 3; scope.launch { drawerState.close() } }
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    DrawerMenuItem(
                        icon = Icons.AutoMirrored.Outlined.Logout, title = stringResource(R.string.drawer_logout),
                        isSelected = false, textColor = Color(0xFFE53935), iconColor = Color(0xFFE53935),
                        onClick = {
                            showLogoutDialog = true
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        // ==========================================
        // KERANGKA UTAMA HALAMAN
        // ==========================================

        val topBarTitle = when (selectedDrawerItem) {
            1 -> stringResource(R.string.drawer_all_expenses)
            2 -> stringResource(R.string.drawer_setting)
            3 -> stringResource(R.string.drawer_feedback)
            else -> stringResource(R.string.app_name)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(topBarTitle, fontWeight = FontWeight.Bold, fontFamily = montserrat) },
                    navigationIcon = {
                        if (selectedDrawerItem in 1..3) {
                            IconButton(onClick = { selectedDrawerItem = 0 }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.btn_scan), fontWeight = FontWeight.Bold, fontFamily = montserrat) },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan") },
                    onClick = { showScanScreen = true },
                    containerColor = Color(0xFFFFA500),
                    contentColor = Color.Black
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFFFFF3E0), tonalElevation = 8.dp) {
                    val tabs = listOf(stringResource(R.string.tab_home), stringResource(R.string.tab_analysis), stringResource(R.string.tab_report))
                    val icons = listOf(Icons.Outlined.Home, Icons.Outlined.Analytics, Icons.Outlined.Description)

                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = title) },
                            label = { Text(title, fontFamily = montserrat) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFFFCC80), selectedIconColor = Color.Black, unselectedIconColor = Color.Gray)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8F9FA))
            ) {
                when (selectedDrawerItem) {
                    1 -> AllExpensesContent() // Kalau merah biarkan dulu, atau pastikan fungsinya ada
                    2 -> SettingContent()
                    3 -> FeedbackContent()
                    else -> {
                        when (selectedTab) {
                            0 -> HomeContent()
                            1 -> AnalysisScreen()
                            2 -> ReportScreen()
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // OVERLAY: HALAMAN FULL-SCREEN SCAN KAMERA
    // ==========================================
    if (showScanScreen) {
        // MENCEGAH STUCK: Memastikan tombol fisik HP mengembalikan ke layar utama
        BackHandler { showScanScreen = false }
        ScanScreen(onBack = { showScanScreen = false })
    }

    // ==========================================
    // OVERLAY: KOMPONEN POP-UP ALERT DIALOG LOGOUT
    // ==========================================
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.dialog_logout_title),
                    fontWeight = FontWeight.Bold,
                    fontFamily = montserrat
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_logout_message),
                    fontFamily = montserrat
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.drawer_logout),
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold,
                        fontFamily = montserrat
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = stringResource(R.string.dialog_cancel), color = Color.Black, fontFamily = montserrat)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ==========================================
// UI UNTUK TAB HOME (YANG SUDAH TERHUBUNG DATABASE)
// ==========================================
@Composable
fun HomeContent() {
    val context = LocalContext.current

    // 1. Inisialisasi Database
    val db = remember { AppDatabase.getDatabase(context) }
    val transactionDao = db.transactionDao()

    LaunchedEffect(Unit) {
        // Fungsi ini akan berjalan otomatis di background
        syncOfflineDataToFirebase(transactionDao)
    }

    // 2. Ambil data secara REAL-TIME dari database
    val transactions by transactionDao.getAllTransactions().collectAsState(initial = emptyList())
    val totalExpense by transactionDao.getTotalExpense().collectAsState(initial = 0.0)

    // 3. Format angka menjadi format Rupiah yang rapi
    val formatter = NumberFormat.getInstance(Locale("id", "ID"))
    val formattedTotal = formatter.format(totalExpense ?: 0.0)

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.Top) {
            Text("RP", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = montserrat, modifier = Modifier.padding(top = 8.dp))
            Spacer(modifier = Modifier.width(4.dp))
            // NOMINAL TOTAL SEKARANG HIDUP (Tidak "0" lagi)
            Text(formattedTotal, fontSize = 42.sp, fontWeight = FontWeight.Bold, fontFamily = montserrat)
        }

        Row(
            modifier = Modifier.background(Color(0xFFFFE0B2), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${stringResource(R.string.label_used_budget)} Rp $formattedTotal", fontSize = 12.sp, color = Color(0xFFE65100), fontWeight = FontWeight.SemiBold, fontFamily = montserrat)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color.Red, modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(stringResource(R.string.title_expenses_today), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = montserrat)
        Spacer(modifier = Modifier.height(16.dp))

        // LIST TRANSAKSI YANG OTOMATIS BERTAMBAH
        LazyColumn {
            if (transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.empty_expenses), color = Color.Gray, fontSize = 14.sp, fontFamily = montserrat)
                    }
                }
            } else {
                items(transactions) { transaction ->
                    TransactionItem(
                        title = transaction.title,
                        amount = "Rp ${formatter.format(transaction.amount)}"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)), shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.btn_view_daily_report), color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = montserrat)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Black)
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun TransactionItem(title: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = montserrat)
        Box(modifier = Modifier.background(Color(0xFFFFCA28), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = montserrat)
        }
    }
}

@Composable
fun DrawerMenuItem(icon: ImageVector, title: String, isSelected: Boolean = false, textColor: Color = Color.Black, iconColor: Color = Color.DarkGray, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFFFFE0B2) else Color.Transparent
    val finalTextColor = if (isSelected) Color(0xFFE65100) else textColor
    val finalIconColor = if (isSelected) Color(0xFFE65100) else iconColor
    Row(
        modifier = Modifier.fillMaxWidth().background(backgroundColor, RoundedCornerShape(12.dp)).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = finalIconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = finalTextColor, fontFamily = montserrat, modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() { MainScreen() }