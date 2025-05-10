package com.example.skystWaffleunivServer.controller

import com.example.skystWaffleunivServer.service.EmotionLabelService
import com.example.skystWaffleunivServer.service.ReactionService
import com.example.skystWaffleunivServer.service.RoomService
import com.example.skystWaffleunivServer.service.SongRequestService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/rooms")
class RoomController(
    private val roomService: RoomService,
    private val songRequestService: SongRequestService,
    private val reactionService: ReactionService,
    private val emotionLabelService: EmotionLabelService
) {


}