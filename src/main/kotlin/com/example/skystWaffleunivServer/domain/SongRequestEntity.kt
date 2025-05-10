package com.example.skystWaffleunivServer.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "song_request",
)
class SongRequestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    // 어떤 방에 대한 요청인지 (여러 요청이 하나의 방에 속함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    var room: RoomEntity,
    // 어떤 사용자가 요청했는지 (각 사용자는 하나의 요청만 가질 수 있음)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
    @Column(nullable = false)
    var title: String,
    @Column(nullable = false)
    var artist: String,
    @Column(name = "video_id", nullable = false)
    var videoId: String,
    @Column(name = "requested_at", nullable = false)
    var requestedAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var duration: Long,
    @Column(nullable = false)
    var status: String,
    @Column(nullable = false)
    var comment: String,
    @Column(nullable = false)
    var fullStory: String,
)
