package com.example.ticketmasterretailandticketsdemo.ui.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

data class ConfigItems(
    val apiKey: String,
    val region: String,
    val market: String
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConfigurationScreen(
    gotoTicketsSDK: (ConfigItems) -> Unit
) {

    val retailMarkets = listOf("US", "CA")
    var retailMarketDropDownExpanded by remember { mutableStateOf(false) }

    val deploymentRegions = listOf("US", "UK")
    var deploymentRegionsDropDownExpanded by remember { mutableStateOf(false) }

    var apiKey by remember { mutableStateOf("") }
    var retailMarketSelected by remember { mutableStateOf("") }
    var deploymentRegionSelected by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(12.dp)
    ) {

        item {
            Text(
                text = "Developer API Key:",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            TextField(
                label = { Text("API Key") },
                value = apiKey,
                onValueChange = { apiKey = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Retail Market Domain",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = retailMarketDropDownExpanded,
                onExpandedChange = {
                    retailMarketDropDownExpanded = !retailMarketDropDownExpanded
                }
            ) {
                TextField(
                    value = retailMarketSelected,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = retailMarketDropDownExpanded)
                    }
                )

                ExposedDropdownMenu(
                    expanded = retailMarketDropDownExpanded,
                    onDismissRequest = { retailMarketDropDownExpanded = false }) {
                    retailMarkets.forEach { market ->
                        DropdownMenuItem(onClick = {
                            retailMarketDropDownExpanded = false
                            retailMarketSelected = market
                        }) {
                            Text(market)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "TMX Deployment Region",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = deploymentRegionsDropDownExpanded,
                onExpandedChange = {
                    deploymentRegionsDropDownExpanded = !deploymentRegionsDropDownExpanded
                }
            ) {
                TextField(
                    value = deploymentRegionSelected,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = deploymentRegionsDropDownExpanded)
                    }
                )

                ExposedDropdownMenu(
                    expanded = deploymentRegionsDropDownExpanded,
                    onDismissRequest = { deploymentRegionsDropDownExpanded = false }) {
                    deploymentRegions.forEach { region ->
                        DropdownMenuItem(onClick = {
                            deploymentRegionsDropDownExpanded = false
                            deploymentRegionSelected = region
                        }) {
                            Text(region)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = apiKey.isNotEmpty()
                        && deploymentRegionSelected.isNotEmpty()
                        && retailMarketSelected.isNotEmpty(),
                onClick = {
                    val configItems = ConfigItems(
                        apiKey = apiKey,
                        region = deploymentRegionSelected,
                        market = retailMarketSelected
                    )
                    gotoTicketsSDK(configItems)
                }
            ) {
                Text(text = "Launch Tickets SDK")
            }
        }
    }
}
