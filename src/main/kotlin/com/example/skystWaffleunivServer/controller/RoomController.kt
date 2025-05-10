package com.example.skystWaffleunivServer.controller

import com.example.skystWaffleunivServer.dto.RoomDto
import com.example.skystWaffleunivServer.service.EmotionLabelService
import com.example.skystWaffleunivServer.service.ReactionService
import com.example.skystWaffleunivServer.service.RoomService
import com.example.skystWaffleunivServer.service.SongRequestService
import com.example.skystWaffleunivServer.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/rooms")
class RoomController(
    private val userService: UserService,
    private val roomService: RoomService,
    private val songRequestService: SongRequestService,
    private val reactionService: ReactionService,
    private val emotionLabelService: EmotionLabelService
) {
    /**
     * 전체 방 목록 조회
     */
    @GetMapping
    fun getAllRooms(): ResponseEntity<List<RoomDto>> {
        val rooms = roomService.findAllRooms()
        return ResponseEntity.ok(rooms)
    }

    /**
     * 단일 방 정보 조회
     */
    @GetMapping("/{roomId}")
    fun getRoom(
        @PathVariable roomId: Long,
    ): ResponseEntity<RoomDto> {
        val room = roomService.findRoomById(roomId)
        return ResponseEntity.ok(room)
    }

    /**
     * 방 입장 (WebSocket 연결 전, HTTP로 선행할 수도 있고
     * Principal 을 통해 현재 로그인된 사용자 기준으로 입장 처리)
     */
    @PostMapping("/{roomId}/join")
    fun joinRoom(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<Unit> {
        roomService.joinRoom(userId, roomId)
        return ResponseEntity.ok().build()
    }

    /**
     * 방 퇴장
     */
    @PostMapping("/{roomId}/leave")
    fun leaveRoom(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<Unit> {
        roomService.leaveRoom(roomId, userId)
        return ResponseEntity.ok().build()
    }
}
