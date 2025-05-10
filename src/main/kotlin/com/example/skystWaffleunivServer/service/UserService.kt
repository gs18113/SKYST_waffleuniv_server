package com.example.skystWaffleunivServer.service

import com.example.skystWaffleunivServer.domain.RoomEntity
import com.example.skystWaffleunivServer.domain.UserEntity
import com.example.skystWaffleunivServer.exception.DomainException
import com.example.skystWaffleunivServer.repository.EmotionLabelRepository
import com.example.skystWaffleunivServer.repository.RoomRepository
import com.example.skystWaffleunivServer.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val emotionLabelRepository: EmotionLabelRepository,
    private val roomRepository: RoomRepository,
    private val roomService: RoomService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val aiService: AiService,
) {
    @Transactional
    fun createUser(colorHex: String): UserEntity {
        val count = userRepository.countByColorHex(colorHex)
        val nickname = "${colorHex}_${count + 1}"

        val user =
            UserEntity(
                nickname = nickname,
                colorHex = colorHex,
                label = null,
                recordContent = null,
                currentRoom = null,
            )

        return userRepository.save(user)
    }

    @Transactional
    fun updateRecordAndAnalyze(
        userId: Long,
        content: String,
    ): AnalysisResult {
        val user =
            userRepository.findById(userId)
                .orElseThrow { DomainException(400, HttpStatus.BAD_REQUEST, "User does not exist") }

        user.recordContent = content
        val aiResult = aiService.analyzeEmotion(content)
        // aiResult -> labelId, lableName, comment
        val labelEntity =
            emotionLabelRepository.findById(aiResult.labelId)
                .orElseThrow { DomainException(128, HttpStatus.INTERNAL_SERVER_ERROR, "Label not found: ${aiResult.labelId}") }

        user.label = labelEntity
        userRepository.save(user)

        return AnalysisResult(
            comment = aiResult.comment,
            labelId = labelEntity.id!!,
            labelName = labelEntity.name,
        )
    }

    @Transactional
    fun applyFeedback(
        userId: Long,
        isCorrect: Boolean,
        labelId: Long?,
    ) {
        // true면 AI 분석 결과가 맞음 -> 그냥 리턴
        // false면 AI 분석 결과가 틀림 -> 사용자에게 맞는 라벨로 변경
        if (isCorrect || labelId == null) return

        val user =
            userRepository.findById(userId)
                .orElseThrow { IllegalArgumentException("User not found: $userId") }

        val label =
            emotionLabelRepository.findById(labelId)
                .orElseThrow { IllegalArgumentException("Label not found: $labelId") }

        user.label = label
        userRepository.save(user)
    }

    @Transactional
    fun joinRoom(userId: Long): RoomAssignment {
        val user =
            userRepository.findById(userId)
                .orElseThrow { IllegalArgumentException("User not found: $userId") }
        val labelId = user.label!!.id!!

        var rooms = roomRepository.findByLabelId(labelId)
        if (rooms.isEmpty()) {
            val room =
                RoomEntity(
                    roomName = user.label!!.name,
                    label = user.label!!,
                )
            roomRepository.save(room)
            rooms = roomRepository.findByLabelId(labelId)
        }

        // 가장 적은 인원 방 선택
        var target =
            rooms.minByOrNull { it.userCount }
                ?: throw IllegalStateException("No available rooms")

        if (target.userCount >= 10) {
            val room =
                RoomEntity(
                    roomName = user.label!!.name,
                    label = user.label!!,
                )
            roomRepository.save(room)
            target = room
        }

        target.userCount += 1
        roomRepository.save(target)

        messagingTemplate.convertAndSend(
            "/topic/room/${target.id}",
            mapOf(
                "action" to "UPD_USER_COUNT",
                "content" to target.userCount,
            ),
        )

        user.currentRoom = target
        userRepository.save(user)

        return RoomAssignment(
            roomId = target.id!!,
            userCount = target.userCount,
            songCount = target.songCount,
            currentSongVideoId = target.currentSong?.videoId,
            currentSongStartedAt = target.currentSongStartedAt,
        )
    }

    @Transactional
    fun getRoomInfo(userId: Long): RoomAssignment {
        val user =
            userRepository.findById(userId)
                .orElseThrow { IllegalArgumentException("User not found: $userId") }

        val room = user.currentRoom ?: throw DomainException(400, HttpStatus.BAD_REQUEST, "User is not in a room")

        return RoomAssignment(
            roomId = room.id!!,
            userCount = room.userCount,
            songCount = room.songCount,
            currentSongVideoId = room.currentSong?.videoId,
            currentSongStartedAt = room.currentSongStartedAt,
        )
    }

    @Transactional
    fun leaveRoom(userId: Long) {
        val user =
            userRepository.findById(userId)
                .orElseThrow { IllegalArgumentException("User not found: $userId") }

        val room = user.currentRoom ?: throw DomainException(400, HttpStatus.BAD_REQUEST, "User is not in a room")

        room.userCount -= 1
        roomRepository.save(room)

        messagingTemplate.convertAndSend(
            "/topic/room/${room.id}",
            mapOf(
                "action" to "UPD_USER_COUNT",
                "content" to room.userCount,
            ),
        )

        user.currentRoom = null
        userRepository.save(user)
    }

    // 반환값 데이터 클래스
    data class AnalysisResult(
        val comment: String,
        val labelId: Long,
        val labelName: String,
    )

    data class RoomAssignment(
        val roomId: Long,
        val userCount: Int,
        val songCount: Int,
        val currentSongVideoId: String? = null,
        val currentSongStartedAt: LocalDateTime? = null,
    )
}
