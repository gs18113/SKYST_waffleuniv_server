package com.example.skystWaffleunivServer.dto

import com.example.skystWaffleunivServer.domain.RoomEntity

class RoomDto() {
    companion object {
        fun fromEntity(entity: RoomEntity): RoomDto {
            return RoomDto(

            )
        }
    }
}
