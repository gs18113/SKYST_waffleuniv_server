package com.example.skystWaffleunivServer.service

import com.example.skystWaffleunivServer.controller.EmotionLabelCreateDto
import com.example.skystWaffleunivServer.controller.EmotionLabelUpdateDto
import com.example.skystWaffleunivServer.domain.EmotionLabelEntity
import com.example.skystWaffleunivServer.dto.EmotionLabelDto
import com.example.skystWaffleunivServer.repository.EmotionLabelRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmotionLabelService(
    private val emotionLabelRepository: EmotionLabelRepository,
) {
    // Fetch all emotion labels
    @Transactional(readOnly = true)
    fun getAllEmotionLabels(): List<EmotionLabelDto> {
        return emotionLabelRepository.findAll().map { label ->
            EmotionLabelDto(label.id!!, label.name)
        }
    }

    // Create a new emotion label
    @Transactional
    fun createEmotionLabel(dto: EmotionLabelCreateDto): EmotionLabelDto {
        val newLabel = EmotionLabelEntity(name = dto.name)
        val savedLabel = emotionLabelRepository.save(newLabel)
        return EmotionLabelDto(savedLabel.id!!, savedLabel.name)
    }

    // Update an existing emotion label
    @Transactional
    fun updateEmotionLabel(
        id: Long,
        dto: EmotionLabelUpdateDto,
    ): EmotionLabelDto {
        val label =
            emotionLabelRepository.findById(id).orElseThrow {
                IllegalArgumentException("Emotion label with ID $id not found")
            }
        label.name = dto.name
        val updatedLabel = emotionLabelRepository.save(label)
        return EmotionLabelDto(updatedLabel.id!!, updatedLabel.name)
    }

    // Delete an emotion label
    @Transactional
    fun deleteEmotionLabel(id: Long) {
        if (!emotionLabelRepository.existsById(id)) {
            throw IllegalArgumentException("Emotion label with ID $id not found")
        }
        emotionLabelRepository.deleteById(id)
    }
}
