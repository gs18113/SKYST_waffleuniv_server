package com.example.skystWaffleunivServer.dto

class ReactionDto(
    val name: String,
) {
    // Add a no-args constructor for Jackson
    constructor() : this("")
}
