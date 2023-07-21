package com.example.ticketmasterretailandticketsdemo.ui.util

import androidx.compose.runtime.Composable

// Function bridges composable to non-composable code
fun composeBridge(showContent: @Composable () -> Unit) {}