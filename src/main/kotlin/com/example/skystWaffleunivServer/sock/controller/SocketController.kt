package com.example.skystWaffleunivServer.sock.controller

import com.example.skystWaffleunivServer.dto.ReactionDto
import com.example.skystWaffleunivServer.dto.SongRequestDto
import com.example.skystWaffleunivServer.jwt.service.TokenService
import com.example.skystWaffleunivServer.repository.UserRepository
import com.example.skystWaffleunivServer.service.RoomService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class SocketController(
    private val messagingTemplate: SimpMessagingTemplate,
    private val roomService: RoomService,
    private val tokenService: TokenService,
    private val userRepository: UserRepository,
) {
    @MessageMapping("/room/{roomId}/songrequest")
    fun handleSongRequest(
        @DestinationVariable roomId: Long,
        @Payload songRequestDto: SongRequestDto,
        @Header("Authorization") accessToken: String,
    ) {
        // Handle the song request
        // For example, you can send the notification to a specific topic
        val userId = tokenService.getUserIdFromToken(accessToken.drop(7))
        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw Exception("User not found")
        if (user.currentRoom?.id != roomId) {
            throw Exception("User is not in the room")
        }
        roomService.addSongToRoom(userId, roomId, songRequestDto)
    }

    @MessageMapping("/room/{roomId}/reaction")
    fun handleReaction(
        @DestinationVariable roomId: Long,
        @Payload reactionDto: ReactionDto,
        @Header("Authorization") accessToken: String,
    ) {
        // Handle the song request
        // For example, you can send the notification to a specific topic
        val userId = tokenService.getUserIdFromToken(accessToken.drop(7))
        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw Exception("User not found")
        if (user.currentRoom?.id != roomId) {
            throw Exception("User is not in the room")
        }
        roomService.addReaction(roomId, reactionDto)
    }
}
