package com.example.ticketmasterretailandticketsdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ticketmasterretailandticketsdemo.utils.parcelable
import com.ticketmaster.authenticationsdk.TMXDeploymentEnvironment
import com.ticketmaster.authenticationsdk.TMXDeploymentRegion
import com.ticketmaster.discoveryapi.enums.TMMarketDomain
import com.ticketmaster.discoveryapi.models.DiscoveryEvent
import com.ticketmaster.discoveryapi.models.MemberInfo
import com.ticketmaster.foundation.entity.TMAuthenticationParams
import com.ticketmaster.purchase.TMPurchase
import com.ticketmaster.purchase.TMPurchaseFragmentFactory
import com.ticketmaster.purchase.TMPurchaseWebsiteConfiguration
import com.ticketmaster.purchase.action.TMCheckoutEndReason
import com.ticketmaster.purchase.action.TMPurchaseMenuItem
import com.ticketmaster.purchase.action.TMPurchaseSubPage
import com.ticketmaster.purchase.action.TMTicketSelectionEndReason
import com.ticketmaster.purchase.entity.TMPurchaseOrder
import com.ticketmaster.purchase.entity.UALCommerceEvent
import com.ticketmaster.purchase.entity.UALPageView
import com.ticketmaster.purchase.entity.UALUserAction
import com.ticketmaster.purchase.exception.TmInvalidConfigurationException
import com.ticketmaster.purchase.listener.TMPurchaseFavoritesListener
import com.ticketmaster.purchase.listener.TMPurchaseNavigationListener
import com.ticketmaster.purchase.listener.TMPurchaseSharingListener
import com.ticketmaster.purchase.listener.TMPurchaseUserAnalyticsListener
import com.ticketmaster.purchase.listener.TMPurchaseWebAnalyticsListener
import kotlinx.coroutines.launch
import java.net.URL

class PurchaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_purchase)

        if (savedInstanceState == null) {

            val tmPurchase: TMPurchase =
                intent.extras?.parcelable(TMPurchase::class.java.name)
                    ?: throw TmInvalidConfigurationException()

            val tmPurchaseWebsiteConfiguration: TMPurchaseWebsiteConfiguration =
                intent.extras?.parcelable(TMPurchaseWebsiteConfiguration::class.java.name)
                    ?: throw TmInvalidConfigurationException()

            lifecycleScope.launch {
                /**
                 * Just like in PrePurchase, we make use of a fragment factory to enable you to implement
                 * a series of listener that fits your business needs and in return you get a Fragment
                 * to display. TMPurchaseFragmentFactory is the Purchase flow's fragment factory
                 */
                val factory = TMPurchaseFragmentFactory(
                    tmPurchaseNavigationListener = PurchaseNavigationListener { finish() },
                    tmPurchaseUserAnalyticsListener = UserAnalyticsListener { finish() },
                    tmPurchaseWebAnalyticsListener = WebAnalyticsListener(),
                    tmPurchaseShareListener = ShareListener(),
                    tmPurchaseFavoritesListener = PurchaseFavoriteListener()
                ).apply {
                    supportFragmentManager.fragmentFactory = this
                }

                val tmAuthenticationParams = setupTMAuthenticationParams(
                    tmPurchase, tmPurchaseWebsiteConfiguration
                )

                val bundle = tmPurchase.getPurchaseBundle(
                    tmPurchaseWebsiteConfiguration,
                    tmAuthenticationParams
                )

                val fragment = factory.instantiatePurchase(classLoader).apply {
                    arguments = bundle
                }

                supportFragmentManager.beginTransaction()
                    .add(android.R.id.content, fragment)
                    .commit()
            }
        }
    }

    private fun setupTMAuthenticationParams(
        tmPurchase: TMPurchase,
        tmPurchaseWebsiteConfiguration: TMPurchaseWebsiteConfiguration
    ): TMAuthenticationParams {
        val region = when (tmPurchaseWebsiteConfiguration.hostType) {
            TMMarketDomain.UK, TMMarketDomain.IE -> TMXDeploymentRegion.UK
            else -> TMXDeploymentRegion.US
        }
        return TMAuthenticationParams(
            apiKey = tmPurchase.apiKey,
            clientName = "Ticketmaster",
            region = region,
            environment = TMXDeploymentEnvironment.Production,
            quickLogin = false,
            autoQuickLogin = false
        )
    }
}

/**
 * This listener will notify you the user navigation
 */
class PurchaseNavigationListener(private val closeScreen: () -> Unit) :
    TMPurchaseNavigationListener {

    /**
     * This method is called when the Purchase event details page could not be loaded due to an error
     * @param error The exception that occurred while loading the event details page
     */
    override fun errorOnEventDetailsPage(error: Exception) {}


    /**
     * This method will be called when the user has completed or aborted purchase
     */
    override fun onPurchaseClosed() {
        closeScreen.invoke()
    }
}

/**
 * This listener can be used to customize the message included in the social sharing feature by
 * providing a new message to replace the default one used by the Marketplace SDK.
 * Default Message:  eventTitle + " " + eventDate
 */
class ShareListener : TMPurchaseSharingListener {

    /**
     * Set a customized sharing message based on the Event being shared.
     * @param event The Event being shared
     * @return returns a customized sharing message
     */
    override fun getShareTextForEvent(event: DiscoveryEvent): String = "Share the event!"
}

/**
 * This listener notifies you about the user actions
 */
class UserAnalyticsListener(private val closeScreen: () -> Unit) : TMPurchaseUserAnalyticsListener {

    /**
     * This method is called when the user opens ticket selection screen
     * @param event The event for which the ticket selection screen is opened
     */
    override fun onTicketSelectionStarted(event: DiscoveryEvent) {}

    /**
     * This method is called when the user has finished the ticket selection process
     * @param event The event for which the ticket selection screen is complete
     * @param reason The reason ticket selection ended
     */
    override fun onTicketSelectionFinished(
        event: DiscoveryEvent,
        reason: TMTicketSelectionEndReason
    ) {
    }

    /**
     * This method is called when the checkout process has started
     * @param event The event for which the checkout process has started
     */
    override fun onCheckoutStarted(event: DiscoveryEvent) {}

    /**
     * This method is called when the checkout process is finished
     * @param event The event for which the checkout process has finished
     * @param reason The reason checkout process finished
     */
    override fun onCheckoutFinished(event: DiscoveryEvent, reason: TMCheckoutEndReason) {
        closeScreen.invoke()
    }

    /**
     * This method is called when the user made a purchase and is currently viewing the Order Confirmation page.
     * @param event The event which the user purchased
     * @param order The order for the purchase
     */
    override fun onTicketPurchased(event: DiscoveryEvent, order: TMPurchaseOrder) {}

    /**
     * This method is called when the user selects a menu item
     * @param event The event on which the user selected the menu item
     * @param menuItemSelected The menu item that was selected
     */
    override fun onMenuItemSelected(event: DiscoveryEvent, menuItemSelected: TMPurchaseMenuItem) {}

    /**
     * This method is called when the user navigated to a sub-page with the EDP or Cart
     * @param event The event on which the user is opening a subpage
     * @param subPage which sub-page with the EDP or Cart page has been viewed
     */
    override fun onSubPageOpened(event: DiscoveryEvent, subPage: TMPurchaseSubPage) {}

    /**
     * This method will be called when the user has clicked "Manage my Tickets" and will
     * be redirected to the My Events page
     * @param event The event the user has purchased
     */
    override fun onManageMyTicketsOpened(event: DiscoveryEvent) {}
}

/**
 * This listener informs you with the server interaction
 */
class WebAnalyticsListener : TMPurchaseWebAnalyticsListener {
    /**
     * This method is called when an error occurs while loading the page
     * @param url The url at which the error occurred
     * @param error The exception
     */
    override fun errorOnPageLoad(url: URL, error: Exception) {}

    /**
     * This method is called when an error occurs on a webpage
     * @param url The url where the error occurred
     * @param error The exception
     */
    override fun errorOnWebpage(url: URL, error: Exception) {}

    /**
     * This method is called when a user visits a webpage
     * @param pageView Data object that holds page info
     */
    override fun onWebpageReportedUALPageView(pageView: UALPageView) {}

    /**
     * This method is called when a user performs an action, like a button click
     * @param action Data object that holds information about what the user clicked
     */
    override fun onWebpageReportedUALUserAction(action: UALUserAction) {}

    /**
     * This method is called when a user adds ticket to cart or purchases the ticket
     * @param commerceEvent Data object holding specific information about the ticket added
     * to the cart or purchased
     */
    override fun onWebpageReportedUALCommerceEvent(commerceEvent: UALCommerceEvent) {}
}

/**
 * This listener is used to back the functionality of the Favorites "Heart" button on the EDP action bar.
 * If implemented, these listener methods should be backed by some kind of data store (a persisted list)
 * that keeps track of a list of favorite events.
 * These methods will only be called if the Favorite Button has been enabled by the  `TMPurchaseWebsiteConfiguration`.
 */
class PurchaseFavoriteListener : TMPurchaseFavoritesListener {

    private var isInFavorite = true

    /**
     * This method is called to determine if the event should be marked as favorite in the toolbar
     * Note: This method is called when creating the options in the PurchaseActivity
     * @return true if already in favorites else return false
     */
    override fun isEventInFavorites(event: DiscoveryEvent, tmMemberInfo: MemberInfo?): Boolean {
        return isInFavorite
    }

    /**
     * Called when a user clicks on favorite icon to add current event to favorites.
     * @param event Details of the event which was added in favorites
     * @param tmMemberInfo Details about the user that has added this event to favorites
     * @param completion Lambda notifying you that the favorite action has successfully
     * propagate into Ticketmaster's backend
     */
    override fun onEventAddedInFavorites(
        event: DiscoveryEvent,
        tmMemberInfo: MemberInfo?,
        completion: (Boolean) -> Unit
    ) {
        isInFavorite = true
        completion.invoke(true)
    }

    /**
     * Called when a user clicks on favorite icon to remove current event from favorites.
     * @param event Details of the event which was removed from favorites
     * @param tmMemberInfo Details about the user that has removed this event from favorites
     * @param completion Lambda notifying you that the favorite action has successfully
     * propagate into Ticketmaster's backend
     */
    override fun onEventRemovedFromFavorites(
        event: DiscoveryEvent,
        tmMemberInfo: MemberInfo?,
        completion: (Boolean) -> Unit
    ) {
        isInFavorite = false
        completion.invoke(true)
    }
}

