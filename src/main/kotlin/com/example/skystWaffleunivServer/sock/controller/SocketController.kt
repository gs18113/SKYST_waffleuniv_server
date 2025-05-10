package com.example.skystWaffleunivServer.sock.controller

import com.example.skystWaffleunivServer.dto.SocketNotification
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class SocketController(private val messagingTemplate: SimpMessagingTemplate) {
    @MessageMapping("/notification")
    fun processNotification(notification: SocketNotification, principal: Principal?) {
        // principal contains the authenticated user information
        val authenticatedUserId = principal?.name

        // You can verify the user has permission to send this notification
        if (authenticatedUserId != null) {
            // Process notification logic here

            // Send to specific user
            messagingTemplate.convertAndSendToUser(
                notification.userId.toString(),
                "/queue/notifications",
                notification,
            )
        }
    }

    // Method to broadcast to all users
    fun broadcastNotification(notification: SocketNotification) {
        messagingTemplate.convertAndSend("/topic/notifications", notification)
    }

    // Method to send notification to a specific user programmatically
    fun sendNotificationToUser(
        userId: Long,
        message: String,
    ) {
        val notification = SocketNotification(userId, message)
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            notification,
        )
    }
}
