package com.westly.naijalaw.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.westly.naijalaw.R
import com.westly.naijalaw.core.data.LawCatalog
import com.westly.naijalaw.core.ui.AppHeader
import com.westly.naijalaw.feature.home.HomeScreen

/** The exact route strings of the app. */
object Routes {
    // The 3 bottom-menu tabs (registered in this phase)
    const val HOME = "home"
    const val FAVOURITES = "favourites"
    const val ABOUT = "about"

    // Later phases register screens for these (not registered yet)
    const val SECTIONS = "sections/{lawId}"
    const val SECTION = "section/{lawId}/{sectionId}"
    const val SETTINGS = "settings"

    /** The real route for one law, for example sections/criminal-code-act. */
    fun sections(lawId: String): String = "sections/$lawId"

    /**
     * Routes that show the bottom menu. A route that is not in this set has no bottom bar,
     * so later phases only need to leave their screen out of this set.
     */
    val bottomBarRoutes: Set<String> = setOf(HOME, FAVOURITES, ABOUT)
}

private class TabItem(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

private val Tabs = listOf(
    TabItem(Routes.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Filled.Home),
    TabItem(Routes.FAVOURITES, R.string.nav_favourites, Icons.Outlined.Star, Icons.Filled.Star),
    TabItem(Routes.ABOUT, R.string.nav_about, Icons.Outlined.Info, Icons.Filled.Info),
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // Before the first screen is ready (route is null) we already show the bar, so it does not jump in.
    val showBottomBar = currentRoute == null || currentRoute in Routes.bottomBarRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Screens handle the status bar themselves (later phases draw a green header behind it).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NaijaBottomBar(navController = navController)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(onLawClick = { lawId -> navController.navigate(Routes.sections(lawId)) })
            }
            composable(Routes.FAVOURITES) { TabPlaceholder(stringResource(R.string.nav_favourites)) }
            composable(Routes.ABOUT) { TabPlaceholder(stringResource(R.string.nav_about)) }

            // Phase 6 replaces this placeholder with the real Sections list.
            // It has no bottom bar (the route is not in Routes.bottomBarRoutes) and shows a back arrow.
            composable(
                route = Routes.SECTIONS,
                arguments = listOf(navArgument("lawId") { type = NavType.StringType }),
            ) { entry ->
                val lawId = entry.arguments?.getString("lawId").orEmpty()
                SectionsPlaceholder(lawId = lawId, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun NaijaBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val accent = MaterialTheme.colorScheme.primary              // green (light) / gold (dark)
    val grey = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.outline

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp,
        modifier = Modifier.drawWithContent {
            drawContent()
            // thin line on top of the bar
            drawRect(
                color = lineColor,
                topLeft = Offset.Zero,
                size = Size(size.width, 1.dp.toPx()),
            )
        },
    ) {
        for (tab in Tabs) {
            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        // Back from Favourites or About goes to Home; each tab keeps its state.
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    // The label already says what the tab is, so the icon is decorative.
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.label),
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = accent,
                    selectedTextColor = accent,
                    indicatorColor = Color.Transparent,   // no filled pill
                    unselectedIconColor = grey,
                    unselectedTextColor = grey,
                ),
                modifier = Modifier.drawBehind {
                    if (selected) {
                        // thin underline under the selected tab
                        val thickness = 3.dp.toPx()
                        drawRect(
                            color = accent,
                            topLeft = Offset(0f, size.height - thickness),
                            size = Size(size.width, thickness),
                        )
                    }
                },
            )
        }
    }
}

/** Placeholder until Phase 8 builds the real Favourites and About screens. */
@Composable
private fun TabPlaceholder(text: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        // The header keeps the status bar icons readable (they are white on the green header).
        AppHeader(title = text)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

/** Phase 6 replaces this with the real Sections list. */
@Composable
private fun SectionsPlaceholder(lawId: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(title = LawCatalog.byId(lawId)?.name ?: lawId, onBack = onBack)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = lawId,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
