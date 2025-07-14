package org.example

import io.ktor.client.HttpClient
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.netty.Netty
import io.ktor.server.routing.get
import io.ktor.server.routing.head
import io.ktor.server.routing.routing

//TIP 코드를 <b>실행</b>하려면 <shortcut actionId="Run"/>을(를) 누르거나
// 에디터 여백에 있는 <icon src="AllIcons.Actions.Execute"/> 아이콘을 클릭하세요.
fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val arduinoIpAddress = "YOUR_ARDUINO_ESP32_IP_ADDRESS" // 아두이노 ESP32의 IP 주소로 변경
    val httpClient = HttpClient(CIO)

    routing {
        get("/") {
            call.respondHtml {
                head {
                    title("Door Lock Control")
                }
                body {
                    h1 { +"무선 자동 문 잠금장치 제어" }

                    // 현재 문 상태를 아두이노에서 가져와 표시
                    val currentState = try {
                        val response = httpClient.get("http://$arduinoIpAddress")
                        response.bodyAsText()
                    } catch (e: Exception) {
                        "오류: ${e.message}"
                    }
                    p {
                        +"현재 상태: "
                        b { +currentState }
                    }

                    // 잠금/해제 버튼
                    p {
                        a(href = "/lock") {
                            button { +"문 잠그기" }
                        }
                        + " "
                        a(href = "/unlock") {
                            button { +"문 열기" }
                        }
                    }
                }
            }
        }

        get("/lock") {
            try {
                val response = httpClient.get("http://$arduinoIpAddress/lock")
                if (response.status.value == 200) {
                    call.respondHtml {
                        head { title("Door Locked") }
                        body {
                            h2 { +"문이 잠겼습니다." }
                            p { a(href = "/") { +"돌아가기" } }
                        }
                    }
                } else {
                    call.respondHtml {
                        head { title("Error") }
                        body {
                            h2 { +"문 잠금 실패: ${response.status.description}" }
                            p { a(href = "/") { +"돌아가기" } }
                        }
                    }
                }
            } catch (e: Exception) {
                call.respondHtml {
                    head { title("Error") }
                    body {
                        h2 { +"문 잠금 중 오류 발생: ${e.message}" }
                        p { a(href = "/") { +"돌아가기" } }
                    }
                }
            }
        }

        get("/unlock") {
            try {
                val response = httpClient.get("http://$arduinoIpAddress/unlock")
                if (response.status.value == 200) {
                    call.respondHtml {
                        head { title("Door Unlocked") }
                        body {
                            h2 { +"문이 열렸습니다." }
                            p { a(href = "/") { +"돌아가기" } }
                        }
                    }
                } else {
                    call.respondHtml {
                        head { title("Error") }
                        body {
                            h2 { +"문 열기 실패: ${response.status.description}" }
                            p { a(href = "/") { +"돌아가기" } }
                        }
                    }
                }
            } catch (e: Exception) {
                call.respondHtml {
                    head { title("Error") }
                    body {
                        h2 { +"문 열기 중 오류 발생: ${e.message}" }
                        p { a(href = "/") { +"돌아가기" } }
                    }
                }
            }
        }
    }
}