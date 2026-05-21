package es.sebas1705.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import es.sebas1705.common.utlis.extensions.types.logI
import es.sebas1705.domain.managers.MediaPlayerManager
import es.sebas1705.main.AppNav
import java.util.Locale
import javax.inject.Inject


/**
 * Main activity of the app
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mediaPlayerManager: MediaPlayerManager

    /**
     * Create the activity and set the content to the SplashScreen
     * before the app is ready enable the edge to edge and set on the
     * decor view the listener to hide the system bars
     *
     * @param savedInstanceState [Bundle]: the saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySelectedLocaleToResources()

        enableEdgeToEdge()
        window.decorView.apply {
            setOnApplyWindowInsetsListener { _, insets ->
                hideBarsAfterDelay()
                insets
            }
            windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            windowInsetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            AppNav()
        }
    }

    /**
     * Handler to hide the system bars after a delay
     */
    private val hideHandler = Handler(Looper.getMainLooper())

    /**
     * Hide the system bars
     */
    private fun hideSystemBars() {
        window.decorView.windowInsetsController?.apply {
            logI("ui.systemBars hide status=true navigation=true behavior=transient_by_swipe")
            hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /**
     * Hide the system bars after a delay
     */
    private fun hideBarsAfterDelay() {
        hideHandler.postDelayed(
            { hideSystemBars() },
            2000
        )
    }

    override fun onStop() {
        super.onStop()
        mediaPlayerManager.onAppBackground()
    }

    override fun onStart() {
        super.onStart()
        mediaPlayerManager.onAppForeground()
    }

    private fun applySelectedLocaleToResources() {
        val locale = Locale.getDefault()

        val updatedConfiguration = resources.configuration
        updatedConfiguration.setLocale(locale)
        resources.updateConfiguration(updatedConfiguration, resources.displayMetrics)
    }

}