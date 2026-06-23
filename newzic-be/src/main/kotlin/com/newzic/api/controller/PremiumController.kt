package com.newzic.api.controller

import com.newzic.api.dto.*
import com.newzic.service.PremiumService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/premium")
class PremiumController(
    private val premiumService: PremiumService
) {

    @GetMapping("/status")
    fun getStatus(auth: Authentication): PremiumStatusResponse {
        val userId = auth.principal as UUID
        return premiumService.getStatus(userId)
    }

    @PostMapping("/activate")
    fun activate(auth: Authentication, @RequestBody request: ActivatePremiumRequest): PremiumStatusResponse {
        val userId = auth.principal as UUID
        return premiumService.activate(userId, request)
    }

    @PostMapping("/deactivate")
    fun deactivate(auth: Authentication): PremiumStatusResponse {
        val userId = auth.principal as UUID
        return premiumService.deactivate(userId)
    }

    @PutMapping("/custom-url")
    fun setCustomUrl(auth: Authentication, @RequestBody request: SetCustomUrlRequest): PremiumStatusResponse {
        val userId = auth.principal as UUID
        return premiumService.setCustomUrl(userId, request)
    }

    @GetMapping("/limits")
    fun getLimits(auth: Authentication): PremiumLimitsResponse {
        val userId = auth.principal as UUID
        return premiumService.getLimits(userId)
    }

    // ── Donations ──

    @PostMapping("/donate")
    fun donate(auth: Authentication, @RequestBody request: CreateDonationRequest): DonationResponse {
        val userId = auth.principal as UUID
        return premiumService.donate(userId, request)
    }

    @GetMapping("/donations/dashboard")
    fun donationDashboard(auth: Authentication): DonationDashboardResponse {
        val userId = auth.principal as UUID
        return premiumService.getDonationDashboard(userId)
    }

    @GetMapping("/donations/{artistId}")
    fun artistDonationDashboard(auth: Authentication, @PathVariable artistId: UUID): DonationDashboardResponse {
        return premiumService.getDonationDashboard(artistId)
    }
}
