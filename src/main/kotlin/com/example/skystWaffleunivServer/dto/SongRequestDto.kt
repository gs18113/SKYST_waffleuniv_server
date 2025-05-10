package com.example.skystWaffleunivServer.dto

import com.example.skystWaffleunivServer.domain.SongRequestEntity

class SongRequestDto(
    val id: Long? = null,
    var title: String,
    var artist: String,
    var sourceUrl: String,
) {
}
