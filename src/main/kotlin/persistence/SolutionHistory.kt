package com.zerofit.persistence

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class SolutionHistory(
    val id: Int,
    val requestId: Int,
    val userId: String?,
    val type: String, // 종합 (total_optimization), 감축량 비중 (emission_reduction), 절감액 비중 (cost_saving), ROI 기간 비중 (roi)
    val rank: Int, // 순위
    val score: Double? = null, // 점수
    val improvementType: String,
    val facility: String,
    val activity: String,
    val industry: String,
    val emissionReduction: Double, // 감축량
    val costSaving: Double, // 절감액
    val roiPeriod: Double, // ROI 기간
    val investmentCost: Double, // 투자 비용
    val bookmark: Boolean = false, // 스크랩 여부
)

object SolutionTable : Table() {
    val id = integer("id").autoIncrement()
    val request_id = integer("request_id").references(SearchHistoryTable.id)
    val user_id = varchar("user_id", length = 50).references(Users.user_id)
    // 종합 (total_optimization), 감축량 비중 (emission_reduction), 절감액 비중 (cost_saving), ROI 기간 비중 (roi)
    val type = varchar("type", length = 100)
    val rank = integer("rank") // 순위
    val score = double("score").nullable()
    val improvement_type = varchar("improvement_type", length = 100)
    val facility = varchar("facility", length = 100)
    val activity = varchar("activity", length = 100)
    val industry = varchar("industry", length = 100)
    val emission_reduction = double("emission_reduction") // 감축량
    val cost_saving = double("cost_saving") // 절감액
    val roi_period = double("roi_period") // ROI 기간
    val investment_cost = double("investment_cost") // 투자 비용
    val bookmark = bool("bookmark").default(false) // 스크랩 여부
    val created_at = long("created_at").default(System.currentTimeMillis())

    override val primaryKey = PrimaryKey(id)
}
