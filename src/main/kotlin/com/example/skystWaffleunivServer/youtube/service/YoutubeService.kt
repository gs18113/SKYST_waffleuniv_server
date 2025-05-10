package com.example.skystWaffleunivServer.youtube.service

import com.example.skystWaffleunivServer.exception.DomainException
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class YoutubeService {
    companion object {
        private val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    }

    @Value("\${spring.youtube.api-key}")
    private val apiKey: String = ""

    fun getVideoDuration(videoId: String): Long {
        val youtube =
            YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY) { }
                .setApplicationName("video-duration-app")
                .setYouTubeRequestInitializer(YouTubeRequestInitializer(apiKey))
                .build()

        val videoRequest = youtube.videos().list("contentDetails")
        videoRequest.key = apiKey
        videoRequest.id = videoId

        val response = videoRequest.execute()

        if (response.items.isEmpty()) {
            throw DomainException(124, HttpStatus.BAD_REQUEST, "Video not found")
        }

        // Duration is in ISO 8601 format (e.g., PT1H2M3S for 1 hour, 2 minutes, 3 seconds)
        return convertDurationToSeconds(response.items[0].contentDetails.duration)
    }

    fun getVideIdFromUrl(url: String): String? {
        val pattern = "(?<=youtu\\.be/|v=|/videos/|embed/|e/|\\?v=|&v=)([^#&?\\n]*)".toRegex()
        val matcher = pattern.find(url)
        return matcher?.groupValues?.getOrNull(1)
    }

    // Convert ISO 8601 duration to seconds
    fun convertDurationToSeconds(isoDuration: String): Long {
        return try {
            val duration = Duration.parse(isoDuration)
            duration.seconds
        } catch (e: Exception) {
            -1L
        }
    }

    // Format duration to readable string
    fun formatDuration(isoDuration: String): String {
        val seconds = convertDurationToSeconds(isoDuration)
        if (seconds < 0) return "Invalid duration"

        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, remainingSeconds)
            else -> String.format("%d:%02d", minutes, remainingSeconds)
        }
    }
}
