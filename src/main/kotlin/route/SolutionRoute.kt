package com.zerofit.route

import com.zerofit.model.Comment
import com.zerofit.model.CommentReqDto
import com.zerofit.model.SearchParams
import com.zerofit.model.Solution
import com.zerofit.service.SolutionService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class SolutionDto(
    val requestId: Int,
    val solution: List<Solution>
)

@Serializable
data class HistoryDto(
    val comment: List<Comment>,
    val solution: List<Solution>
)

fun Route.solutionRoute(solutionService: SolutionService) {

    authenticate("auth-jwt") {
        //솔루션 요청
        post("/api/solution/request") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: throw IllegalArgumentException("User ID not found in token")
            val searchParams = call.receive<SearchParams>()

            // 요청 기록 및 ID 생성
            val requestId = solutionService.createSearchHistory(
                userId,
                searchParams
            )
            //AI 서버에 솔루션 요청
            val solutions = solutionService.requestSolution(searchParams)
            // 솔루션 기록 생성
            solutionService.createSolutionHistory(
                userId,
                requestId,
                solutions
            )
            // 요청 기록 조회
            val responseSol = solutionService.getSolutionByReqId(requestId)

            val solutionDto = SolutionDto(
                requestId = requestId,
                solution = responseSol
            )

            call.respond(HttpStatusCode.OK, solutionDto)
        }

        // type별 comment 요청
        post("/api/solution/comment/{requestId}") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: throw IllegalArgumentException("User ID not found in token")

            val requestId = call.parameters["requestId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid request ID")

            val llmParams = call.receive<CommentReqDto>().llmParams

            // AI 서버에 코멘트 요청
            val comment = solutionService.requestComment(llmParams)

            solutionService.createCommentHistory(userId, requestId, comment)

            call.respond(HttpStatusCode.OK, comment)
        }

        //북마크된 솔루션 조회
        get("/api/solution/bookmark") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: throw IllegalArgumentException("User ID not found in token")

            val bookmarkedSolutions = solutionService.getBookmarkedSolutions(userId)

            call.respond(HttpStatusCode.OK, bookmarkedSolutions)
        }

        // 솔루션 북마크
        put("/api/solution/{solId}") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: throw IllegalArgumentException("User ID not found in token")

            val solId = call.parameters["solId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid solution ID")

            // 솔루션 업데이트
            val updatedSolution = solutionService.updateSolution(userId, solId)

            if (updatedSolution == 0) {
                throw IllegalArgumentException("Solution not found or user does not have permission to update this solution.")
            }

            call.respond(HttpStatusCode.OK, "업데이트 성공")
        }

        //솔루션 상세 조회
        get("/api/solution/{solId}") {
            val solId = call.parameters["solId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid solution ID")

            // 솔루션 상세 조회
            val solution = solutionService.getSolutionBySolId(solId)
            if (solution != null) {
                call.respond(HttpStatusCode.OK, solution)
            } else {
                call.respond(HttpStatusCode.NotFound, "Solution not found")
            }
        }

        //검색내역 조회
        get("/api/search/history") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
                ?: throw IllegalArgumentException("User ID not found in token")

            // 검색 내역 조회
            val searchHistory = solutionService.getSearchHistory(userId)

            call.respond(HttpStatusCode.OK, searchHistory)
        }

        // 검색 내역에 해당하는 솔루션 기록 조회
        get("/api/solution/history/{requestId}") {
            val requestId = call.parameters["requestId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid request ID")

            val solutionHistory = solutionService.getSolutionByReqId(requestId)
            val commentHistory = solutionService.getCommentByReqId(requestId)

            val historyDto = HistoryDto(
                comment = commentHistory,
                solution = solutionHistory
            )
            if (solutionHistory.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, historyDto)
            } else {
                call.respond(HttpStatusCode.NotFound, "No solutions found for the given request ID")
            }
        }
    }
}