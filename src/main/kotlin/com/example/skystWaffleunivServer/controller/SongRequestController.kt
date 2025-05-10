package com.example.skystWaffleunivServer.controller
import com.example.skystWaffleunivServer.service.SongRequestService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/rooms/{roomId}/requests")
class SongRequestController(
    private val songRequestService: SongRequestService,
) {
    @PostMapping
    fun createRequest(
        @PathVariable roomId: Long,
        @RequestBody dto: SongRequestCreateDto,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<SongRequestCreateResponseDto> {
        // 1) 서비스 호출로 DB INSERT 및 song_count 증가
        val requestId = songRequestService.createRequest(
            userId, roomId,
            dto.title, dto.artist, dto.sourceUrl
        )

        return ResponseEntity.ok(
            SongRequestCreateResponseDto(requestId)
        )
    }

    @GetMapping()
    fun getRequest(
        @PathVariable roomId: Long,
    ): ResponseEntity<List<SongRequestDto>> {
        val dto = songRequestService.getRequests(roomId)
        return ResponseEntity.ok(dto)
    }
}

// --- DTOs ---

data class SongRequestCreateDto(
    val title: String,
    val artist: String,
    val sourceUrl: String
)

data class SongRequestCreateResponseDto(
    val requestId: Long
)

data class SongRequestDto(
    val requestId: Long,
    val userId: Long,
    val roomId: Long,
    val title: String,
    val artist: String,
    val sourceUrl: String,
    val status: String,
    val requestedAt: LocalDateTime,
    val songCount: Int
)
