package com.example.ticketmasterretailandticketsdemo.util

import com.ticketmaster.authenticationsdk.TMXDeploymentRegion
import com.ticketmaster.discoveryapi.enums.TMMarketDomain

fun getMarketDomain(market: String): TMMarketDomain {
    return when (market) {
        "CA" -> TMMarketDomain.CA
        else -> TMMarketDomain.US
    }
}

fun getDeploymentRegion(region: String): TMXDeploymentRegion {
    return when (region) {
        "UK" -> TMXDeploymentRegion.UK
        else -> TMXDeploymentRegion.US
    }
}