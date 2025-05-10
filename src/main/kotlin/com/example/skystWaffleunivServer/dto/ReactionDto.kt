package com.example.skystWaffleunivServer.dto

import com.example.skystWaffleunivServer.domain.ReactionEntity

class ReactionDto() {
    companion object {
        fun fromEntity(entity: ReactionEntity): ReactionDto {
            return ReactionDto()
        }
    }
}
