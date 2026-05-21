package es.sebas1705.domain.managers

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import es.sebas1705.core.resources.Musics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager to handle MediaPlayer operations
 *
 * @property mediaPlayer The MediaPlayer instance used to play audio
 * @property context The application context used to access resources
 *
 * @since 0.1.0
 * @author Sebas1705 05/07/2025
 */
@Singleton
class MediaPlayerManager @Inject constructor(
    private val mediaPlayer: MediaPlayer,
    @param:ApplicationContext private val context: Context
){
    /** True once [changeSong] has been called at least once. */
    private var musicLoaded = false

    /** Whether music was playing just before the app went to the background. */
    private var wasPlayingBeforeBackground = false

    fun play() {
        if (musicLoaded && !mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    fun pause() {
        if (musicLoaded && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    fun stop() {
        if (musicLoaded && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.prepare() // Prepare the MediaPlayer for future use
        }
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        mediaPlayer.setVolume(leftVolume, rightVolume)
    }

    fun setVolume(volume: Float) {
        mediaPlayer.setVolume(volume, volume)
    }

    fun changeSong(music: Musics) {
        mediaPlayer.reset()
        context.resources.openRawResourceFd(music.resourceId).use { afd ->
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        }
        mediaPlayer.isLooping = true
        mediaPlayer.prepare()
        mediaPlayer.start()
        musicLoaded = true
    }

    /** Call from Activity.onStop() so music pauses when the app goes to the background. */
    fun onAppBackground() {
        wasPlayingBeforeBackground = musicLoaded && mediaPlayer.isPlaying
        pause()
    }

    /** Call from Activity.onStart() so music resumes when the app returns to the foreground. */
    fun onAppForeground() {
        if (wasPlayingBeforeBackground) {
            play()
        }
    }
}