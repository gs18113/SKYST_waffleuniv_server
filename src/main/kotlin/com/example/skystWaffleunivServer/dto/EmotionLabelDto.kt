package com.example.skystWaffleunivServer.dto

import com.example.skystWaffleunivServer.domain.EmotionLabelEntity

class EmotionLabelDto() {
    companion object {
        fun fromEntity(entity: EmotionLabelEntity): EmotionLabelDto {
            return EmotionLabelDto()
        }
    }
}
