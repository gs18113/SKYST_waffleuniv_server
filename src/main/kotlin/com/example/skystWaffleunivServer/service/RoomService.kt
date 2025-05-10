package com.example.skystWaffleunivServer.service

import com.example.skystWaffleunivServer.domain.SongRequestEntity
import com.example.skystWaffleunivServer.dto.ReactionDto
import com.example.skystWaffleunivServer.dto.RoomDto
import com.example.skystWaffleunivServer.dto.SongPlayDto
import com.example.skystWaffleunivServer.dto.SongRequestDto
import com.example.skystWaffleunivServer.exception.DomainException
import com.example.skystWaffleunivServer.repository.EmotionLabelRepository
import com.example.skystWaffleunivServer.repository.RoomRepository
import com.example.skystWaffleunivServer.repository.SongRequestRepository
import com.example.skystWaffleunivServer.repository.UserRepository
import com.example.skystWaffleunivServer.youtube.service.YoutubeService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.time.LocalDateTime
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Service
@EnableScheduling
class RoomService(
    private val userRepository: UserRepository,
    private val emotionLabelRepository: EmotionLabelRepository,
    private val messagingTemplate: SimpMessagingTemplate,
    private val roomRepository: RoomRepository,
    private val songRequestRepository: SongRequestRepository,
    private val youtubeService: YoutubeService,
    private val applicationContext: ApplicationContext,
) {
    // kotlin logger
    private val logger = KotlinLogging.logger {}

    // Then in your service:
    @Autowired
    private lateinit var songPlayerExecutor: ScheduledExecutorService

    @Transactional
    fun findAllRooms(): List<RoomDto> {
        return roomRepository.findAll().map { RoomDto.fromEntity(it) }
    }

    @Transactional
    fun findRoomById(roomId: Long): RoomDto {
        return roomRepository.findByIdOrNull(roomId)?.let { RoomDto.fromEntity(it) }
            ?: throw DomainException(400, HttpStatus.BAD_REQUEST, "Room not found")
    }

    fun addReaction(
        roomId: Long,
        reactionDto: ReactionDto,
    ) {
        roomRepository.findByIdOrNull(roomId) ?: throw DomainException(400, HttpStatus.BAD_REQUEST, "Room not found")
        messagingTemplate.convertAndSend(
            "/topic/room/$roomId",
            mapOf(
                "action" to "REACTION",
                "content" to reactionDto.name,
            ),
        )
    }

    @Transactional
    fun addSongToRoom(
        userId: Long,
        roomId: Long,
        song: SongRequestDto,
    ) {
        if (songRequestRepository.existsByUserIdAndStatus(userId, "REQUESTED")) {
            throw Exception("You already have a song in the queue")
        }
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
                        comment = song.comment,
                        fullStory = user.recordContent ?: "",
                    )
                songRequestRepository.save(songRequestEntity)
                room.songCount++
                if (room.songCount == 1) {
                    room.currentSong = songRequestEntity
                    roomRepository.save(room)
                    startPlaylist(roomId)
                }
                messagingTemplate.convertAndSend(
                    "/topic/room/$roomId",
                    mapOf(
                        "action" to "UPD_SONG_COUNT",
                        "content" to room.songCount,
                    ),
                )
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
                "content" to SongPlayDto.fromEntity(currentSong),
            ),
        )
        room.currentSongStartedAt = LocalDateTime.now()
        currentSong.status = "PLAYING"
        songRequestRepository.save(currentSong)
        roomRepository.save(room)

        val pending =
            songRequestRepository
                .findAllByRoomIdAndStatusOrderByRequestedAtAsc(roomId, "REQUESTED")
                .map { req ->
                    mapOf(
                        "user_color" to req.user.colorHex,
                    )
                }
        if (pending.isNotEmpty()) {
            messagingTemplate.convertAndSend(
                "/topic/room/$roomId",
                mapOf(
                    "action" to "UPD_PENDING_LIST",
                    "content" to pending,
                ),
            )
        }

        // Schedule the next song
        logger.info { room.currentSong!!.duration }
        val nextSongTime = LocalDateTime.now().plusSeconds(room.currentSong!!.duration + 5)

        // Use a Runnable instead of trying to get the bean
        logger.info { "Scheduling next song for room $roomId at $nextSongTime (current time: ${LocalDateTime.now()})" }

        val delayInSeconds = room.currentSong!!.duration + 5
        songPlayerExecutor.schedule(
            {
                try {
                    logger.info { "Executing scheduled song change for room $roomId" }
                    val transactionManager = applicationContext.getBean(PlatformTransactionManager::class.java)
                    val status = transactionManager.getTransaction(DefaultTransactionDefinition())
                    try {
                        playNextSong(roomId)
                        transactionManager.commit(status)
                    } catch (e: Exception) {
                        transactionManager.rollback(status)
                        logger.error(e) { "Error playing next song for room $roomId" }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Uncaught exception in song player executor" }
                }
            },
            delayInSeconds,
            TimeUnit.SECONDS,
        )
    }

    fun playNextSong(roomId: Long) {
        logger.info { "playNextSong called" }
        val room = roomRepository.findByIdOrNull(roomId) ?: throw DomainException(400, HttpStatus.BAD_REQUEST, "Room not found")
        val currentSong = room.currentSong ?: throw DomainException(400, HttpStatus.BAD_REQUEST, "No current song to play")

        // remove current song from songRequestRepository and
        // set room.currentSong to the next song, which is the SongRequestEntity associated with this room and the oldest requestedAt.
        room.currentSong = songRequestRepository.findFirstByRoomIdAndStatusOrderByRequestedAtAsc(roomId, "REQUESTED")
        room.songCount--
        roomRepository.save(room)
        songRequestRepository.delete(currentSong)

        messagingTemplate.convertAndSend(
            "/topic/room/$roomId",
            mapOf(
                "action" to "UPD_SONG_COUNT",
                "content" to room.songCount,
            ),
        )

        if (room.currentSong != null) playAndScheduleNextSong(roomId)
    }
}
