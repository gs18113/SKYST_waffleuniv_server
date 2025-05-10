package com.example.skystWaffleunivServer.dto

class RoomDto() {
    companion object {
        fun fromEntity(entity: com.example.skystWaffleunivServer.domain.room.RoomEntity): RoomDto {
            return RoomDto()
        }
    }
}
