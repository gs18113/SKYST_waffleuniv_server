package com.example.skystWaffleunivServer.service

import com.example.skystWaffleunivServer.domain.UserEntity
import com.example.skystWaffleunivServer.repository.EmotionLabelRepository
import com.example.skystWaffleunivServer.repository.RoomRepository
import com.example.skystWaffleunivServer.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
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
                songRequest = null,
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
                .orElseThrow { IllegalArgumentException("User not found: $userId") }

        user.recordContent = content
        val aiResult = aiService.analyzeEmotion(content)
        // aiResult -> labelId, lableName, comment
        val labelEntity =
            emotionLabelRepository.findById(aiResult.labelId)
                .orElseThrow { IllegalArgumentException("Label not found: ${aiResult.labelId}") }

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
        user.label = emotionLabelRepository.findByIdOrNull(1)
        val labelId = user.label!!.id!!

        val rooms = roomRepository.findByLabelId(labelId)
        if (rooms.isEmpty()) {
            roomService.createRoom(user)
        }

        // 가장 적은 인원 방 선택
        var target =
            rooms.minByOrNull { it.userCount }
                ?: throw IllegalStateException("No available rooms")

        if (target.userCount >= 10) {
            roomService.createRoom(user)
            target = rooms.minByOrNull { it.userCount }
                ?: throw IllegalStateException("No available rooms")
        }

        target.userCount += 1
        roomRepository.save(target)

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
    fun leaveRoom(userId: Long) {
        val user =
            userRepository.findById(userId)
                .orElseThrow { IllegalArgumentException("User not found: $userId") }

        val room = user.currentRoom ?: throw IllegalStateException("User is not in a room")

        room.userCount -= 1
        roomRepository.save(room)

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
