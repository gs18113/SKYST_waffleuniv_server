package com.example.skystWaffleunivServer.dto

import com.example.skystWaffleunivServer.domain.EmotionLabelEntity

class EmotionLabelDto(
    val id: Long,
    val name: String,
) {
    companion object {
        fun fromEntity(entity: EmotionLabelEntity): EmotionLabelDto {
            return EmotionLabelDto(
                id = entity.id!!,
                name = entity.name,
            )
        }
    }
}
