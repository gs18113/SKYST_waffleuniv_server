package com.example.skystWaffleunivServer.controller

import com.example.skystWaffleunivServer.dto.EmotionLabelDto
import com.example.skystWaffleunivServer.service.EmotionLabelService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/emotion-labels")
class EmotionLabelController(
    private val emotionLabelService: EmotionLabelService,
) {
    // 1) 감정 레이블 목록 조회
    @GetMapping
    fun getEmotionLabels(): ResponseEntity<List<EmotionLabelDto>> {
        val labels = emotionLabelService.getAllEmotionLabels()
        return ResponseEntity.ok(labels)
    }

    // 2) 감정 레이블 생성
    @PostMapping
    fun createEmotionLabel(
        @RequestBody dto: EmotionLabelCreateDto,
    ): ResponseEntity<EmotionLabelDto> {
        val createdLabel = emotionLabelService.createEmotionLabel(dto)
        return ResponseEntity.ok(createdLabel)
    }

    // 3) 감정 레이블 수정
    @PutMapping("/{id}")
    fun updateEmotionLabel(
        @PathVariable id: Long,
        @RequestBody dto: EmotionLabelUpdateDto,
    ): ResponseEntity<EmotionLabelDto> {
        val updatedLabel = emotionLabelService.updateEmotionLabel(id, dto)
        return ResponseEntity.ok(updatedLabel)
    }

    // 4) 감정 레이블 삭제
    @DeleteMapping("/{id}")
    fun deleteEmotionLabel(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        emotionLabelService.deleteEmotionLabel(id)
        return ResponseEntity.noContent().build()
    }
}

data class EmotionLabelCreateDto(
    val name: String,
) {
    // Add a no-args constructor for Jackson
    constructor() : this("")
}

data class EmotionLabelUpdateDto(
    val name: String,
) {
    // Add a no-args constructor for Jackson
    constructor() : this("")
}
