package com.example.ticketmasterretailandticketsdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ticketmasterretailandticketsdemo.utils.hideKeyboard
import com.google.android.material.textfield.TextInputEditText
import com.ticketmaster.authenticationsdk.TMXDeploymentEnvironment
import com.ticketmaster.authenticationsdk.TMXDeploymentRegion
import com.ticketmaster.discoveryapi.enums.TMMarketDomain
import com.ticketmaster.discoveryapi.models.DiscoveryAbstractEntity
import com.ticketmaster.discoveryapi.models.DiscoveryAttraction
import com.ticketmaster.discoveryapi.models.DiscoveryEvent
import com.ticketmaster.discoveryapi.models.DiscoveryVenue
import com.ticketmaster.foundation.entity.TMAuthenticationParams
import com.ticketmaster.prepurchase.TMPrePurchase
import com.ticketmaster.prepurchase.TMPrePurchaseCountryConfiguration
import com.ticketmaster.prepurchase.TMPrePurchaseFragmentFactory
import com.ticketmaster.prepurchase.TMPrePurchaseWebsiteConfiguration
import com.ticketmaster.prepurchase.action.TMPrePurchaseMenuItem
import com.ticketmaster.prepurchase.data.Location
import com.ticketmaster.prepurchase.discovery.entity.UALPageView
import com.ticketmaster.prepurchase.discovery.entity.UALUserAction
import com.ticketmaster.prepurchase.internal.UpdateLocationInfo
import com.ticketmaster.prepurchase.listener.TMPrePurchaseFavoritesListener
import com.ticketmaster.prepurchase.listener.TMPrePurchaseNavigationListener
import com.ticketmaster.prepurchase.listener.TMPrePurchaseSharingListener
import com.ticketmaster.prepurchase.listener.TMPrePurchaseUserAnalyticsListener
import com.ticketmaster.prepurchase.listener.TMPrePurchaseWebAnalyticsListener
import com.ticketmaster.purchase.TMPurchase
import com.ticketmaster.purchase.TMPurchaseWebsiteConfiguration
import kotlinx.parcelize.Parcelize
import java.net.URL


@Parcelize
data class ExtraInfo(
    val region: String
) : Parcelable

sealed class DiscoveryEntity {
    data object Arist : DiscoveryEntity()
    data object Venue : DiscoveryEntity()
}

class PrePurchaseActivity : AppCompatActivity() {

    companion object {
        enum class Regions(val item: String) { US("US"), UK("UK") }

        val markets = TMMarketDomain.entries
    }

    private val mainActivityContainer: ConstraintLayout by lazy {
        findViewById(R.id.main_activity_container)
    }

    private val apikeyEditText: TextInputEditText by lazy {
        mainActivityContainer.findViewById(R.id.api_key_edit_text)
    }

    private val clientNameEditText: TextInputEditText by lazy {
        mainActivityContainer.findViewById(R.id.client_name_edit_text)
    }

    private val artistOrVenueIdEditText: TextInputEditText by lazy {
        mainActivityContainer.findViewById(R.id.artist_venue_id_edit_text)
    }

    private val artistRadioButton: RadioButton by lazy {
        findViewById(R.id.radio_artist)
    }

    private val venueRadioButton: RadioButton by lazy {
        findViewById(R.id.radio_venue)
    }

    private val regionTextInput: AutoCompleteTextView by lazy {
        mainActivityContainer.findViewById(R.id.region_autocomplete_textview)
    }

    private val marketTextInput: AutoCompleteTextView by lazy {
        mainActivityContainer.findViewById(R.id.market_autocomplete_textview)
    }

    private val launchButton: Button by lazy {
        mainActivityContainer.findViewById(R.id.launch_button)
    }

    private val discoveryApiTextView: TextView by lazy {
        mainActivityContainer.findViewById(R.id.discovery_api_version)
    }

    private val prePurchaseApiTextView: TextView by lazy {
        mainActivityContainer.findViewById(R.id.prepurchase_api_version)
    }

    private val purchaseApiTextView: TextView by lazy {
        mainActivityContainer.findViewById(R.id.purchase_api_version)
    }

    private val ticketsApiTextView: TextView by lazy {
        mainActivityContainer.findViewById(R.id.tickets_api_version)
    }

    private lateinit var fragment: Fragment

    private var entityType: DiscoveryEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_main)

        mainActivityContainer.visibility = View.VISIBLE

        val deploymentRegions = ArrayAdapter(this, R.layout.list_item, Regions.entries)
        val regionTextInput: AutoCompleteTextView =
            mainActivityContainer.findViewById(R.id.region_autocomplete_textview)
        regionTextInput.setAdapter(deploymentRegions)

        val marketAdapter = ArrayAdapter(this, R.layout.list_item, markets)
        val marketTextInput: AutoCompleteTextView =
            mainActivityContainer.findViewById(R.id.market_autocomplete_textview)
        marketTextInput.setAdapter(marketAdapter)

        val discoveryVersion = resources.getString(R.string.discovery_api, "v3.3.0")
        discoveryApiTextView.text = discoveryVersion

        val purchaseVersion = resources.getString(R.string.purchase_api, "v3.2.6")
        purchaseApiTextView.text = purchaseVersion

        val prePurchaseVersion = resources.getString(R.string.prepurchase_api, "v3.2.6")
        prePurchaseApiTextView.text = prePurchaseVersion

        val ticketsVersion = resources.getString(R.string.tickets_api, "v3.10.0")
        ticketsApiTextView.text = ticketsVersion

        setupLaunchListener()
    }

    private fun setupLaunchListener() {
        apikeyEditText.addTextChangedListener(TextFieldValidation(apikeyEditText))
        regionTextInput.addTextChangedListener(TextFieldValidation(regionTextInput))
        marketTextInput.addTextChangedListener(TextFieldValidation(marketTextInput))
        clientNameEditText.addTextChangedListener(TextFieldValidation(clientNameEditText))
        artistOrVenueIdEditText.addTextChangedListener(TextFieldValidation(artistOrVenueIdEditText))

        venueRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                entityType = DiscoveryEntity.Venue
            }
        }

        artistRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                entityType = DiscoveryEntity.Arist
            }
        }

        launchButton.setOnClickListener {
            if (!isFormComplete()) {
                return@setOnClickListener
            }

            val apiKey = apikeyEditText.text.toString()
            val region = regionTextInput.text.toString()
            val market = marketTextInput.text.toString()
            val clientName = clientNameEditText.text.toString()
            val artistOrVenueId = artistOrVenueIdEditText.text.toString()

            val abstractEntity: DiscoveryAbstractEntity = if (entityType is DiscoveryEntity.Arist) {
                DiscoveryAttraction(hostID = artistOrVenueId)
            } else {
                DiscoveryVenue(hostID = artistOrVenueId)
            }

            val tmPrePurchaseWebsiteConfiguration = TMPrePurchaseWebsiteConfiguration(
                hostType = TMMarketDomain.valueOf(market),
                abstractEntity = abstractEntity
            )

            val countryConfiguration = TMPrePurchaseCountryConfiguration(
                isCountrySelectorEnabled = true,
                countrySelectorMap = getCountryMap()
            )

            val tmPrePurchase = TMPrePurchase(
                discoveryAPIKey = apiKey,
                brandColor = ContextCompat.getColor(
                    this@PrePurchaseActivity,
                    R.color.black
                )
            )

            val tmAuthenticationParams = setupTMAuthenticationParams(
                tmPrePurchase, tmPrePurchaseWebsiteConfiguration, clientName
            )

            val bundle: Bundle = tmPrePurchase.getPrePurchaseBundle(
                tmPrePurchaseWebsiteConfiguration,
                tmAuthenticationParams,
                countryConfiguration
            )

            /**
             * TMPrePurchaseFragmentFactory is the fragment factory that must be implemented
             * since it gives you a series of listeners based on your business needs and in
             * return you will get a Fragment that you can show to the end user.
             */
            val factory = TMPrePurchaseFragmentFactory(
                tmPrePurchaseNavigationListener = PrePurchaseNavigationListener(
                    context = this,
                    apiKey = tmPrePurchase.discoveryAPIKey,
                    region = region
                ) {
                    if (::fragment.isInitialized) {
                        supportFragmentManager.beginTransaction().remove(fragment).commit()
                    } else {
                        finish()
                    }
                },
                tmPrePurchaseFavoritesListener = PrePurchaseFavoriteListener(),
                tmPrePurchaseShareListener = ShareListener(),
                tmPrePurchaseUserAnalyticsListener = UserAnalyticsListener(),
                tmPrePurchaseWebAnalyticsListener = WebAnalyticsListener()
            )

            supportFragmentManager.fragmentFactory = factory

            fragment = factory.instantiatePrePurchase(classLoader).apply {
                arguments = bundle
            }

            hideKeyboard()

            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, fragment)
                .commit()
        }
    }

    private fun getCountryMap(): Map<TMMarketDomain, Boolean> {
        val map = mutableMapOf<TMMarketDomain, Boolean>()
        map[TMMarketDomain.US] = true
        map[TMMarketDomain.CA] = true
        map[TMMarketDomain.UK] = true
        map[TMMarketDomain.IE] = true
        map[TMMarketDomain.AU] = true
        map[TMMarketDomain.NZ] = true
        map[TMMarketDomain.MX] = true
        return map
    }

    private fun setupTMAuthenticationParams(
        tmPrePurchase: TMPrePurchase,
        tmPrePurchaseWebsiteConfiguration: TMPrePurchaseWebsiteConfiguration,
        clientName: String
    ): TMAuthenticationParams {
        val region = when (tmPrePurchaseWebsiteConfiguration.hostType) {
            TMMarketDomain.UK, TMMarketDomain.IE -> TMXDeploymentRegion.UK
            else -> TMXDeploymentRegion.US
        }
        return TMAuthenticationParams(
            apiKey = tmPrePurchase.discoveryAPIKey,
            clientName = clientName,
            region = region,
            environment = TMXDeploymentEnvironment.Production
        )
    }

    private fun isFormComplete(): Boolean {
        return validateAPIKey()
                && validateMarket()
                && validateRegion()
                && validateClientName()
                && validateAristOrVenueId()
                && entityType != null
    }

    private fun validateAPIKey(): Boolean {
        if (apikeyEditText.text.toString().trim().isBlank()) {
            apikeyEditText.error = "Required Field"
            apikeyEditText.requestFocus()
            return false
        } else {
            apikeyEditText.error = null
        }
        return true
    }

    private fun validateRegion(): Boolean {
        if (regionTextInput.text.toString().trim().isBlank()) {
            regionTextInput.error = "Required Field"
            regionTextInput.requestFocus()
            return false
        } else {
            regionTextInput.error = null
        }
        return true
    }

    private fun validateMarket(): Boolean {
        if (marketTextInput.text.toString().trim().isBlank()) {
            marketTextInput.error = "Required Field"
            marketTextInput.requestFocus()
            return false
        } else {
            marketTextInput.error = null
        }
        return true
    }

    private fun validateClientName(): Boolean {
        if (clientNameEditText.text.toString().trim().isBlank()) {
            clientNameEditText.error = "Required Field"
            clientNameEditText.requestFocus()
            return false
        } else {
            clientNameEditText.error = null
        }
        return true
    }

    private fun validateAristOrVenueId(): Boolean {
        if (artistOrVenueIdEditText.text.toString().trim().isBlank()) {
            artistOrVenueIdEditText.error = "Required Field"
            artistOrVenueIdEditText.requestFocus()
            return false
        } else {
            artistOrVenueIdEditText.error = null
        }
        return true
    }

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
                R.id.api_key_edit_text -> {
                    validateAPIKey()
                }

                R.id.client_name_edit_text -> {
                    validateClientName()
                }

                R.id.artist_venue_id_edit_text -> {
                    validateAristOrVenueId()
                }

                R.id.region_autocomplete_textview -> {
                    validateRegion()
                }

                R.id.market_autocomplete_textview -> {
                    validateMarket()
                }
            }
        }
    }

    /**
     * This listener will notify you the user navigation
     */
    class PrePurchaseNavigationListener(
        private val context: Context,
        private val apiKey: String,
        private val region: String,
        private val closeScreen: () -> Unit
    ) :
        TMPrePurchaseNavigationListener {

        /**
         * User has selected an EventDetail from either ADP/VDP. This method is the brdige between
         * PrePurchase and Purchase.  It must be implemented where you supply the event info that
         * the user has selected
         * @param event is the EventDetail
         * @param abstractEntity is the Artist, Team or Venue for the corresponding event
         * @return true if you want the sdk to open the venue details page
         */
        override fun openEventDetailsPage(
            abstractEntity: DiscoveryAbstractEntity?,
            event: DiscoveryEvent
        ) {
            val tmPurchase = TMPurchase(
                apiKey = apiKey,
                brandColor = ContextCompat.getColor(
                    context,
                    R.color.black
                )
            )
            val tmPurchaseWeb = TMPurchaseWebsiteConfiguration(
                event.hostID.orEmpty(),
                TMMarketDomain.US,
            )
            val extraInfo = ExtraInfo(
                region
            )

            context.startActivity(
                Intent(
                    context, PurchaseActivity::class.java
                ).apply {
                    putExtra(TMPurchase::class.java.name, tmPurchase)
                    putExtra(
                        TMPurchaseWebsiteConfiguration::class.java.name,
                        tmPurchaseWeb
                    )
                    putExtra(ExtraInfo::class.java.name, extraInfo)
                }
            )
        }

        /**
         * Allow integrators to communicate with webview with custom location that may not be their
         * current location
         */
        override fun updateCurrentLocation(updateLocationInfo: (UpdateLocationInfo) -> Unit) {}

        /**
         * PrePurchase doesn't have anymore screens on the back stack.
         * Integrating app decides how to proceed
         */
        override fun onPrePurchaseClosed() {
            closeScreen.invoke()
        }

        /**
         * Must implement if requesting location from users' as well as
         * requesting they grant your application permission to their location
         */
        override fun onDidRequestCurrentLocation(
            globalMarketDomain: TMMarketDomain?,
            completion: (Location?) -> Unit
        ) {
        }

        /**
         *  Invoked whenever user clicked on the location toggle on the webview. Implement
         *  whatever location based logic in this callback.
         */
        override fun onDidRequestNativeLocationSelector() {}

        /**
         *  Must implement if requesting location from users' as well as
         *  requesting they grant your application permission to their location
         */
        override fun onDidUpdateCurrentLocation(
            globalMarketDomain: TMMarketDomain?,
            location: Location
        ) {
        }

        /**
         *  Depending on your app and navigation structure, this callback helps you capture
         *  when a user is seeking to go back. Depending on whether you're using a custom navigation
         *  manager or fragment manager or Jetpack Navigation, whatever method available to you to
         *  simulate going back will be used here.
         */
        override fun onPrePurchaseBackPressed() {}
    }

    /**
     * This listener informs you with the server interaction
     */
    class WebAnalyticsListener : TMPrePurchaseWebAnalyticsListener {

        /**
         * This method is called when a webpage is loading
         * @param url The url that is been loaded
         */
        override fun onLoadingPage(url: URL) {}

        /**
         * A warning that the page is taking so long to load, that the native loading was dismissed as a precaution.
         * @param url The url that is taking long to load
         * @param duration
         */
        override fun onPageLoadProgressBarTimeout(url: URL, duration: Long) {}

        /**
         * This method is called when an error occurs while loading the page
         * @param url The url at which the error occurred
         * @param error The exception
         */
        override fun errorOnPageLoad(url: URL, error: Exception) {}

        /**
         * This method is called when the page is loaded
         * @param url The url that has loaded
         * @param duration The time taken for loading the url
         */
        override fun onPageLoadComplete(url: URL, duration: Long) {}

        /**
         * This method is called when an error occurs on a webpage
         * @param url The url where the error occurred
         * @param error The exception
         */
        override fun errorOnWebpage(url: URL, error: Exception) {}

        /**
         * This method is called when a user visits a webpage
         * @param
         */
        override fun onWebpageReportedUALPageView(pageView: UALPageView) {}

        /**
         * This method is called when a user performs an action, like a button click
         * @param
         */
        override fun onWebpageReportedUALUserAction(action: UALUserAction) {}
    }

    /**
     * This listener can be used to customize the message included in the social sharing feature by
     * providing a new message to replace the default one used by the Marketplace SDK.
     * Default Message:  URL for shared Attraction/Venue
     */
    class ShareListener : TMPrePurchaseSharingListener {

        /**
         * Set a customized sharing message based on the Artist or Venue being shared.
         * @param abstractEntity The Artist or Venue being shared
         * @return returns a customized sharing message
         */
        override fun getShareTextForArtistOrVenue(abstractEntity: DiscoveryAbstractEntity): String {
            return when (abstractEntity) {
                is DiscoveryAttraction -> "Is an attraction"
                is DiscoveryVenue -> "Is a venue"
                else -> "Share"
            }
        }
    }

    /**
     * This listener is used to back the functionality of the Favorites "Heart" button on the EDP action bar.
     * If implemented, these listener methods should be backed by some kind of data store (a persisted list)
     * that keeps track of a list of favorite events.
     * These methods will only be called if the Favorite Button has been enabled by the  `TMPurchaseWebsiteConfiguration`.
     */
    class PrePurchaseFavoriteListener : TMPrePurchaseFavoritesListener {

        private var isInFavorite = false

        /**
         * This method is called to determine if the event should be marked as favorite in the toolbar_prepurchase
         * Note: This method is called when creating the options in the PurchaseActivity
         * @return true if already in favorites else return false
         */
        override fun isAbstractEntityInFavorites(
            event: DiscoveryAbstractEntity,
        ): Boolean {
            return isInFavorite
        }

        /**
         * Called when a user clicks on favorite icon to add current event to favorites.
         * @param event Details of the event which was added in favorites
         * @param didAdd Lambda that is invoked when user has clicked heart icon for event
         */
        override fun onAbstractEntityAddedInFavorites(
            event: DiscoveryAbstractEntity,
            didAdd: (Boolean) -> Unit
        ) {
            isInFavorite = true
            didAdd.invoke(true)
        }

        /**
         * Called when a user clicks on favorite icon to remove current event from favorites.
         * @param event Details of the event which was removed from favorites
         * @param didRemove Lambda that is invoked when user has unclicked heart icon for event
         *
         */
        override fun onAbstractEntityRemovedFromFavorites(
            event: DiscoveryAbstractEntity,
            didRemove: (Boolean) -> Unit
        ) {
            isInFavorite = false
            didRemove.invoke(true)
        }
    }


    /**
     * This listener notifies you about the user actions
     */
    class UserAnalyticsListener : TMPrePurchaseUserAnalyticsListener {

        /**
         * This method is called when the user select an event available on ADP/VDP
         * @param event The event for which the ticket selection screen is opened
         */
        override fun onEDPSelectionStarted(event: DiscoveryEvent) {}

        /**
         * Method called when user loads an unsupported
         * venue,attraction, or any unsupported url
         * not related to Ticketmaster like maps
         */
        override fun openURLNotSupported(url: String) {}

        /**
         * This method is called when the user selects a menu item
         * @param event The Artist/Venue on which the user selected the menu item
         * @param menuItemSelected The menu item that was selected
         */
        override fun onMenuItemSelected(
            event: DiscoveryAbstractEntity,
            menuItemSelected: TMPrePurchaseMenuItem
        ) {
        }
    }
}
