package com.example.ticketmasterretailandticketsdemo.ui.screen

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.example.ticketmasterretailandticketsdemo.ui.util.FragmentContainer
import com.example.ticketmasterretailandticketsdemo.ui.util.composeBridge
import com.example.ticketmasterretailandticketsdemo.util.getDeploymentRegion
import com.ticketmaster.authenticationsdk.AuthSource
import com.ticketmaster.authenticationsdk.TMAuthentication
import com.ticketmaster.tickets.ticketssdk.TicketsSDKClient
import com.ticketmaster.tickets.ticketssdk.TicketsSDKSingleton
import kotlinx.coroutines.launch

@Composable
fun TicketSDKScreen(
    configItems: ConfigItems
) {
    val context = LocalContext.current
    val activity = context as? AppCompatActivity ?: return

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                composeBridge {
                    LaunchTicketsView(activity = activity)
                }
            }
        }

    setupTicketSDKClient(activity, configItems, launcher)
}

fun setupTicketSDKClient(
    activity: AppCompatActivity,
    configItems: ConfigItems,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    activity.lifecycleScope.launch {
        val tmAuthentication = TMAuthentication
            .Builder()
            .apiKey(configItems.apiKey)
            .region(getDeploymentRegion(configItems.region))
            .clientName("Ticketmaster Demo")
            .build(activity)

        tmAuthentication.configuration?.let {
            val tokenMap = validateAuthToken(tmAuthentication)

            TicketsSDKClient
                .Builder()
                .authenticationSDKClient(tmAuthentication)
                .build(activity)
                .apply {
                    TicketsSDKSingleton.setTicketsSdkClient(this)

                    if (tokenMap.isNotEmpty()) {
                        composeBridge {
                            LaunchTicketsView(activity = activity)
                        }
                    } else {
                        launcher.launch(TicketsSDKSingleton.getLoginIntent(activity))
                    }
                }
        }
    }
}

@Composable
private fun LaunchTicketsView(activity: AppCompatActivity) {
    val fragment = TicketsSDKSingleton.getEventsFragment(activity)
    val fragmentManager = activity.supportFragmentManager

    fragment?.let {
        FragmentContainer(
            fragmentManager = fragmentManager,
            commit = { add(it, fragment) }
        )
    }
}

private suspend fun validateAuthToken(authentication: TMAuthentication): Map<AuthSource, String> {
    val tokenMap = mutableMapOf<AuthSource, String>()
    AuthSource.values().forEach {
        //Validate if there is an active token for the AuthSource, if not it returns null.
        authentication.getToken(it)?.let { token ->
            tokenMap[it] = token
        }
    }
    return tokenMap
}
