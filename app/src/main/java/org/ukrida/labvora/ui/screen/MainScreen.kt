package org.ukrida.labvora.ui.screen

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.ukrida.labvora.R
import org.ukrida.labvora.ui.navigation.BottomNav
import org.ukrida.labvora.viewmodel.BookingViewModel
import org.ukrida.labvora.viewmodel.CartViewModel
import org.ukrida.labvora.viewmodel.HistoryViewModel
import org.ukrida.labvora.viewmodel.ResultViewModel
import org.ukrida.labvora.viewmodel.UserViewModel

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    role: String,
    navController: NavHostController,
    userViewModel: UserViewModel,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit = onLogout
) {
    val innerNavController = rememberNavController()
    val context = LocalContext.current
    val userId = userViewModel.currentUser.value?.id ?: 0
    // ponytail: instance per akun agar cart/history tak bocor saat ganti user
    val bookingViewModel = remember(userId) { BookingViewModel() }
    val historyViewModel = remember(userId) { HistoryViewModel() }
    val resultViewModel = remember(userId) { ResultViewModel() }
    val cartViewModel = remember(userId) { CartViewModel() }
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()

    // 3 Halaman Utama User mendukung Swipe Navigation (HorizontalPager)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        if (userId > 0) {
            cartViewModel.initCartForUser(context, userId)
        }
    }
    // ponytail: baseline max id dicatat SEBELUM refetch, agar pill/stripe
    // "baru" hanya untuk order yang dibuat pada pemesanan ini
    fun snapshotHighlightBaseline() {
        val ids = (historyViewModel.pendingOrders.value + historyViewModel.historyList.value)
            .map { it.id }
        historyViewModel.highlightAboveId.value = ids.maxOrNull() ?: 0
    }
    LaunchedEffect(userId, bookingViewModel.isOrderCompleted) {
        if (userId > 0 && bookingViewModel.isOrderCompleted) {
            snapshotHighlightBaseline()
            historyViewModel.getHistoryList(userId)
        } else if (userId > 0) {
            historyViewModel.getHistoryList(userId)
        }
    }
    // ponytail: checkout keranjang tidak menyentuh BookingViewModel, jadi
    // refetch + baseline di-trigger dari flag sukses CartViewModel. Tanpa ini
    // badge "ada yang baru" tidak muncul setelah Tutup dari modal keranjang.
    LaunchedEffect(userId, cartViewModel.showCheckoutSuccessModal.value) {
        if (userId > 0 && cartViewModel.showCheckoutSuccessModal.value) {
            snapshotHighlightBaseline()
            historyViewModel.getHistoryList(userId)
        }
    }
    val currentRoute = navBackStackEntry?.destination?.route ?: "main_tabs"

    // Sinkronisasi tab aktif BottomNav
    val activeTabRoute = when {
        currentRoute == "main_tabs" -> {
            when (pagerState.currentPage) {
                0 -> "home"
                1 -> "listtest"
                else -> "user"
            }
        }
        else -> currentRoute
    }

    // Top Bar ditampilkan di 3 tab utama: Beranda, Tes Lab, dan Profil
    val showTopBar = currentRoute == "main_tabs"

    Scaffold(
        containerColor = Color.White,
        // ================= TOP BAR =================
        topBar = {
            if (showTopBar) {
                Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(Color.White)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Left: Logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.height(40.dp),
                    contentScale = ContentScale.Fit
                )

                // Center: Logo name
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logoname),
                        contentDescription = "Labvora Logo Name",
                        modifier = Modifier.height(20.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Right: Cart Icon with Item Badge Counter
                IconButton(
                    onClick = { innerNavController.navigate("cart") },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    BadgedBox(
                        badge = {
                            if (cartViewModel.cartItemCount > 0) {
                                Badge(
                                    containerColor = Color(0xFFF75F65),
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = cartViewModel.cartItemCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Keranjang Saya",
                            tint = Color(0xFF1F2937)
                        )
                    }
                }
                }
            }
        },

        // ================= BOTTOM NAV =================
        // ponytail: hide di layar dgn bottom bar sendiri (history, result, cart,
        // orderstatus, profileedit, faq) sesuai commit lama; privacypolicy route sudah dihapus
        bottomBar = {
            if (currentRoute != "history" && !currentRoute.startsWith("result") && currentRoute != "cart" && currentRoute != "orderstatus" && currentRoute != "profileedit" && currentRoute != "faq") {
                BottomNav(
                    navController = innerNavController,
                    role = role,
                    selectedTabRoute = if (currentRoute == "main_tabs") activeTabRoute else null,
                    onTabSelected = { targetRoute ->
                        if (currentRoute != "main_tabs") {
                            innerNavController.popBackStack("main_tabs", false)
                        }
                        val targetPage = when (targetRoute) {
                            "home" -> 0
                            "listtest" -> 1
                            "user" -> 2
                            else -> 0
                        }
                        coroutineScope.launch {
                            pagerState.scrollToPage(targetPage)
                        }
                    }
                )
            }
        }

    ) { padding ->

        NavHost(
            navController = innerNavController,
            startDestination = "main_tabs",
            // ponytail: tanpa animasi slide — bottomBar hide-nya instan,
            // kalau layar pakai slide, ikon hilang duluan & layar nyusul (kelihatan glitch)
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
            modifier = Modifier.padding(
                top = if (showTopBar) padding.calculateTopPadding() else 0.dp,
                bottom = padding.calculateBottomPadding()
            )
        ) {

            // Main Swipeable Pager for 3 Primary Tabs (Beranda, Tes Lab, Profil)
            composable("main_tabs") {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> {
                            HomeScreen(
                                userViewModel = userViewModel,
                                bookingViewModel = bookingViewModel,
                                historyViewModel = historyViewModel,
                                onNavigateToListTest = {
                                    coroutineScope.launch {
                                        pagerState.scrollToPage(1)
                                    }
                                },
                                onNavigateToDetail = { testId ->
                                    innerNavController.navigate("detailtest/$testId")
                                },
                                onNavigateToHistory = {
                                    innerNavController.navigate("history")
                                },
                                onNavigateToResult = { bookingId, testId, date ->
                                    val dateArg = if (date != null) "?date=${android.net.Uri.encode(date)}" else ""
                                    innerNavController.navigate("result/$bookingId/$testId$dateArg")
                                },
                                onNavigateToProfile = {
                                    coroutineScope.launch {
                                        pagerState.scrollToPage(2)
                                    }
                                },
                                onNavigateToOrderStatus = {
                                    innerNavController.navigate("orderstatus")
                                }
                            )
                        }
                        1 -> {
                            ListTestScreen(
                                bookingViewModel = bookingViewModel,
                                onNavigateToDetail = { testId ->
                                    innerNavController.navigate("detailtest/$testId")
                                }
                            )
                        }
                        2 -> {
                            ProfileScreen(
                                viewModel = userViewModel,
                                navController = innerNavController,
                                bookingViewModel = bookingViewModel,
                                historyViewModel = historyViewModel,
                                onNavigateToHistory = {
                                    innerNavController.navigate("history")
                                },
                                onNavigateToOrderStatus = {
                                    innerNavController.navigate("orderstatus")
                                },
                                onLogout = onLogout,
                                onDeleteAccount = onDeleteAccount
                            )
                        }
                    }
                }
            }

            composable("history") {
                HistoryScreen(
                    userId = userId,
                    viewModel = historyViewModel,
                    onBack = {
                        innerNavController.popBackStack()
                    },
                    onNavigateToResult = { bookingId, testId, date ->
                        val dateArg = if (date != null) "?date=${android.net.Uri.encode(date)}" else ""
                        innerNavController.navigate("result/$bookingId/$testId$dateArg")
                    }
                )
            }

            composable(
                route = "result/{bookingId}/{testId}?date={date}",
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.IntType },
                    navArgument("testId") { type = NavType.IntType },
                    navArgument("date") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: 0
                val testId = backStackEntry.arguments?.getInt("testId") ?: 1
                val date = backStackEntry.arguments?.getString("date")
                ResultScreen(
                    bookingId = bookingId,
                    testId = testId,
                    resultViewModel = resultViewModel,
                    bookingViewModel = bookingViewModel,
                    gender = userViewModel.currentUser.value?.gender,
                    date = date,
                    onBack = {
                        innerNavController.popBackStack()
                    }
                )
            }

            composable("home") {
                LaunchedEffect(Unit) {
                    innerNavController.popBackStack("main_tabs", false)
                    pagerState.scrollToPage(0)
                }
            }

            composable("listtest") {
                LaunchedEffect(Unit) {
                    innerNavController.popBackStack("main_tabs", false)
                    pagerState.scrollToPage(1)
                }
            }

            composable("user") {
                LaunchedEffect(Unit) {
                    innerNavController.popBackStack("main_tabs", false)
                    pagerState.scrollToPage(2)
                }
            }

            composable("profileedit") {
                ProfileEditScreen(
                    viewModel = userViewModel,
                    navController = innerNavController
                )
            }

            composable("faq") {
                FaqScreen(
                    onBack = {
                        innerNavController.popBackStack()
                    }
                )
            }

            composable(
                route = "detailtest/{testId}",
                arguments = listOf(navArgument("testId") { type = NavType.IntType })
            ) { backStackEntry ->
                val testId = backStackEntry.arguments?.getInt("testId") ?: 1
                DetailTestScreen(
                    testId = testId,
                    bookingViewModel = bookingViewModel,
                    onBack = {
                        innerNavController.popBackStack()
                    },
                    onNavigateToSchedule = {
                        innerNavController.navigate("bookschedule")
                    }
                )
            }

            composable("bookschedule") {
                BookScheduleScreen(
                    bookingViewModel = bookingViewModel,
                    cartViewModel = cartViewModel,
                    onBack = {
                        innerNavController.popBackStack()
                    },
                    onNavigateToReview = {
                        innerNavController.navigate("orderreview")
                    },
                    onNavigateToCart = {
                        innerNavController.navigate("cart") {
                            val popped = innerNavController.popBackStack("listtest", false)
                            if (!popped) {
                                innerNavController.popBackStack("home", false)
                            }
                        }
                    }
                )
            }

            composable("cart") {
                CartScreen(
                    cartViewModel = cartViewModel,
                    userViewModel = userViewModel,
                    onBack = {
                        val popped = innerNavController.popBackStack("listtest", false)
                        if (!popped) {
                            val poppedHome = innerNavController.popBackStack("home", false)
                            if (!poppedHome) {
                                innerNavController.popBackStack()
                            }
                        }
                    },
                    onNavigateToProfile = {
                        innerNavController.navigate("user") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onNavigateToListTest = {
                        val popped = innerNavController.popBackStack("listtest", false)
                        if (!popped) {
                            innerNavController.navigate("listtest") {
                                popUpTo("cart") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    onNavigateToHome = {
                        innerNavController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onNavigateToOrderStatus = {
                        innerNavController.navigate("orderstatus") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                )
            }

            composable("orderreview") {
                OrderReviewScreen(
                    bookingViewModel = bookingViewModel,
                    userViewModel = userViewModel,
                    onBack = {
                        innerNavController.popBackStack()
                    },
                    onNavigateToProfile = {
                        innerNavController.navigate("user") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onNavigateToHome = {
                        innerNavController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onNavigateToOrderStatus = {
                        innerNavController.navigate("orderstatus") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                )
            }

            composable("orderstatus") {
                OrderStatusScreen(
                    userViewModel = userViewModel,
                    historyViewModel = historyViewModel,
                    onBack = {
                        innerNavController.popBackStack()
                    },
                    onNavigateToListTest = {
                        val popped = innerNavController.popBackStack("listtest", false)
                        if (!popped) {
                            innerNavController.navigate("listtest") {
                                popUpTo("orderstatus") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    }
}

