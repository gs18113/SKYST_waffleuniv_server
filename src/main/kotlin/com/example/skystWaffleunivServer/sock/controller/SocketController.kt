package com.example.skystWaffleunivServer.sock.controller

import com.example.skystWaffleunivServer.dto.SongRequestDto
import com.example.skystWaffleunivServer.service.RoomService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable

@Controller
class SocketController(
    private val messagingTemplate: SimpMessagingTemplate,
    private val roomService: RoomService,
) {
    @MessageMapping("/room/{roomId}/songrequest")
    fun handleSongRequest(
        @PathVariable roomId: Long,
        songRequestDto: SongRequestDto,
        @AuthenticationPrincipal userId: Long,
    ) {
        // Handle the song request
        // For example, you can send the notification to a specific topic
        roomService.addSongToRoom(userId, roomId, songRequestDto)
    }
}
