package com.example.hourblocker

import android.app.Activity
import android.Manifest
import android.app.role.RoleManager
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = TextView(this).apply { textSize = 18f; gravity = Gravity.CENTER }
        val button = Button(this).apply {
            text = "Enable call blocking"
            setOnClickListener { requestRole() }
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            addView(status); addView(button)
        })
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun rm() = getSystemService(RoleManager::class.java)

    private fun refresh() {
        val on = rm().isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        val contacts = checkSelfPermission(Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
        status.text = when {
            !on -> "Not active yet"
            !contacts -> "Active, but Favorites can't be checked: contacts permission missing"
            else -> "Active: repeat callers are blocked for 1 hour (Favorites are never blocked)"
        }
    }

    private fun requestRole() {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 2)
        if (!rm().isRoleHeld(RoleManager.ROLE_CALL_SCREENING))
            startActivityForResult(rm().createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), 1)
    }

    override fun onRequestPermissionsResult(r: Int, p: Array<out String>, g: IntArray) {
        super.onRequestPermissionsResult(r, p, g); refresh()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(r: Int, c: Int, d: Intent?) { super.onActivityResult(r, c, d); refresh() }
}
