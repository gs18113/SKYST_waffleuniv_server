package com.example.skystWaffleunivServer.repository

import com.example.skystWaffleunivServer.domain.SongRequestEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SongRequestRepository : JpaRepository<SongRequestEntity, Long> {
    fun findFirstByRoomIdAndStatusOrderByRequestedAtAsc(
        roomId: Long,
        status: String,
    ): SongRequestEntity?

    fun findAllByRoomIdOrderByRequestedAtAsc(roomId: Long): List<SongRequestEntity>

    fun existsByUserIdAndStatus(
        userId: Long,
        status: String,
    ): Boolean
}
