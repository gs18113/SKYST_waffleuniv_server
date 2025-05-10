package com.example.skystWaffleunivServer.controller

import com.example.skystWaffleunivServer.dto.UserDto
import com.example.skystWaffleunivServer.repository.UserRepository
import com.example.skystWaffleunivServer.service.UserService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val userRepository: UserRepository,
) {
    // 1) 최초 프로필 설정: colorHex만 입력, nickname 생성 후 userId 반환
    @PostMapping("/register")
    fun register(
        @RequestBody dto: UserCreateDto,
    ): ResponseEntity<UserRegisterResponseDto> {
        val user = userService.createUser(dto.colorHex)
        return ResponseEntity.ok(UserRegisterResponseDto(user.id!!))
    }

    // 2) 감정 기록 작성/수정 → 내부에서 AI 분석까지 수행
    @PutMapping("/record")
    fun updateRecordAndAnalyze(
        @RequestBody dto: UserRecordDto,
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<UserRecordResponseDto> {
        val analysis = userService.updateRecordAndAnalyze(userId, dto.content)
        // analysis.comment, analysis.labelId 포함
        return ResponseEntity.ok(
            UserRecordResponseDto(
                comment = analysis.comment,
                labelId = analysis.labelId,
            ),
        )
    }

    // 3) AI 피드백: 분석된 라벨이 맞는지 여부 전달
    @PostMapping("/feedback")
    fun feedback(
        @RequestBody dto: FeedbackDto,
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<Void> {
        userService.applyFeedback(userId, dto.isCorrect, dto.labelId)
        return ResponseEntity.noContent().build()
    }

    // 4) 방 입장 (라벨 기반 자동 배정)
    @PostMapping("/join-room")
    fun joinRoom(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<JoinRoomResponseDto> {
        val assignment = userService.joinRoom(userId)
        return ResponseEntity.ok(
            JoinRoomResponseDto(
                roomId = assignment.roomId,
                userCount = assignment.userCount,
                songCount = assignment.songCount,
                currentSongUrl = assignment.currentSongUrl,
                currentSongStartedAt = assignment.currentSongStartedAt,
            ),
        )
    }

    // 5) 방 퇴장
    @PostMapping("/leave-room")
    fun leaveRoom(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<Unit> {
        userService.leaveRoom(userId)
        return ResponseEntity.noContent().build()
    }

    // test, 현재 로그인된 사용자 정보 조회
    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserDto> {
        val userId = authentication.principal as Long
        val user =
            userRepository.findByIdOrNull(userId)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(UserDto.fromEntity(user))
    }
}

// 1) 사용자 생성 요청 DTO
data class UserCreateDto(
    val colorHex: String,
)

// 1) 사용자 생성 응답 DTO
data class UserRegisterResponseDto(
    val userId: Long,
)

// 2) 감정 기록 요청 DTO
data class UserRecordDto(
    val content: String,
)

// 2) 감정 기록 + AI 분석 응답 DTO
data class UserRecordResponseDto(
    val comment: String,
    // AI 공감 코멘트
    val labelId: Long,
    // AI 추천 감정 라벨 ID
)

// 3) AI 피드백 DTO
data class FeedbackDto(
    val isCorrect: Boolean,
    // AI 분석 결과가 맞는지 여부
    val labelId: Long? = null,
    // 피드백 시, 라벨 ID도 함께 전달
)

// 4) 방 입장 응답 DTO
data class JoinRoomResponseDto(
    val roomId: Long,
    val userCount: Int,
    val songCount: Int,
    val currentSongUrl: String?,
    val currentSongStartedAt: LocalDateTime?,
)
