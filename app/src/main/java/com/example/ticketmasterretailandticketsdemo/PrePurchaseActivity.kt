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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ticketmasterretailandticketsdemo.utils.hideKeyboard
import com.google.android.material.textfield.TextInputEditText
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

class PrePurchaseActivity : AppCompatActivity() {

    private val mainActivityContainer: ConstraintLayout by lazy {
        findViewById(R.id.main_activity_container)
    }

    private val apikeyEditText: TextInputEditText by lazy {
        mainActivityContainer.findViewById(R.id.api_key_edit_text)
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

        val discoveryVersion = resources.getString(R.string.discovery_api, "v1.0.2")
        discoveryApiTextView.text = discoveryVersion

        val purchaseVersion = resources.getString(R.string.purchase_api, "v1.0.6")
        purchaseApiTextView.text = purchaseVersion

        val prePurchaseVersion = resources.getString(R.string.prepurchase_api, "v1.0.5")
        prePurchaseApiTextView.text = prePurchaseVersion

        val ticketsVersion = resources.getString(R.string.tickets_api, "v3.0.2")
        ticketsApiTextView.text = ticketsVersion

        setupLaunchListener()
    }

    private fun setupLaunchListener() {
        apikeyEditText.addTextChangedListener(TextFieldValidation(apikeyEditText))
        regionTextInput.addTextChangedListener(TextFieldValidation(regionTextInput))
        marketTextInput.addTextChangedListener(TextFieldValidation(marketTextInput))

        launchButton.setOnClickListener {
            if (!isFormComplete()) {
                return@setOnClickListener
            }

            val apiKey = apikeyEditText.text.toString()
            val region = regionTextInput.text.toString()
            val market = marketTextInput.text.toString()

            val tmPrePurchase = TMPrePurchase(
                discoveryAPIKey = apiKey,
                brandColor = ContextCompat.getColor(
                    this@PrePurchaseActivity,
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
                ) {
                    if (::fragment.isInitialized) {
                        supportFragmentManager.beginTransaction().remove(fragment).commit()
                    } else {
                        finish()
                    }
                }
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

    private fun isFormComplete(): Boolean {
        return validateAPIKey() && validateMarket() && validateRegion()
    }

    private fun validateAPIKey(): Boolean {
        if (apikeyEditText.text.toString().trim().isEmpty()) {
            apikeyEditText.error = "Required Field"
            apikeyEditText.requestFocus()
            return false
        } else {
            apikeyEditText.error = null
        }
        return true
    }

    private fun validateRegion(): Boolean {
        if (regionTextInput.text.toString().trim().isEmpty()) {
            regionTextInput.error = "Required Field"
            regionTextInput.requestFocus()
            return false
        } else {
            regionTextInput.error = null
        }
        return true
    }

    private fun validateMarket(): Boolean {
        if (marketTextInput.text.toString().trim().isEmpty()) {
            marketTextInput.error = "Required Field"
            marketTextInput.requestFocus()
            return false
        } else {
            marketTextInput.error = null
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
                R.id.region_autocomplete_textview -> {
                    validateRegion()
                }
                R.id.market_autocomplete_textview -> {
                    validateMarket()
                }
            }
        }
    }

    class PrePurchaseNavigationListener(
        private val context: Context,
        private val apiKey: String,
        private val region: String,
        private val closeScreen: () -> Unit
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
            closeScreen.invoke()
        }

        override fun onDidRequestCurrentLocation(
            globalMarketDomain: TMMarketDomain?,
            completion: (CoordinatesWithMarketDomain?) -> Unit
        ) {
            // MUST implement if requesting location from users' as well as
            // requesting they grant your application permission to their location
        }

        override fun onDidUpdateCurrentLocation(
            globalMarketDomain: TMMarketDomain?,
            location: Location
        ) {}
    }
}
