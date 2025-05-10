package com.example.skystWaffleunivServer.domain

import jakarta.persistence.*

@Entity
@Table(name = "emotion_label")
class EmotionLabelEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String
)