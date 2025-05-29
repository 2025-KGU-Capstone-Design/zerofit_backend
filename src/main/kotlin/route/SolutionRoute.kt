package com.zerofit.route

import com.zerofit.service.SolutionService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class SearchParams(
    val industry: String,
    val targetFacilities: List<String>,
    val availableInvestment: Double,
    val currentEmission: Double,
    val targetEmission: Double,
    val targetRoiPeriod: Double
)

@Serializable
data class Solution(
    val type: String,
    val rank: Int,
    val score: Double? = null,
    val improvementType: String,
    val facility: String,
    val activity: String,
    val industry: String,
    val emissionReduction: Double,
    val costSaving: Double,
    val roiPeriod: Double,
    val investmentCost: Double
)

fun Route.solutionRoute(solutionService: SolutionService) {

    authenticate("auth-jwt") {
        //솔루션
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

            call.respond(HttpStatusCode.OK, hashMapOf("solution" to responseSol))
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
            val solId = call.parameters["solId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid solution ID")

            // 솔루션 업데이트
            val updatedSolution = solutionService.updateSolution(solId)

            call.respond(HttpStatusCode.OK, updatedSolution)
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
    }
}