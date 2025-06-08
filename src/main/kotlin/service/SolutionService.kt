package com.zerofit.service

import com.zerofit.model.AiSolutionResponse
import com.zerofit.model.Comment
import com.zerofit.model.SearchHistory
import com.zerofit.model.SolutionHistory
import com.zerofit.persistence.*
import com.zerofit.model.SearchParams
import com.zerofit.model.Solution
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

interface SolutionService {

    suspend fun requestSolution(searchParams: SearchParams): List<Solution>
    suspend fun requestComment(llmParams: List<Solution>): Comment
    suspend fun createSearchHistory(userId: String, searchParams: SearchParams): Int
    suspend fun getSearchHistory(userId: String): List<SearchHistory>

    suspend fun createSolutionHistory(userId: String, requestId: Int, solutions: List<Solution>)
    suspend fun getSolutionByReqId(requestId: Int): List<Solution>
    suspend fun getBookmarkedSolutions(userId: String): List<Solution>
    suspend fun getSolutionBySolId(solId: Int): SolutionHistory?
    suspend fun updateSolution(userId: String, solId: Int): Int

    suspend fun createCommentHistory(userId: String, requestId: Int, comment: Comment): Int
    suspend fun getCommentByReqId(requestId: Int): List<Comment>
}

open class SolutionServiceImpl : SolutionService, BaseService() {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true // AI 서버 응답에 Solution DTO에 없는 필드가 있어도 오류 방지
            })
        }
    }

    val ai: String = "zerofit-ai:8000" // AI 서버 컨테이너 이름

    override suspend fun requestSolution(searchParams: SearchParams): List<Solution> {
        val aiServerUrl = "http://$ai/recommend"

        try {
            val response = client.post(aiServerUrl) {
                contentType(ContentType.Application.Json)
                setBody(searchParams)
            }

            val aiResponse = response.body<AiSolutionResponse>()

            return aiResponse.solution
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to request solution from AI server: ${e.message}")
        }
    }

    override suspend fun requestComment(llmParams: List<Solution>): Comment {
        val aiServerUrl = "http://$ai/comment"

        try {
            val response = client.post(aiServerUrl) {
                contentType(ContentType.Application.Json)
                setBody(hashMapOf("llmParams" to llmParams))
            }

            return response.body<Comment>()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to request comment from AI server: ${e.message}")
        }
    }

    override suspend fun createSearchHistory(
        userId: String,
        searchParams: SearchParams
    ): Int = dbQuery {
        val id = SearchHistoryTable.insert {
            it[user_id] = userId
            it[industry] = searchParams.industry
            it[available_investment] = searchParams.availableInvestment
            it[current_emission] = searchParams.currentEmission
            it[target_emission] = searchParams.targetEmission
            it[target_roi_period] = searchParams.targetRoiPeriod
        }[SearchHistoryTable.id]

        searchParams.targetFacilities.forEach { facility ->
            FacilitiesTable.insert {
                it[requestId] = id
                it[FacilitiesTable.facility] = facility
            }
        }

        return@dbQuery id
    }

    override suspend fun getSearchHistory(userId: String): List<SearchHistory> = dbQuery {
        SearchHistoryTable.selectAll()
            .where(SearchHistoryTable.user_id eq userId)
            .map {
                SearchHistory(
                    id = it[SearchHistoryTable.id],
                    userId = it[SearchHistoryTable.user_id],
                    industry = it[SearchHistoryTable.industry],
                    targetFacilities = FacilitiesTable.selectAll()
                        .where { FacilitiesTable.requestId eq it[SearchHistoryTable.id] }
                        .map { row -> row[FacilitiesTable.facility] },
                    availableInvestment = it[SearchHistoryTable.available_investment],
                    currentEmission = it[SearchHistoryTable.current_emission],
                    targetEmission = it[SearchHistoryTable.target_emission],
                    targetRoiPeriod = it[SearchHistoryTable.target_roi_period]
                )
            }
    }

    override suspend fun createSolutionHistory(
        userId: String,
        requestId: Int,
        solutions: List<Solution>
    ): Unit = dbQuery {
        solutions.forEach { s ->
            SolutionTable.insert {
                it[request_id] = requestId
                it[user_id] = userId
                it[type] = s.type
                it[rank] = s.rank
                it[improvement_type] = s.improvementType
                it[facility] = s.facility
                it[activity] = s.activity
                it[industry] = s.industry
                it[emission_reduction] = s.emissionReduction
                it[cost_saving] = s.costSaving
                it[roi_period] = s.roiPeriod
                it[investment_cost] = s.investmentCost
            }
        }
    }


    override suspend fun getSolutionByReqId(requestId: Int): List<Solution> = dbQuery {
        SolutionTable.selectAll()
            .where { SolutionTable.request_id eq requestId }
            .map {
                Solution(
                    id = it[SolutionTable.id],
                    type = it[SolutionTable.type],
                    rank = it[SolutionTable.rank],
                    improvementType = it[SolutionTable.improvement_type],
                    facility = it[SolutionTable.facility],
                    activity = it[SolutionTable.activity],
                    industry = it[SolutionTable.industry],
                    emissionReduction = it[SolutionTable.emission_reduction],
                    costSaving = it[SolutionTable.cost_saving],
                    roiPeriod = it[SolutionTable.roi_period],
                    investmentCost = it[SolutionTable.investment_cost],
                    bookmark = it[SolutionTable.bookmark]
                )
            }
    }

    override suspend fun updateSolution(userId: String, solId: Int): Int = dbQuery {
        SolutionTable.update({
            SolutionTable.id.eq(solId) and
                    SolutionTable.user_id.eq(userId)
        }) {
            it[bookmark] = not(bookmark)
        }
    }

    override suspend fun createCommentHistory(
        userId: String,
        requestId: Int,
        comment: Comment
    ): Int = dbQuery {
        CommentTable.insert {
            it[request_id] = requestId
            it[user_id] = userId
            it[type] = comment.type
            it[top1] = comment.top1
            it[comparison] = comment.comparison
        }[CommentTable.id]
    }

    override suspend fun getCommentByReqId(requestId: Int): List<Comment> = dbQuery {
        CommentTable.selectAll()
            .where { CommentTable.request_id eq requestId }
            .map {
                Comment(
                    id = it[CommentTable.id],
                    type = it[CommentTable.type],
                    top1 = it[CommentTable.top1],
                    comparison = it[CommentTable.comparison]
                )
            }
    }

    override suspend fun getBookmarkedSolutions(userId: String): List<Solution> = dbQuery {
        SolutionTable.selectAll()
            .where {
                SolutionTable.user_id.eq(userId) and
                        SolutionTable.bookmark
            }
            .map {
                Solution(
                    id = it[SolutionTable.id],
                    type = it[SolutionTable.type],
                    rank = it[SolutionTable.rank],
                    improvementType = it[SolutionTable.improvement_type],
                    facility = it[SolutionTable.facility],
                    activity = it[SolutionTable.activity],
                    industry = it[SolutionTable.industry],
                    emissionReduction = it[SolutionTable.emission_reduction],
                    costSaving = it[SolutionTable.cost_saving],
                    roiPeriod = it[SolutionTable.roi_period],
                    investmentCost = it[SolutionTable.investment_cost],
                    bookmark = it[SolutionTable.bookmark]
                )
            }
    }

    override suspend fun getSolutionBySolId(solId: Int): SolutionHistory? = dbQuery {
        SolutionTable.selectAll()
            .where { SolutionTable.id eq solId }
            .map {
                SolutionHistory(
                    id = it[SolutionTable.id],
                    requestId = it[SolutionTable.request_id],
                    userId = it[SolutionTable.user_id],
                    type = it[SolutionTable.type],
                    rank = it[SolutionTable.rank],
                    improvementType = it[SolutionTable.improvement_type],
                    facility = it[SolutionTable.facility],
                    activity = it[SolutionTable.activity],
                    industry = it[SolutionTable.industry],
                    emissionReduction = it[SolutionTable.emission_reduction],
                    costSaving = it[SolutionTable.cost_saving],
                    roiPeriod = it[SolutionTable.roi_period],
                    investmentCost = it[SolutionTable.investment_cost],
                    bookmark = it[SolutionTable.bookmark]
                )
            }
            .singleOrNull()
    }
}
