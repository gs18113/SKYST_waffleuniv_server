package com.example.skystWaffleunivServer.service

import com.example.skystWaffleunivServer.controller.RoomCreateDto
import com.example.skystWaffleunivServer.domain.RoomEntity
import com.example.skystWaffleunivServer.dto.RoomDto
import com.example.skystWaffleunivServer.dto.SongRequestDto
import com.example.skystWaffleunivServer.repository.EmotionLabelRepository
import com.example.skystWaffleunivServer.repository.RoomRepository
import com.example.skystWaffleunivServer.repository.SongRequestRepository
import com.example.skystWaffleunivServer.repository.UserRepository
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
) {
    private val scheduledTasks = ConcurrentHashMap<Long, ScheduledFuture<*>>()
    private val taskScheduler = ThreadPoolTaskScheduler().apply { initialize() }

    fun findAllRooms(): List<RoomDto> {
        return roomRepository.findAll().map { RoomDto.fromEntity(it) }
    }

    fun findRoomById(roomId: Long): RoomDto {
        return roomRepository.findByIdOrNull(roomId)?.let { RoomDto.fromEntity(it) }
            ?: throw Exception("Room not found")
    }

    fun createRoom(
        userId: Long,
        dto: RoomCreateDto,
    ): RoomEntity {
        val label =
            emotionLabelRepository.findByName(dto.emotionLabel)
                ?: throw Exception("Emotion label not found")

        val room =
            RoomEntity(
                roomName = dto.roomName,
                label = label,
            )

        return room
    }

    fun addSongToRoom(
        userId: Long,
        roomId: Long,
        song: SongRequestDto,
    ) {
        // TODO
        // if first song, start the playlist
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
                "song" to SongRequestDto.fromEntity(currentSong),
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
