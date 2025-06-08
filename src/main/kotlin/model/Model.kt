package com.zerofit.model

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: Int?,
    val type: String,
    val top1: String, // 1위 솔루션
    val comparison: String, // 비교 내용
)

@Serializable
data class SolutionHistory(
    val id: Int,
    val requestId: Int,
    val userId: String?,
    val type: String, // 종합 (total_optimization), 감축량 비중 (emission_reduction), 절감액 비중 (cost_saving), ROI 기간 비중 (roi)
    val rank: Int, // 순위
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

@Serializable
data class RequestLogin(
    val userId: String,
    val password: String
)

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

@Serializable
data class User(
    val userId: String,
    val password: String,
    val email: String,
    val phone: String,
    val companyName: String
)

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
    val id: Int?,
    val type: String,
    val rank: Int,
    val improvementType: String,
    val facility: String,
    val activity: String,
    val industry: String,
    val emissionReduction: Double,
    val costSaving: Double,
    val roiPeriod: Double,
    val investmentCost: Double,
    val bookmark: Boolean?
)

@Serializable
data class AiSolutionResponse(
    val solution: List<Solution>
)

@Serializable
data class CommentReqDto(
    val llmParams: List<Solution>,
)