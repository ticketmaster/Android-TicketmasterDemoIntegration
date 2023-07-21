package com.example.ticketmasterretailandticketsdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.ticketmaster.discoveryapi.enums.TMMarketDomain
import com.ticketmaster.discoveryapi.models.DiscoveryAbstractEntity
import com.ticketmaster.discoveryapi.models.DiscoveryEvent
import com.ticketmaster.prepurchase.TMPrePurchase
import com.ticketmaster.prepurchase.TMPrePurchaseFragmentFactory
import com.ticketmaster.prepurchase.TMPrePurchaseWebsiteConfiguration
import com.ticketmaster.prepurchase.data.CoordinatesWithMarketDomain
import com.ticketmaster.prepurchase.data.Location
import com.ticketmaster.prepurchase.listener.TMPrePurchaseNavigationListener
import com.ticketmaster.purchase.TMPurchase
import com.ticketmaster.purchase.TMPurchaseWebsiteConfiguration
import kotlinx.parcelize.Parcelize

val regions = listOf("US", "UK")
val markets = TMMarketDomain.values().toList()

@Parcelize
data class ExtraInfo(
    val region: String
) : Parcelable

class MainActivity : AppCompatActivity() {

    private val mainActivityContainer: ConstraintLayout by lazy {
        findViewById(R.id.main_activity_container)
    }

    private val apiKeyInput: TextInputLayout by lazy {
        mainActivityContainer.findViewById(R.id.api_key_input)
    }

    private val regionTextInput: AutoCompleteTextView by lazy {
        mainActivityContainer.findViewById(R.id.region_autocomplete_textview)
    }

    private val marketTextInput: AutoCompleteTextView by lazy {
        mainActivityContainer.findViewById(R.id.market_autocomplete_textview)
    }

    private val launchButton: Button by lazy {
        mainActivityContainer.findViewById(R.id.launchButton)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_main)

        mainActivityContainer.visibility = View.VISIBLE

        val deploymentRegions = ArrayAdapter(this, R.layout.list_item, regions)
        val regionTextInput: AutoCompleteTextView =
            mainActivityContainer.findViewById(R.id.region_autocomplete_textview)
        regionTextInput.setAdapter(deploymentRegions)

        val marketAdapter = ArrayAdapter(this, R.layout.list_item, markets)
        val marketTextInput: AutoCompleteTextView =
            mainActivityContainer.findViewById(R.id.market_autocomplete_textview)
        marketTextInput.setAdapter(marketAdapter)

        setupLaunchListener()
    }

    private fun setupLaunchListener() {
        launchButton.setOnClickListener {
            val apiKey = apiKeyInput.editText?.text.toString()
            val region = regionTextInput.text.toString()
            val market = marketTextInput.text.toString()

            val tmPrePurchase = TMPrePurchase(
                discoveryAPIKey = apiKey,
                brandColor = ContextCompat.getColor(
                    this@MainActivity,
                    R.color.black
                )
            )

            val tMPrePurchaseWebsiteConfiguration = TMPrePurchaseWebsiteConfiguration(
                hostType = TMMarketDomain.valueOf(market)
            )

            val bundle = tmPrePurchase.getPrePurchaseBundle(
                tMPrePurchaseWebsiteConfiguration
            )

            val factory = TMPrePurchaseFragmentFactory(
                tmPrePurchaseNavigationListener = PrePurchaseNavigationListener(
                    context = this,
                    apiKey = tmPrePurchase.discoveryAPIKey.orEmpty(),
                    region = region
                ) { finish() }
            )

            supportFragmentManager.fragmentFactory = factory

            val fragment = factory.instantiatePrePurchase(classLoader).apply {
                arguments = bundle
            }

            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, fragment)
                .commit()
        }
    }

    class PrePurchaseNavigationListener(
        private val context: Context,
        private val apiKey: String,
        private val region: String,
        private val completion: () -> Unit
    ) :
        TMPrePurchaseNavigationListener {
        // openEventDetailsPage is the bridge between PrePurchase and Purchase.
        // It must be implemented
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

        override fun onPrePurchaseClosed() {
            completion.invoke()
        }

        override fun onDidRequestCurrentLocation(
            globalMarketDomain: TMMarketDomain?,
            completion: ((CoordinatesWithMarketDomain) -> Unit)?
        ) {
            val coordinates = CoordinatesWithMarketDomain(
                latitude = 37.4139,
                longitude = -122.0851,
                marketDomain = globalMarketDomain
            )
            completion?.invoke(coordinates)
        }

        override fun onDidUpdateCurrentLocation(
            globalMarketDomain: TMMarketDomain?,
            location: Location
        ) {
        }
    }
}
