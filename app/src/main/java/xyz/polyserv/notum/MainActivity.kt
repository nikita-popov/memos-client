package xyz.polyserv.notum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import xyz.polyserv.notum.data.local.SharedPrefManager
import xyz.polyserv.notum.presentation.ui.screens.CreateMemoScreen
import xyz.polyserv.notum.presentation.ui.screens.MemoDetailScreen
import xyz.polyserv.notum.presentation.ui.screens.MemoListScreen
import xyz.polyserv.notum.presentation.ui.screens.SettingsScreen
import xyz.polyserv.notum.presentation.ui.theme.MemosTheme
import xyz.polyserv.notum.util.LocaleHelper

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    private val themeModeFlow = MutableStateFlow(xyz.polyserv.notum.data.model.ThemeMode.SYSTEM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Apply saved language
        val savedLanguage = sharedPrefManager.getAppLanguage()
        LocaleHelper.setLocale(this, savedLanguage)

        // Load saved theme
        themeModeFlow.value = sharedPrefManager.getThemeMode()

        setContent {
            val themeMode by themeModeFlow.collectAsState()

            MemosTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "memos_list"
                    ) {
                        composable("memos_list") {
                            MemoListScreen(
                                onMemoClick = { memo ->
                                    val encodedId = URLEncoder.encode(memo.id, StandardCharsets.UTF_8.toString())
                                    navController.navigate("memo_detail/$encodedId")
                                },
                                onCreateClick = {
                                    navController.navigate("create_memo")
                                },
                                onSettingsClick = { navController.navigate("settings") }
                            )
                        }

                        composable("create_memo") {
                            CreateMemoScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            "edit_memo/{memoId}",
                            arguments = listOf(
                                navArgument("memoId") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->
                            val memoId = backStackEntry.arguments?.getString("memoId") ?: return@composable
                            CreateMemoScreen(
                                onBackClick = { navController.popBackStack() },
                                memoId = memoId
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                onBackClick = {navController.popBackStack() },
                                onThemeChanged = { newTheme ->
                                    themeModeFlow.value = newTheme
                                },
                                onLanguageChanged = { newLanguage ->
                                    LocaleHelper.setLocale(this@MainActivity, newLanguage)
                                    recreate()
                                }
                            )
                        }

                        composable(
                            "memo_detail/{memoId}",
                            arguments = listOf(
                                navArgument("memoId") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->
                            val memoId = backStackEntry.arguments?.getString("memoId") ?: return@composable
                            MemoDetailScreen(
                                memoId = memoId,
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onEditClick = { memo ->
                                    navController.navigate("edit_memo/${memo.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
