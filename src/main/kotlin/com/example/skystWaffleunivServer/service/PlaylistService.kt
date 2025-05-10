package com.example.skystWaffleunivServer.service

import com.example.skystWaffleunivServer.dto.RoomDto
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

@Service
@EnableScheduling
class RoomService(private val messagingTemplate: SimpMessagingTemplate) {

    private val scheduledTasks = ConcurrentHashMap<String, ScheduledFuture<*>>()
    private val taskScheduler = ThreadPoolTaskScheduler().apply { initialize() }

    fun createRoom(): RoomDto {
        return playlistId
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        playlists[playlistId]?.songs?.add(song)
    }

    fun startPlaylist(playlistId: String) {
        val playlist = playlists[playlistId] ?: return

        if (playlist.songs.isEmpty()) return

        playlist.isPlaying = true

        if (playlist.currentSongIndex == -1) {
            playlist.currentSongIndex = 0
        }

        scheduleNextSong(playlistId)
    }

    fun pausePlaylist(playlistId: String) {
        val playlist = playlists[playlistId] ?: return
        playlist.isPlaying = false

        // Cancel any scheduled tasks for this playlist
        scheduledTasks[playlistId]?.cancel(false)
        scheduledTasks.remove(playlistId)
    }

    private fun scheduleNextSong(playlistId: String) {
        val playlist = playlists[playlistId] ?: return

        if (!playlist.isPlaying) return

        val currentSong = playlist.songs[playlist.currentSongIndex]
        val nextSongIndex = (playlist.currentSongIndex + 1) % playlist.songs.size
        val nextSong = playlist.songs[nextSongIndex]

        // Broadcast current song to all subscribers
        messagingTemplate.convertAndSend(
            "/topic/playlist/$playlistId",
            mapOf(
                "action" to "PLAY",
                "currentSong" to currentSong,
                "nextSong" to nextSong
            )
        )

        // Schedule the next song
        val nextSongTime = LocalDateTime.now().plusSeconds(currentSong.duration)
        playlist.nextSongScheduledTime = nextSongTime

        // Cancel any existing scheduled tasks for this playlist
        scheduledTasks[playlistId]?.cancel(false)

        // Schedule the next song broadcast
        val scheduledTask = taskScheduler.schedule(
            { playNextSong(playlistId) },
            nextSongTime.toInstant(ZoneOffset.UTC)
        )

        scheduledTasks[playlistId] = scheduledTask
    }

    private fun playNextSong(playlistId: String) {
        val playlist = playlists[playlistId] ?: return

        if (!playlist.isPlaying) return

        playlist.currentSongIndex = (playlist.currentSongIndex + 1) % playlist.songs.size

        scheduleNextSong(playlistId)
    }

    fun getPlaylistStatus(playlistId: String): Map<String, Any>? {
        val playlist = playlists[playlistId] ?: return null

        if (playlist.currentSongIndex == -1 || playlist.songs.isEmpty()) {
            return mapOf(
                "playlistId" to playlistId,
                "status" to "NOT_PLAYING"
            )
        }

        val currentSong = playlist.songs[playlist.currentSongIndex]
        val nextSongIndex = (playlist.currentSongIndex + 1) % playlist.songs.size
        val nextSong = playlist.songs[nextSongIndex]

        return mapOf(
            "playlistId" to playlistId,
            "status" to if (playlist.isPlaying) "PLAYING" else "PAUSED",
            "currentSong" to currentSong,
            "nextSong" to nextSong,
            "nextSongAt" to playlist.nextSongScheduledTime
        )
    }
}

