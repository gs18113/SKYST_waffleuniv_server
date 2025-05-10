package com.example.skystWaffleunivServer.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reaction")
class ReactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // 어떤 곡 요청에 대한 반응인지 (여러 Reaction이 하나의 SongRequest에 속함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    var request: SongRequestEntity,

    // 어떤 사용자가 반응했는지 (여러 Reaction이 하나의 User에 속함)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @Column(name = "reacted_at", nullable = false)
    var reactedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var emoji: String
)
