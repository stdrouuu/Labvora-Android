package org.ukrida.labvora

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import org.ukrida.labvora.di.Injection
import org.ukrida.labvora.ui.screen.LoginScreen
import org.ukrida.labvora.ui.screen.MainScreen
import org.ukrida.labvora.ui.screen.RegisterScreen
import org.ukrida.labvora.ui.screen.WelcomeScreen
import org.ukrida.labvora.ui.theme.LabvoraTheme
import org.ukrida.labvora.viewmodel.UserViewModel
import org.ukrida.labvora.viewmodel.AdminViewModel
import org.ukrida.labvora.ui.screen.AdminHomeScreen
import org.ukrida.labvora.ui.screen.AdminOrderScreen
import org.ukrida.labvora.ui.screen.AdminInputScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LabvoraTheme {
                val navController = rememberNavController()
                val userViewModel = remember { UserViewModel(Injection.userRepo) }
                val adminViewModel = remember { AdminViewModel() }
                val context = LocalContext.current

                // State login — ponytail: saveable agar survive rotasi, bukan session server
                var isLoggedIn by rememberSaveable { mutableStateOf(false) }
                var role by rememberSaveable { mutableStateOf("") }

                val startDest = if (isLoggedIn) {
                    if (role == "admin") "admin-home" else "main"
                } else {
                    "welcome"
                }

                NavHost(
                    navController = navController,
                    startDestination = startDest
                ) {
                    // ================= WELCOME =================
                    composable("welcome") {
                        WelcomeScreen(
                            onNavigateLogin = {
                                navController.navigate("login")
                            }
                        )
                    }
                    // ================= LOGIN =================
                    composable("login") {
                        LoginScreen(
                            viewModel = userViewModel,
                            onLoginSuccess = { userRole ->
                                role = userRole
                                isLoggedIn = true
                                val dest = if (userRole == "admin") "admin-home" else "main"
                                navController.navigate(dest) {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            },
                            onNavigateRegister = {
                                navController.navigate("register")
                            }
                        )
                    }
                    // ================= REGISTER =================
                    composable("register") {
                        RegisterScreen(
                            viewModel = userViewModel,
                            onRegisterSuccess = {
                                val popped = navController.popBackStack("login", false)
                                if (!popped) {
                                    navController.navigate("login") {
                                        popUpTo("welcome") { inclusive = false }
                                    }
                                }
                            },
                            onNavigatePrivacyPolicy = {
                                // ponytail: external URL replaces in-app screen, no route needed
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://labvora.ifukrida.net/privacy-policy.html")))
                            }
                        )
                    }
                    // ================= MAIN =================
                    composable("main") {
                        MainScreen(
                            role = role,
                            navController = navController,
                            userViewModel = userViewModel,
                            onLogout = {
                                isLoggedIn = false
                                role = ""
                                userViewModel.currentUser.value = null
                                navController.navigate("welcome") {
                                    popUpTo("main") { inclusive = true }
                                }
                            },
                            onDeleteAccount = {
                                isLoggedIn = false
                                role = ""
                                navController.navigate("welcome") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }
                    // ================= ADMIN HOME =================
                    composable("admin-home") {
                        AdminHomeScreen(
                            viewModel = adminViewModel,
                            navController = navController,
                            onLogout = {
                                isLoggedIn = false
                                role = ""
                                userViewModel.currentUser.value = null
                                navController.navigate("welcome") {
                                    popUpTo("admin-home") { inclusive = true }
                                }
                            }
                        )
                    }
                    // ================= ADMIN ORDER =================
                    composable("admin-order") {
                        AdminOrderScreen(
                            viewModel = adminViewModel,
                            navController = navController,
                            onLogout = {
                                isLoggedIn = false
                                role = ""
                                userViewModel.currentUser.value = null
                                navController.navigate("welcome") {
                                    popUpTo("admin-order") { inclusive = true }
                                }
                            }
                        )
                    }
                    // ================= ADMIN INPUT =================
                    composable("admin-input") {
                        AdminInputScreen(
                            viewModel = adminViewModel,
                            navController = navController,
                            onLogout = {
                                isLoggedIn = false
                                role = ""
                                userViewModel.currentUser.value = null
                                navController.navigate("welcome") {
                                    popUpTo("admin-input") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
