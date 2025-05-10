package com.example.skystWaffleunivServer.service

import com.example.skystWaffleunivServer.exception.DomainException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class AiService(
    @Value("\${spring.ai.openai.api-key}")
    private val openAiKey: String,
) {
    private val client: WebClient =
        WebClient.builder()
            .baseUrl("https://api.openai.com/v1/chat/completions")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $openAiKey")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()

    data class AnalyzeResult(
        val labelId: Long,
        val comment: String,
    )

    /**
     * 사용자 감정 기록(content)을 AI에 보내어
     * - 9개 감정 라벨 중 하나 선택(labelId)
     * - 3~4줄 공감 멘트(comment)
     * JSON 형태로 받아 파싱하여 반환
     */
    fun analyzeEmotion(content: String): AnalyzeResult {
        val systemPrompt =
            """
            다음 사용자 감정 기록을 분석하세요.

            1) 제공된 9가지 감정 카테고리 중 가장 적합한 하나를 선택하고, 그 라벨의 ID(labelId)와 한글 이름(labelName)을 결정합니다.
               라벨 목록:
                 1. 기쁨
                 2. 슬픔
                 3. 분노
                 4. 무기력
                 5. 평온
                 6. 두려움
                 7. 놀람
                 8. 기대
                 9. 혐오

            2) 해당 감정에 맞는 3~4줄 길이의 진심 어린 공감성 멘트를 작성합니다.

            3) **반환 형식은 반드시 순수 JSON 객체**로만 응답해야 하며, 그 외 설명이나 추가 텍스트를 포함하지 마세요.
               예시 형식:
               {
                 "labelId": 2,
                 "labelName": "슬픔",
                 "comment": "..."
               }
            """.trimIndent()

        val userPrompt = "감정 기록: \"$content\""
        val requestBody =
            mapOf(
                "model" to "gpt-3.5-turbo",
                "messages" to
                    listOf(
                        mapOf("role" to "system", "content" to systemPrompt),
                        mapOf("role" to "user", "content" to userPrompt),
                    ),
                "max_tokens" to 300,
                "temperature" to 0.7,
            )

        val responseJson: String =
            client.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String::class.java)
                .block() ?: throw DomainException(123, HttpStatus.INTERNAL_SERVER_ERROR, "Empty response from OpenAI")

        val mapper = jacksonObjectMapper()
        val root = mapper.readTree(responseJson)
        val messageContent =
            root["choices"]
                ?.get(0)
                ?.get("message")
                ?.get("content")
                ?.asText()
                ?: throw DomainException(125, HttpStatus.INTERNAL_SERVER_ERROR, "No content in OpenAI response")

        // AI가 순수 JSON만 반환한다고 가정하고 파싱
        val resultNode = mapper.readTree(messageContent)
        val labelId =
            resultNode["labelId"]?.asLong()
                ?: throw DomainException(500, HttpStatus.INTERNAL_SERVER_ERROR, "labelId missing in AI response")
        val comment =
            resultNode["comment"]?.asText()
                ?: throw DomainException(500, HttpStatus.INTERNAL_SERVER_ERROR, "comment missing in AI response")

        return AnalyzeResult(labelId, comment.trim())
    }
}
