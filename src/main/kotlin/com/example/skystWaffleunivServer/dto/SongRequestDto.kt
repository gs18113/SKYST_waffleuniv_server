package com.example.skystWaffleunivServer.dto

import com.example.skystWaffleunivServer.domain.SongRequestEntity

class SongRequestDto() {
    companion object {
        fun fromEntity(entity: SongRequestEntity): SongRequestDto {
            return SongRequestDto()
        }
    }
}
