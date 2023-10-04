package com.example.ticketmasterretailandticketsdemo.extension

import android.app.Activity
import android.view.View

fun Activity.hideKeyboard() {
    // Calls Context.hideKeyboard
    hideKeyboard(currentFocus ?: View(this))
}