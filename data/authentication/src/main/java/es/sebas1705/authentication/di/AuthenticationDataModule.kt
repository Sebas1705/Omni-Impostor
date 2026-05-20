package es.sebas1705.authentication.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.sebas1705.common.BuildConfig
import javax.inject.Singleton

/**
 * Provides Firebase authentication dependencies for data authentication sources.
 *
 * Set [USE_AUTH_EMULATOR] to true while developing to avoid Firebase rate-limits.
 * Requires the emulator to be running: `firebase emulators:start --only auth`
 * AVD  → host 10.0.2.2  |  Physical device → your PC's local IP (e.g. 192.168.x.x)
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthenticationDataModule {

    /** Toggle to true locally when you need the emulator (no rate-limits). */
    private const val USE_AUTH_EMULATOR = false
    private const val EMULATOR_HOST_AVD = "10.0.2.2"
    private const val EMULATOR_HOST_DEVICE = "192.168.1.x" // replace with your PC's IP
    private const val EMULATOR_PORT = 9099

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance().also { auth ->
        if (BuildConfig.DEBUG && USE_AUTH_EMULATOR) {
            runCatching { auth.useEmulator(EMULATOR_HOST_AVD, EMULATOR_PORT) }
        }
    }
}

