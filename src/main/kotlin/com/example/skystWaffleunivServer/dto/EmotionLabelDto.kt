package com.example.skystWaffleunivServer.dto

class EmotionLabelDto() {
    companion object {
        fun fromEntity(entity: com.example.skystWaffleunivServer.domain.emotionlabel.EmotionLabelEntity): EmotionLabelDto {
            return EmotionLabelDto()
        }
    }
}
