package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object CommunicationHelper {

    fun openSmsComposer(
        context: Context,
        phoneNumber: String,
        messageBody: String,
        onStatusUpdate: (String) -> Unit = {}
    ) {
        val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
        try {
            val uri = Uri.parse("smsto:$cleanPhone")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageBody)
                putExtra(Intent.EXTRA_TEXT, messageBody)
            }
            context.startActivity(intent)
            onStatusUpdate("Opened in SMS Composer")
        } catch (e: Exception) {
            // Fallback to generic send intent
            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, messageBody)
                    putExtra("address", cleanPhone)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Send SMS via"))
                onStatusUpdate("Opened via Intent Chooser")
            } catch (ex: Exception) {
                Toast.makeText(context, "Cannot open SMS application: ${ex.message}", Toast.LENGTH_LONG).show()
                onStatusUpdate("Failed to Open")
            }
        }
    }

    fun openDialer(context: Context, phoneNumber: String) {
        val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanPhone")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open phone dialer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
