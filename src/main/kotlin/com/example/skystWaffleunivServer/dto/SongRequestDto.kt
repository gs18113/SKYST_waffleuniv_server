package com.example.skystWaffleunivServer.dto

class SongRequestDto() {
    companion object {
        fun fromEntity(entity: com.example.skystWaffleunivServer.domain.songrequest.SongRequestEntity): SongRequestDto {
            return SongRequestDto()
        }
    }
}
