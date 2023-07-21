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
The first flow you'll encounter after entering in your config items will be `PrePurchase`. `PrePurchase` is where you can search and discover tickets based on what you desire.
When you select a ticket, `openEventDetailsPage` of the PrePurchase navigation listener is where you'll then head to the `Purchase` section. Purchase consists of ticket selections
and checkout.

PrePurchase  ->               if (logged in)                  -> Purchase
             ->  if (not logged in) -> Tickets Authentication -> Purchase

## Demo App Screenshots

<img src="screenshots/sample_integration_app_1.jpg" alt="Getting Started" /> <img src="screenshots/sample_integration_app_2.jpg" alt="Login" /> <img src="screenshots/sample_integration_app_4.jpg" alt="Tickets Listing Page" /> 
