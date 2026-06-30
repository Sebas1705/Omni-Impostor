package es.sebas1705.app.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module to provide firebase dependencies
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * Provides [FirebaseAnalytics] that is used to track events
     *
     * @param context [Context]: Application context to initialize Firebase Analytics
     *
     * @return [FirebaseAnalytics]
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}