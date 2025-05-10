package com.example.skystWaffleunivServer.sock.controller

import com.example.skystWaffleunivServer.dto.SongRequestDto
import com.example.skystWaffleunivServer.service.RoomService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import java.security.Principal

@Controller
class SocketController(
    private val messagingTemplate: SimpMessagingTemplate,
    private val roomService: RoomService,
) {
    @MessageMapping("/room/{roomId}/songrequest")
    fun handleSongRequest(
        @PathVariable roomId: Long,
        songRequestDto: SongRequestDto,
        principal: Principal,
    ) {
        // Handle the song request
        // For example, you can send the notification to a specific topic
        roomService.addSongToRoom(principal.name.toLong(), roomId, songRequestDto)
    }
}
