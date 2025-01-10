# Android-TicketmasterDemoIntegration
This is an example integration of the Ticketmaster RetailSDKs and TicketsSDKs.

* Overview: https://business.ticketmaster.com/ignite/
* RetailSDK Documentation: https://ignite.ticketmaster.com/docs/retail-sdk-overview
* TicketsSDK Documentation: https://ignite.ticketmaster.com/docs/tickets-sdk-overview


## Getting Started
1. Open `Android-TicketMasterRetailAndTicketsDemo` in Android Studio
   - This will also download all the required libraries
     
2. Get a developer API key: https://developer.ticketmaster.com/explore/
3. Build and Run


## Flow

The RetailSDK makes use of fragment factories whereby you supply the necessary configuration keys and listeners of your choice and in return a fragment that you can display is returned.

### PrePurchase

The first step is the `PrePurchase` flow. `PrePurchase` is where you can search and discover tickets based on what you desire. There are a variety of listeners that can be supplied to `TMPrePurchaseFragmentFactory`
to manage user interaction and show artist or venue details. One important listener is `TMPrePurchaseNavigationListener`.

When you select a ticket, the `openEventDetailsPage` callback of PrePurchase's navigation listener, `TMPrePurchaseNavigationListener` is invoked and you'll have access to the necessary data to head to the `Purchase` flow.

Look at this sample code to get `PrePurchase` working:

```
val attractionID = ....
val venueID = ...
val market = TMMarketDomain.valueOf([CLIENT_MARKET])
val apiKey = [YOUR_API_KEY]
val clientName = [YOUR_CLIENT_NAME]
val region = TMXDeploymentRegion.US

val abstractEntity: DiscoveryAbstractEntity = if (attractionID.isNotEmpty()) {
   DiscoveryAttraction(hostID = attractionID)
} else {
   DiscoveryVenue(hostID = venueID)
}

val environment = TMEnvironment.Production

val tmPrePurchase = TMPrePurchase(
   environment,
   ContextCompat.getColor(
      this,
      R.color.black
   ),
   apiKey
)

val tmPrePurchaseWebsiteConfiguration = TMPrePurchaseWebsiteConfiguration(
   abstractEntity,
   market
)

// Add whatever listener fits your business needs
val tmPurchaseFragmentFactory = TMPrePurchaseFragmentFactory(
   tmPrePurchaseNavigationListener = TMPrePurchaseNavigationListener(..),
   tmPrePurchaseShareListener = TMPrePurchaseSharingListener(..),
   tmPrePurchaseUserAnalyticsListener = TMPrePurchaseUserAnalyticsListener(..),
   tmPrePurchaseWebAnalyticsListener = TMPrePurchaseWebAnalyticsListener(..),
   tmPrePurchaseFavoritesListener = TMPrePurchaseFavoritesListener(..)
)

val tmAuthenticationParams = TMAuthenticationParams(
   apiKey = apiKey,
   clientName = clientName,
   region = region,
   environment = environment
)

val countryConfiguration = TMPrePurchaseCountryConfiguration(
   isCountrySelectorEnabled = true,
   countrySelectorMap = mapOf(TMMarketDomain.US, true)
)

val bundle: Bundle = tmPrePurchaseWebsiteConfiguration.let {
   tmPrePurchase.getPrePurchaseBundle(
      it,
      tmAuthenticationParams,
      countryConfiguration
   )
}

// Use fragment to display to user
val fragment = tmPurchaseFragmentFactory.instantiatePrePurchase(classLoader).apply {
   arguments = bundle
}
```


### Purchase

`Purchase` consists of ticket selections and checkout. When a user has selected on an event that is associated with an artist or venue, they are then taken to an event details page where they can select their seats, browse the different ticket prices and proceeding to buy tickets.

Look at this sample code to get `Purchase` working:

```
val apiKey = [YOUR_API_KEY]
val clientName = [YOUR_CLIENT_NAME]
val environment = TMEnvironment.Production
val tmHost = TMMarketDomain.US
val eventId = [EVENT_ID]
val region = TMXDeploymentRegion.US

val factory = TMPurchaseFragmentFactory(
   tmPurchaseNavigationListener = TMPurchaseNavigationListener(..),
   tmPurchaseShareListener = TMPurchaseSharingListener(..),
   tmPurchaseUserAnalyticsListener = TMPurchaseUserAnalyticsListener(..),
   tmPurchaseWebAnalyticsListener = TMPurchaseWebAnalyticsListener(..),
   tmPurchaseFavoritesListener = TMPurchaseFavoritesListener(..)
)

val tmPurchase = TMPurchase(
   apiKey = apiKey,
   environment = environment,
   ContextCompat.getColor(
      this,
      R.color.black
   )
)

val tmPurchaseWebsiteConfiguration = TMPurchaseWebsiteConfiguration(
   eventId,
   tmHost
)

val tmAuthenticationParams = TMAuthenticationParams(
   apiKey = apiKey,
   clientName = clientName,
   region = region,
   environment = environment
)


val bundle = tmPurchase.getPurchaseBundle(
   tmPurchaseWebsiteConfiguration,
   tmAuthenticationParams
)

// Use fragment to display to user
val fragment = factory.instantiatePurchase(classLoader).apply {
   arguments = bundle
}
```


### Scenarios:

1. PrePurchase  ->               if (logged in)                  -> Purchase
2. PrePurchase  ->  if (not logged in) -> Tickets Authentication -> Purchase

## Demo App Screenshots

<img src="https://github.com/user-attachments/assets/a74d5a4d-b165-4df8-9b75-46dcd492f1cc" width="200" height="400" /> <img src="https://github.com/user-attachments/assets/e029b96b-91d6-4a28-aef5-43bdf02f52a1" width="200" height="400" /> <img src="https://github.com/user-attachments/assets/f54cc760-81d9-4426-9bcf-40ca1373dd80" width="200" height="400" /> <img src="Screenshot_20230721_091626.png" width="200" height="400" /> 

