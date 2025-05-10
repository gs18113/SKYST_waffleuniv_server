package com.example.skystWaffleunivServer.service

import com.example.skystWaffleunivServer.domain.SongRequestEntity
import com.example.skystWaffleunivServer.dto.RoomDto
import com.example.skystWaffleunivServer.dto.SongPlayDto
import com.example.skystWaffleunivServer.dto.SongRequestDto
import com.example.skystWaffleunivServer.repository.EmotionLabelRepository
import com.example.skystWaffleunivServer.repository.RoomRepository
import com.example.skystWaffleunivServer.repository.SongRequestRepository
import com.example.skystWaffleunivServer.repository.UserRepository
import com.example.skystWaffleunivServer.youtube.service.YoutubeService
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

@Service
@EnableScheduling
class RoomService(
    private val userRepository: UserRepository,
    private val emotionLabelRepository: EmotionLabelRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val roomRepository: RoomRepository,
    private val songRequestRepository: SongRequestRepository,
    private val youtubeService: YoutubeService,
) {
    private val scheduledTasks = ConcurrentHashMap<Long, ScheduledFuture<*>>()
    private val taskScheduler = ThreadPoolTaskScheduler().apply { initialize() }

    @Transactional
    fun findAllRooms(): List<RoomDto> {
        return roomRepository.findAll().map { RoomDto.fromEntity(it) }
    }

    @Transactional
    fun findRoomById(roomId: Long): RoomDto {
        return roomRepository.findByIdOrNull(roomId)?.let { RoomDto.fromEntity(it) }
            ?: throw Exception("Room not found")
    }

    @Transactional
    fun addSongToRoom(
        userId: Long,
        roomId: Long,
        song: SongRequestDto,
    ) {
        youtubeService.getVideIdFromUrl(song.sourceUrl)?.let { videoId ->
            youtubeService.getVideoDuration(videoId).let {
                    duration ->
                val room = roomRepository.findByIdOrNull(roomId) ?: throw Exception("Room not found")
                val user = userRepository.findByIdOrNull(userId) ?: throw Exception("User not found")
                val songRequestEntity =
                    SongRequestEntity(
                        videoId = videoId,
                        title = song.title,
                        requestedAt = LocalDateTime.now(),
                        status = "REQUESTED",
                        room = room,
                        user = user,
                        duration = duration,
                        artist = song.artist,
                    )
                songRequestRepository.save(songRequestEntity)
                room.songCount++
                room.currentSong = songRequestEntity
                roomRepository.save(room)
                if (room.songCount == 1) {
                    startPlaylist(roomId)
                }
            }
        } ?: throw Exception("Invalid YouTube URL")
    }

    fun startPlaylist(roomId: Long) {
        playAndScheduleNextSong(roomId)
    }

    private fun playAndScheduleNextSong(roomId: Long) {
        val room = roomRepository.findByIdOrNull(roomId) ?: throw Exception("Room not found")
        val currentSong = room.currentSong ?: throw Exception("No current song to play")

        // Broadcast current song to all subscribers
        messagingTemplate.convertAndSend(
            "/topic/room/$roomId",
            mapOf(
                "action" to "PLAY",
                "song" to SongPlayDto.fromEntity(currentSong),
            ),
        )
        room.currentSongStartedAt = LocalDateTime.now()
        currentSong.status = "PLAYING"
        songRequestRepository.save(currentSong)

        // Schedule the next song
        val nextSongTime = LocalDateTime.now().plusSeconds(room.currentSong!!.duration + 5)

        // Cancel any existing scheduled tasks for this playlist
        scheduledTasks[roomId]?.cancel(false)

        // Schedule the next song broadcast
        val scheduledTask =
            taskScheduler.schedule(
                { playNextSong(roomId) },
                nextSongTime.toInstant(ZoneOffset.UTC),
            )

        scheduledTasks[roomId] = scheduledTask
    }

    private fun playNextSong(roomId: Long) {
        val room = roomRepository.findByIdOrNull(roomId) ?: throw Exception("Room not found")
        val currentSong = room.currentSong ?: throw Exception("No current song to play")
        // remove current song from songRequestRepository and
        // set room.currentSong to the next song, which is the SongRequestEntity associated with this room and the oldest requestedAt.
        songRequestRepository.delete(currentSong)
        room.currentSong = songRequestRepository.findFirstByRoomIdAndStatusOrderByRequestedAtAsc(roomId, "REQUESTED")
            ?: throw Exception("No next song to play")
        room.songCount--

        roomRepository.save(room)
        playAndScheduleNextSong(roomId)
    }

    fun joinRoom(
        userId: Long,
        roomId: Long,
    ) {
        val room = roomRepository.findByIdOrNull(roomId) ?: throw Exception("Room not found")
        val user = userRepository.findByIdOrNull(userId) ?: throw Exception("User not found")
        room.userCount++
        user.currentRoom = room
        userRepository.save(user)
        roomRepository.save(room)
    }

    fun leaveRoom(
        userId: Long,
        roomId: Long,
    ) {
        val room = roomRepository.findByIdOrNull(roomId) ?: throw Exception("Room not found")
        val user = userRepository.findByIdOrNull(userId) ?: throw Exception("User not found")
        room.userCount--
        user.currentRoom = null
        userRepository.save(user)
        roomRepository.save(room)
    }
}
