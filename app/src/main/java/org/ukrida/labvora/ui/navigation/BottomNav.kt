package org.ukrida.labvora.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen(route = "home", title = "Beranda", icon = Icons.Default.Home)
    object ListTest : Screen(route = "listtest", title = "Tes Lab", icon = Icons.Default.Science)
    object User : Screen(route = "user", title = "Profil", icon = Icons.Default.Person)
}

@Composable
fun BottomNav(navController: NavHostController, role: String) {
    val items = listOf(Screen.Home, Screen.ListTest, Screen.User)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    // ponytail: border = divider 1.dp di atas bar (ganti shadow blur yg bikin blok abu2),
    // background putih cover inset nav agar tak ada celah transparan
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE5E7EB))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = when (screen) {
                    Screen.Home -> currentRoute == Screen.Home.route || currentRoute == "history" || currentRoute.startsWith("result") || currentRoute == "orderstatus"
                    Screen.ListTest -> currentRoute == Screen.ListTest.route || currentRoute.startsWith("detailtest") || currentRoute == "bookschedule" || currentRoute == "orderreview" || currentRoute == "cart"
                    Screen.User -> currentRoute == Screen.User.route || currentRoute == "profileedit"
                }
                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            val currentDest = navController.currentBackStackEntry?.destination?.route
                            if (screen == Screen.ListTest && currentDest == "cart") {
                                navController.popBackStack(Screen.ListTest.route, false)
                            } else if (screen == Screen.Home && currentDest != Screen.Home.route) {
                                val popped = navController.popBackStack(Screen.Home.route, false)
                                if (!popped) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            } else if (currentDest != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        // Floating Active State
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .offset(y = (-16).dp)
                                    .shadow(0.5.dp, shape = CircleShape)
                                    .background(Color.White, CircleShape)
                                    .padding(4.dp)
                                    .background(Color(0xFF3CB7A6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = screen.title,
                                color = Color(0xFF3CB7A6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.offset(y = (-8).dp)
                            )
                        }
                    } else {
                        // Inactive State
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = screen.title,
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
