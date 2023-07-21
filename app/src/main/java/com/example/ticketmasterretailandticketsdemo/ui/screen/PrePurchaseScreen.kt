package com.example.ticketmasterretailandticketsdemo.ui.screen

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.ticketmasterretailandticketsdemo.R
import com.example.ticketmasterretailandticketsdemo.ui.util.FragmentContainer
import com.example.ticketmasterretailandticketsdemo.ui.util.composeBridge
import com.ticketmaster.discoveryapi.enums.TMMarketDomain
import com.ticketmaster.discoveryapi.models.DiscoveryAbstractEntity
import com.ticketmaster.discoveryapi.models.DiscoveryAttraction
import com.ticketmaster.discoveryapi.models.DiscoveryEvent
import com.ticketmaster.discoveryapi.models.DiscoveryVenue
import com.ticketmaster.prepurchase.TMPrePurchase
import com.ticketmaster.prepurchase.TMPrePurchaseFragmentFactory
import com.ticketmaster.prepurchase.TMPrePurchaseWebsiteConfiguration
import com.ticketmaster.prepurchase.data.CoordinatesWithMarketDomain
import com.ticketmaster.prepurchase.data.Location
import com.ticketmaster.prepurchase.listener.TMPrePurchaseNavigationListener
import kotlinx.coroutines.launch

@Composable
fun PrePurchaseScreen(
    configItems: ConfigItems,
    attractionId: String? = "",
    venueId: String? = ""
) {
    val context = LocalContext.current
    val activity = context as? AppCompatActivity ?: return

    setupPrePurchase(activity, configItems, attractionId, venueId)
}

fun setupPrePurchase(
    activity: AppCompatActivity,
    configItems: ConfigItems,
    attractionId: String?,
    venueId: String?
) {
    activity.lifecycleScope.launch {
        val abstractEntity = if (attractionId != null) {
            DiscoveryAttraction(hostID = attractionId)
        } else if (venueId != null) {
            DiscoveryVenue(hostID = venueId)
        } else null

        val tMPrePurchaseWebsiteConfiguration = TMPrePurchaseWebsiteConfiguration(
            abstractEntity = abstractEntity,
            hostType = TMMarketDomain.US
        )

        val tmPrePurchase = TMPrePurchase(
            brandColor = ContextCompat.getColor(
                activity,
                R.color.black
            ),
            discoveryAPIKey = configItems.apiKey
        )

        val factory = TMPrePurchaseFragmentFactory(
            tmPrePurchaseNavigationListener = PurchaseNavigationListener { activity.finish() }
        )

        val fragmentManager = activity.supportFragmentManager
        fragmentManager.fragmentFactory = factory

        val bundle = tmPrePurchase.getPrePurchaseBundle(
            tMPrePurchaseWebsiteConfiguration
        )

        val fragment = factory
            .instantiatePrePurchase(activity.classLoader).apply {
                arguments = bundle
            }

        composeBridge {
            FragmentContainer(
                fragmentManager = fragmentManager,
                commit = { add(it, fragment) }
            )
        }
    }
}

class PurchaseNavigationListener(private val closeScreen: () -> Unit) :
    TMPrePurchaseNavigationListener {

    override fun onDidRequestCurrentLocation(
        globalMarketDomain: TMMarketDomain?,
        completion: ((CoordinatesWithMarketDomain) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun onDidUpdateCurrentLocation(
        globalMarketDomain: TMMarketDomain?,
        location: Location
    ) {
        TODO("Not yet implemented")
    }

    override fun onPrePurchaseClosed() {
        closeScreen.invoke()
    }

    override fun openEventDetailsPage(
        abstractEntity: DiscoveryAbstractEntity?,
        event: DiscoveryEvent
    ) {
        TODO("Not yet implemented")
    }
}