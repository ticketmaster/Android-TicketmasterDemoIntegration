package com.example.ticketmasterretailandticketsdemo.utils

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.hideKeyboard() {
    val inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun Context.hideKeyboard(view: View) {
    view.hideKeyboard()
}

fun Activity.hideKeyboard() {
    // Calls Context.hideKeyboard
    hideKeyboard(currentFocus ?: View(this))
}