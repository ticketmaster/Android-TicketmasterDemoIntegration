package com.example.ticketmasterretailandticketsdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ticketmasterretailandticketsdemo.extension.parcelable
import com.ticketmaster.authenticationsdk.TMXDeploymentEnvironment
import com.ticketmaster.authenticationsdk.TMXDeploymentRegion
import com.ticketmaster.foundation.entity.TMAuthenticationParams
import com.ticketmaster.purchase.TMPurchase
import com.ticketmaster.purchase.TMPurchaseFragmentFactory
import com.ticketmaster.purchase.TMPurchaseWebsiteConfiguration
import com.ticketmaster.purchase.exception.TmInvalidConfigurationException
import com.ticketmaster.purchase.listener.TMPurchaseNavigationListener
import kotlinx.coroutines.launch

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

            // ExtraInfo exists to pass in data that TicketsSDK might need from RetailSDK, such
            // as the deployment region
            val extraInfo: ExtraInfo =
                intent.extras?.parcelable(ExtraInfo::class.java.name)
                    ?: throw TmInvalidConfigurationException()

            val factory = TMPurchaseFragmentFactory(
                tmPurchaseNavigationListener = PurchaseNavigationListener { finish() }
            ).apply {
                supportFragmentManager.fragmentFactory = this
            }

            val bundle = tmPurchase.getPurchaseBundle(
                tmPurchaseWebsiteConfiguration,
                setupTMAuthParams(
                    tmPurchase,
                    getRegion(extraInfo.region)
                )
            )

            val fragment = factory.instantiatePurchase(classLoader).apply {
                arguments = bundle
            }

            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, fragment)
                .commit()
        }
    }

    private fun getRegion(region: String): TMXDeploymentRegion {
        return when (region) {
            "UK" -> TMXDeploymentRegion.UK
            else -> TMXDeploymentRegion.US
        }
    }

    private fun setupTMAuthParams(
        tmPurchase: TMPurchase,
        region: TMXDeploymentRegion
    ): TMAuthenticationParams = TMAuthenticationParams(
        apiKey = tmPurchase.apiKey,
        clientName = "Ticketmaster Demo",
        environment = TMXDeploymentEnvironment.Production,
        region = region,
        quickLogin = false,
        autoQuickLogin = true
    )
}

class PurchaseNavigationListener(private val closeScreen: () -> Unit) :
    TMPurchaseNavigationListener {
    override fun errorOnEventDetailsPage(error: Exception) {}

    override fun onPurchaseClosed() {
        closeScreen.invoke()
    }
}
