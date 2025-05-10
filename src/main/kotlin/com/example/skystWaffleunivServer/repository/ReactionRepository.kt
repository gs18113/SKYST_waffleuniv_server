package com.example.skystWaffleunivServer.repository

import com.example.skystWaffleunivServer.domain.ReactionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ReactionRepository : JpaRepository<ReactionEntity, Long>
