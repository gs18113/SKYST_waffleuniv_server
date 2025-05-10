package com.example.skystWaffleunivServer.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "rooms")
class RoomEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "room_name", nullable = false)
    var roomName: String,
    // 감정 라벨: 여러 방이 같은 감정 라벨을 가질 수 있음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_id", nullable = false)
    var label: EmotionLabelEntity,
    @Column(name = "user_count", nullable = false)
    var userCount: Int = 0,
    @Column(name = "song_count", nullable = false)
    var songCount: Int = 0,
    // 현재 재생 중인 곡: 방마다 한 곡
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_song_id")
    var currentSong: SongRequestEntity? = null,
    @Column(name = "current_song_started_at")
    var currentSongStartedAt: LocalDateTime? = null,
)
