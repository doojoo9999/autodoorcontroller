package org.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.thymeleaf.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

//TIP 코드를 <b>실행</b>하려면 <shortcut actionId="Run"/>을(를) 누르거나
// 에디터 여백에 있는 <icon src="AllIcons.Actions.Execute"/> 아이콘을 클릭하세요.
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val arduinoIpAddress = "YOUR_ARDUINO_ESP32_IP_ADDRESS" // 아두이노 ESP32의 IP 주소로 변경
    val httpClient = HttpClient(CIO)

    // Thymeleaf 템플릿 엔진 설치
    install(Thymeleaf) {
        val templateResolver = ClassLoaderTemplateResolver().apply {
            prefix = "templates/" // 템플릿 파일이 위치한 디렉토리
            suffix = ".html"      // 템플릿 파일 확장자
            characterEncoding = "UTF-8"
            // 캐시를 비활성화하여 개발 시 변경 사항이 즉시 반영되도록 합니다. (운영 시에는 true)
            isCacheable = false
        }
        setTemplateResolver(templateResolver)
    }

    routing {
        get("/") {
            val doorState = try {
                val response = httpClient.get("http://$arduinoIpAddress")
                // 아두이노 응답에서 "Closed" 또는 "Open" 문자열 파싱
                val responseText = response.bodyAsText().trim() // 공백 제거
                if (responseText.contains("Closed", ignoreCase = true)) "Closed" else "Open"
            } catch (e: Exception) {
                println("Error fetching door state: ${e.message}")
                "알 수 없음 (오류: ${e.message})"
            }

            // Thymeleaf 템플릿에 데이터 전달 및 렌더링
            call.respond(
                ThymeleafContent(
                    "index.html", mapOf(
                        "doorState" to doorState
                    )
                )
            )
        }

        post("/lock") {
            var message: String? = null
            var isError: Boolean = false
            try {
                val response = httpClient.post("http://$arduinoIpAddress/lock")
                if (response.status == HttpStatusCode.OK) {
                    message = "문이 성공적으로 잠겼습니다."
                } else {
                    message = "문 잠금 실패: ${response.status.description}"
                    isError = true
                }
            } catch (e: Exception) {
                message = "문 잠금 중 오류 발생: ${e.message}"
                isError = true
            }

            // 아두이노에서 최신 상태를 다시 가져와서 Thymeleaf에 전달
            val doorState = try {
                val response = httpClient.get("http://$arduinoIpAddress")
                val responseText = response.bodyAsText().trim()
                if (responseText.contains("Closed", ignoreCase = true)) "Closed" else "Open"
            } catch (e: Exception) {
                "알 수 없음 (오류: ${e.message})"
            }

            call.respond(
                ThymeleafContent(
                    "index.html", mapOf(
                        "message" to message,
                        "isError" to isError,
                        "doorState" to doorState
                    )
                )
            )
        }

        post("/unlock") {
            var message: String? = null
            var isError: Boolean = false
            try {
                val response = httpClient.post("http://$arduinoIpAddress/unlock")
                if (response.status == HttpStatusCode.OK) {
                    message = "문이 성공적으로 열렸습니다."
                } else {
                    message = "문 열기 실패: ${response.status.description}"
                    isError = true
                }
            } catch (e: Exception) {
                message = "문 열기 중 오류 발생: ${e.message}"
                isError = true
            }

            // 아두이노에서 최신 상태를 다시 가져와서 Thymeleaf에 전달
            val doorState = try {
                val response = httpClient.get("http://$arduinoIpAddress")
                val responseText = response.bodyAsText().trim()
                if (responseText.contains("Closed", ignoreCase = true)) "Closed" else "Open"
            } catch (e: Exception) {
                "알 수 없음 (오류: ${e.message})"
            }

            call.respond(
                ThymeleafContent(
                    "index.html", mapOf(
                        "message" to message,
                        "isError" to isError,
                        "doorState" to doorState
                    )
                )
            )
        }
    }
}