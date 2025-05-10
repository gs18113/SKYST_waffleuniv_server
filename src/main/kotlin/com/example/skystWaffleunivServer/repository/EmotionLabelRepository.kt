package com.example.skystWaffleunivServer.repository

import com.example.skystWaffleunivServer.domain.EmotionLabelEntity
import org.springframework.data.jpa.repository.JpaRepository

interface EmotionLabelRepository : JpaRepository<EmotionLabelEntity, Long> {
    fun findByName(name: String): EmotionLabelEntity?
}
