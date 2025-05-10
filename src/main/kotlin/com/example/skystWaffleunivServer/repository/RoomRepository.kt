package com.example.skystWaffleunivServer.repository

import com.example.skystWaffleunivServer.domain.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<RoomEntity, Long> {
    fun findByLabelId(labelId: Long): List<RoomEntity>
}
