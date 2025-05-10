package com.example.skystWaffleunivServer.repository

import com.example.skystWaffleunivServer.domain.SongRequestEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SongRequestRepository : JpaRepository<SongRequestEntity, Long>
