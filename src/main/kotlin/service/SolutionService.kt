package com.zerofit.service

import com.zerofit.persistence.FacilitiesTable
import com.zerofit.persistence.SearchHistory
import com.zerofit.persistence.SearchHistoryTable
import com.zerofit.persistence.SolutionHistory
import com.zerofit.persistence.SolutionTable
import com.zerofit.route.SearchParams
import com.zerofit.route.Solution
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

interface SolutionService {

    suspend fun requestSolution(searchParams: SearchParams): List<Solution>
    suspend fun createSearchHistory(userId: String, searchParams: SearchParams): Int
    suspend fun getSearchHistory(userId: String): List<SearchHistory>

    suspend fun createSolutionHistory(userId: String, requestId: Int, solutions: List<Solution>)
    suspend fun getSolutionByReqId(requestId: Int): List<SolutionHistory>
    suspend fun getBookmarkedSolutions(userId: String): List<SolutionHistory>
    suspend fun getSolutionBySolId(solId: Int): SolutionHistory?
    suspend fun updateSolution(solId: Int): Int
}

open class SolutionServiceImpl : SolutionService, BaseService() {

    override suspend fun requestSolution(searchParams: SearchParams): List<Solution> {
        // 이 부분은 실제 AI 서버와의 통신을 구현해야 합니다.
        val dummySolutions = listOf(
            Solution(
                type = "total_optimization",
                rank = 1,
                score = 95.38,
                improvementType = "설비 개선",
                facility = "보일러",
                activity = "고효율 보일러 교체",
                industry = "철강",
                emissionReduction = 120.5,
                costSaving = 3000.0,
                roiPeriod = 2.5,
                investmentCost = 750.0,
            ),
            Solution(
                type = "emission_reduction",
                rank = 1,
                improvementType = "프로세스 최적화",
                facility = "냉동기",
                activity = "스마트 모니터링 도입",
                industry = "철강",
                emissionReduction = 80.0,
                costSaving = 2000.0,
                roiPeriod = 4.0,
                investmentCost = 450.0,
            ),
            Solution(
                type = "cost_saving",
                rank = 1,
                improvementType = "공정 개선",
                facility = "냉동기",
                activity = "효율적인 냉각 시스템 도입",
                industry = "철강",
                emissionReduction = 100.0,
                costSaving = 2500.0,
                roiPeriod = 3.0,
                investmentCost = 600.0,
            ),
            Solution(
                type = "roi",
                rank = 1,
                improvementType = "설비 개선",
                facility = "보일러",
                activity = "연료 전환",
                industry = "철강",
                emissionReduction = 90.0,
                costSaving = 2200.0,
                roiPeriod = 3.5,
                investmentCost = 500.0,
            ),
        )

        return dummySolutions
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
                it[score] = s.score
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


    override suspend fun getSolutionByReqId(requestId: Int): List<SolutionHistory> = dbQuery {
        SolutionTable.selectAll()
            .where { SolutionTable.request_id eq requestId }
            .map {
                SolutionHistory(
                    id = it[SolutionTable.id],
                    requestId = it[SolutionTable.request_id],
                    userId = it[SolutionTable.user_id],
                    type = it[SolutionTable.type],
                    rank = it[SolutionTable.rank],
                    score = it[SolutionTable.score],
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

    override suspend fun updateSolution(solId: Int): Int = dbQuery {
        SolutionTable.update({ SolutionTable.id eq solId }) {
            it[bookmark] = not(bookmark)
        }
    }

    override suspend fun getBookmarkedSolutions(userId: String): List<SolutionHistory> = dbQuery {
        SolutionTable.selectAll()
            .where { SolutionTable.user_id eq userId }
            .filter { it[SolutionTable.bookmark] }
            .map {
                SolutionHistory(
                    id = it[SolutionTable.id],
                    requestId = it[SolutionTable.request_id],
                    userId = it[SolutionTable.user_id],
                    type = it[SolutionTable.type],
                    rank = it[SolutionTable.rank],
                    score = it[SolutionTable.score],
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
                    score = it[SolutionTable.score],
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
