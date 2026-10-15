package com.example.hourblocker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService

/**
 * First call from a number is allowed and starts a 1-hour window.
 * Any call from the same number inside that window is rejected.
 */
class HourBlockService : CallScreeningService() {

    override fun onScreenCall(details: Call.Details) {
        if (details.callDirection != Call.Details.DIRECTION_INCOMING) return
        val number = details.handle?.schemeSpecificPart
        if (number.isNullOrBlank()) {            // private/unknown caller: let through
            respondToCall(details, CallResponse.Builder().build())
            return
        }

        if (isFavorite(number)) {                // starred contacts are never blocked
            respondToCall(details, CallResponse.Builder().build())
            return
        }

        val prefs = getSharedPreferences("blocks", Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val firstCall = prefs.getLong(number, 0L)

        val response = if (firstCall != 0L && now - firstCall < HOUR_MS) {
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipNotification(true)       // no missed-call notification
                .build()
        } else {
            prefs.edit().putLong(number, now).apply()   // start new 1-hour window
            CallResponse.Builder().build()              // allow this call
        }
        respondToCall(details, response)
    }

    private fun isFavorite(number: String): Boolean {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            return false
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.STARRED), null, null, null)
            ?.use { c ->
                while (c.moveToNext()) if (c.getInt(0) == 1) return true
            }
        return false
    }

    companion object { const val HOUR_MS = 60L * 60L * 1000L }
}
