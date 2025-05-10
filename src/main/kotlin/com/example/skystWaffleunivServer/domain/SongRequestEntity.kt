package com.example.skystWaffleunivServer.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "song_request")
class SongRequestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    // 어떤 방에 대한 요청인지 (여러 요청이 하나의 방에 속함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    var room: RoomEntity,
    // 어떤 사용자가 요청했는지 (각 사용자는 하나의 요청만 가질 수 있음)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    var user: UserEntity,
    @Column(nullable = false)
    var title: String,
    @Column(nullable = false)
    var artist: String,
    @Column(name = "source_url", nullable = false)
    var sourceUrl: String,
    @Column(name = "requested_at", nullable = false)
    var requestedAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var duration: Long,
    @Column(nullable = false)
    var status: String,
)
