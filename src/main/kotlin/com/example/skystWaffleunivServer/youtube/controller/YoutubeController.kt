package com.example.skystWaffleunivServer.youtube.controller

import com.example.skystWaffleunivServer.youtube.service.YoutubeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/youtube")
@Tag(name = "Youtube controller", description = "Simple youtube API for testing")
class YoutubeController(private val youtubeService: YoutubeService) {
    @GetMapping("/duration/{videoId}")
    @Operation(summary = "get video duration", description = "get video duration")
    fun getVideoDuration(
        @PathVariable videoId: String,
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val isoDuration = youtubeService.getVideoDuration(videoId)
            val seconds = youtubeService.convertDurationToSeconds(isoDuration)
            val formatted = youtubeService.formatDuration(isoDuration)

            val response =
                mapOf(
                    "videoId" to videoId,
                    "isoDuration" to isoDuration,
                    "seconds" to seconds,
                    "formatted" to formatted,
                )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                .body(mapOf("error" to (e.message ?: "Unknown error")))
        }
    }
}
