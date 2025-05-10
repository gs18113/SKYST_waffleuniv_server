package com.example.skystWaffleunivServer.dto

import com.example.skystWaffleunivServer.domain.SongRequestEntity

class SongRequestDto(
    val id: Long? = null,
    var title: String,
    var artist: String,
    var sourceUrl: String,
) {
    companion object {
        fun fromEntity(entity: SongRequestEntity): SongRequestDto {
            return SongRequestDto(
                id = entity.id,
                title = entity.title,
                artist = entity.artist,
                sourceUrl = entity.sourceUrl,
            )
        }
    }
}
