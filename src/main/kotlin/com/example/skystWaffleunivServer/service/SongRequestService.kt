package com.example.skystWaffleunivServer.service

import com.example.skystWaffleunivServer.domain.SongRequestEntity
import com.example.skystWaffleunivServer.repository.RoomRepository
import com.example.skystWaffleunivServer.repository.SongRequestRepository
import com.example.skystWaffleunivServer.repository.UserRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

@Service
class SongRequestService(
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository,
    private val songRequestRepository: SongRequestRepository,
    private val messagingTemplate: SimpMessagingTemplate,
) {
    /**
     * 곡 신청 생성 및 Room.songCount 증가
     */
    @Transactional
    fun createRequest(
        userId: Long,
        roomId: Long,
        title: String,
        artist: String,
        sourceUrl: String,
    ): Long {
        val user =
            userRepository.findById(userId)
                .orElseThrow { IllegalArgumentException("User not found: $userId") }
        val room =
            roomRepository.findById(roomId)
                .orElseThrow { IllegalArgumentException("Room not found: $roomId") }

        // 1) DB INSERT
        val request =
            SongRequestEntity(
                room = room,
                user = user,
                title = title,
                artist = artist,
                sourceUrl = sourceUrl,
                requestedAt = LocalDateTime.now(),
                status = "PENDING",
                duration = 0L,
                // duration은 0으로 초기화 임시로
            )
        val saved = songRequestRepository.save(request)

        // 2) room.songCount 증가
        room.songCount += 1

        roomRepository.save(room)

        return saved.id!!
    }

    /**
     * 해당 방의 모든 신청곡 조회 (순서: 요청 시각 오름차순)
     */
    @Transactional(readOnly = true)
    fun getRequests(roomId: Long): List<com.example.skystWaffleunivServer.controller.SongRequestDto> {
        val room =
            roomRepository.findById(roomId)
                .orElseThrow { IllegalArgumentException("Room not found: $roomId") }

        val requests =
            songRequestRepository
                .findAllByRoomIdOrderByRequestedAtAsc(roomId)

        return requests.map { req ->
            com.example.skystWaffleunivServer.controller.SongRequestDto(
                requestId = req.id!!,
                userId = req.user.id!!,
                roomId = room.id!!,
                title = req.title,
                artist = req.artist,
                sourceUrl = req.sourceUrl,
                status = req.status,
                requestedAt = req.requestedAt,
                songCount = room.songCount,
            )
        }
    }
}

// Repository extension needed in SongRequestRepository:
// fun findAllByRoomIdOrderByRequestedAtAsc(roomId: Long): List<SongRequestEntity>
