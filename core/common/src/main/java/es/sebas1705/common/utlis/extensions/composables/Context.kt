package es.sebas1705.common.utlis.extensions.composables

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import java.util.Locale

/**
 * Print a text generating a [Toast]
 *
 * @receiver [Context]: context of the app
 *
 * @param message [String]: the message to print
 *
 * @see Toast
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
fun Context.printTextInToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Returns a context whose resources use the app language ([Locale.getDefault], which the app sets when the
 * user picks a language) instead of the system language. The application context never follows that choice,
 * so anything that resolves strings outside an Activity (e.g. ViewModels) must go through this.
 *
 * @receiver [Context]: usually the application context
 */
fun Context.withAppLocale(): Context {
    val locale = Locale.getDefault()
    val current = resources.configuration
    if (current.locales[0] == locale) return this
    val localized = Configuration(current).apply { setLocale(locale) }
    return createConfigurationContext(localized)
}
