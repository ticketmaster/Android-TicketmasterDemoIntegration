package com.example.ticketmasterretailandticketsdemo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ticketmasterretailandticketsdemo.ui.screen.ConfigItems
import com.example.ticketmasterretailandticketsdemo.ui.screen.ConfigurationScreen
import com.example.ticketmasterretailandticketsdemo.ui.screen.TicketSDKScreen
import com.example.ticketmasterretailandticketsdemo.ui.theme.TicketMasterRetailAndTicketsDemoTheme
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TicketMasterRetailAndTicketsDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ShowRetailAndTicketsDemo()
                }
            }
        }
    }
}

@Composable
fun ShowRetailAndTicketsDemo() {
    val navController = rememberNavController()

    val moshi = Moshi
        .Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val configJsonAdapter = moshi.adapter(ConfigItems::class.java).lenient()

    val ticketsSDKRoute = "ticketsSDK/config={config}"

    NavHost(navController = navController, startDestination = "configurationScreen") {

        composable("configurationScreen") {
            ConfigurationScreen(gotoTicketsSDK = { configItems ->
                val configJson = configJsonAdapter.toJson(configItems)
                navController.navigate(
                    ticketsSDKRoute.replace("{config}", configJson)
                )
            })
        }

        composable(ticketsSDKRoute) { backstackEntry ->
            val configJson = backstackEntry.arguments?.getString("config")
            val config = configJsonAdapter.fromJson(configJson)

            if (config != null) {
                TicketSDKScreen(configItems = config)
            }
        }
    }
}