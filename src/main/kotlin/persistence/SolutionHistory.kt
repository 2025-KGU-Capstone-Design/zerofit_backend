package com.zerofit.persistence

import org.jetbrains.exposed.sql.Table

object SolutionTable : Table() {
    val id = integer("id").autoIncrement()
    val request_id = integer("request_id").references(SearchHistoryTable.id)
    val user_id = varchar("user_id", length = 50).references(Users.user_id)
    // 종합 (total_optimization), 감축량 비중 (emission_reduction), 절감액 비중 (cost_saving), ROI 기간 비중 (roi)
    val type = varchar("type", length = 100)
    val rank = integer("rank") // 순위
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

object CommentTable : Table() {
    val id = integer("id").autoIncrement()
    val request_id = integer("request_id").references(SearchHistoryTable.id)
    val user_id = varchar("user_id", length = 50).references(Users.user_id)
    val type = varchar("type", length = 100)
    val top1 = text("content")
    val comparison = text("comparison")
    val created_at = long("created_at").default(System.currentTimeMillis())

    override val primaryKey = PrimaryKey(id)
}
