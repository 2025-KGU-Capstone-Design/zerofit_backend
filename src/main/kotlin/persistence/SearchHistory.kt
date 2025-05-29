package com.zerofit.persistence

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class SearchHistory(
    val id: Int,
    val userId: String,
    val industry: String,
    val targetFacilities: List<String>,
    val availableInvestment: Double,
    val currentEmission: Double,
    val targetEmission: Double,
    val targetRoiPeriod: Double
)

object SearchHistoryTable : Table() {
    val id = integer("id").autoIncrement()
    val user_id = varchar("user_id", length = 50).references(Users.user_id)
    val industry = varchar("industry", length = 100)
    val available_investment = double("available_investment")
    val current_emission = double("current_emission")
    val target_emission = double("target_emission")
    val target_roi_period = double("target_roi_period")
    val created_at = long("created_at").default(System.currentTimeMillis())

    override val primaryKey = PrimaryKey(id)
}

object FacilitiesTable : Table() {
    val id = integer("id").autoIncrement()
    val requestId = integer("request_id").references(SearchHistoryTable.id)
    val facility = varchar("facility", length = 100)

    override val primaryKey = PrimaryKey(id)
}