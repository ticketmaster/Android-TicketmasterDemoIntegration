package com.example.ticketmasterretailandticketsdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ticketmaster.authenticationsdk.TMAuthentication
import com.ticketmaster.authenticationsdk.TMXDeploymentEnvironment
import com.ticketmaster.authenticationsdk.TMXDeploymentRegion
import com.ticketmaster.discoveryapi.models.DiscoveryAbstractEntity
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
                intent.extras?.getParcelable(TMPurchase::class.java.name)
                    ?: throw TmInvalidConfigurationException()

            val tmPurchaseWebsiteConfiguration: TMPurchaseWebsiteConfiguration =
                intent.extras?.getParcelable(TMPurchaseWebsiteConfiguration::class.java.name)
                    ?: throw TmInvalidConfigurationException()

            // ExtraInfo exists to pass in data that TicketsSDK might need from RetailSDK, such
            // as the deployment region
            val extraInfo: ExtraInfo =
                intent.extras?.getParcelable(ExtraInfo::class.java.name)
                    ?: throw TmInvalidConfigurationException()

            lifecycleScope.launch {
                val factory = TMPurchaseFragmentFactory(
                    tmPurchaseNavigationListener = PurchaseNavigationListener { finish() }
                ).apply {
                    supportFragmentManager.fragmentFactory = this
                }

                val tmAuthentication = setupTMAuthentication(
                    tmPurchase,
                    getRegion(extraInfo.region)
                )

                val tmAuthenticationParam = TMAuthenticationParams(
                    apiKey = tmPurchase.apiKey,
                    clientName = "Ticketmaster Demo",
                    region = getRegion(extraInfo.region)
                )

                val bundle = tmPurchase.getPurchaseBundle(
                    tmPurchaseWebsiteConfiguration,
                    tmAuthenticationParam
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

    private fun getRegion(region: String): TMXDeploymentRegion {
        return when (region) {
            "UK" -> TMXDeploymentRegion.UK
            else -> TMXDeploymentRegion.US
        }
    }

    private suspend fun setupTMAuthentication(
        tmPurchase: TMPurchase,
        region: TMXDeploymentRegion
    ): TMAuthentication {

        return TMAuthentication
            .Builder()
            .apiKey(tmPurchase.apiKey)
            .clientName("Ticketmaster Demo")
            .region(region)
            .build(this)
    }
}

class PurchaseNavigationListener(private val closeScreen: () -> Unit) :
    TMPurchaseNavigationListener {
    override fun errorOnEventDetailsPage(error: Exception) {}

    override fun onPurchaseClosed() {
        closeScreen.invoke()
    }
}
