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

    LaunchedEffect(userId) {
        if (userId > 0) {
            cartViewModel.initCartForUser(context, userId)
        }
    }
    LaunchedEffect(userId, bookingViewModel.isOrderCompleted) {
        if (userId > 0) {
            historyViewModel.getHistoryList(userId)
        }
    }
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val showTopBar = currentRoute == "home" || currentRoute == "listtest"

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
                BottomNav(innerNavController, role)
            }
        }

    ) { padding ->

        NavHost(
            navController = innerNavController,
            startDestination = "home",
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

            composable("home") {
                HomeScreen(
                    userViewModel = userViewModel,
                    bookingViewModel = bookingViewModel,
                    historyViewModel = historyViewModel,
                    onNavigateToListTest = {
                        innerNavController.navigate("listtest") {
                            popUpTo(innerNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
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
                        innerNavController.navigate("user")
                    },
                    onNavigateToOrderStatus = {
                        innerNavController.navigate("orderstatus")
                    }
                )
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

            composable("listtest") {
                ListTestScreen(
                    bookingViewModel = bookingViewModel,
                    onNavigateToDetail = { testId ->
                        innerNavController.navigate("detailtest/$testId")
                    }
                )
            }

            composable("user") {
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

