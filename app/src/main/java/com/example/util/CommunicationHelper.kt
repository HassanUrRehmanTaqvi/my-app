package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object CommunicationHelper {

    /**
     * 1. WhatsApp Direct Message (https://wa.me/...)
     * Formats Pakistani numbers (03... to 923...)
     */
    fun openWhatsApp(context: Context, phone: String, message: String = "") {
        try {
            var cleanPhone = phone.replace(Regex("[^0-9]"), "")
            if (cleanPhone.startsWith("0")) {
                cleanPhone = "92" + cleanPhone.substring(1)
            } else if (cleanPhone.startsWith("+")) {
                cleanPhone = cleanPhone.substring(1)
            }
            val encodedMsg = try {
                URLEncoder.encode(message, "UTF-8")
            } catch (e: Exception) {
                Uri.encode(message)
            }
            val url = if (encodedMsg.isNotBlank()) {
                "https://wa.me/$cleanPhone?text=$encodedMsg"
            } else {
                "https://wa.me/$cleanPhone"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "واٹس ایپ انسٹال نہیں ہے یا نمبر درست نہیں", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 2. Direct Phone Dialer
     */
    fun makePhoneCall(context: Context, phone: String) {
        val cleanNumber = phone.trim().replace(" ", "").replace("-", "")
        if (cleanNumber.isBlank()) {
            Toast.makeText(context, "فون نمبر موجود نہیں ہے", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ڈائلر کھولنے میں مسئلہ ہوا: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openDialer(context: Context, phoneNumber: String) {
        makePhoneCall(context, phoneNumber)
    }

    /**
     * 3. Local SMS App
     */
    fun sendSms(context: Context, phoneNumber: String, message: String = "") {
        val cleanNumber = phoneNumber.trim().replace(" ", "").replace("-", "")
        if (cleanNumber.isBlank()) {
            Toast.makeText(context, "فون نمبر موجود نہیں ہے", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanNumber")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ایس ایم ایس کھولنے میں مسئلہ ہوا: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openSmsApp(context: Context, phoneNumber: String, message: String = "") {
        sendSms(context, phoneNumber, message)
    }

    fun openSmsComposer(
        context: Context,
        phoneNumber: String,
        message: String = "",
        onStatusUpdate: (String) -> Unit = {}
    ) {
        sendSms(context, phoneNumber, message)
        onStatusUpdate("Opened in SMS Composer")
    }

    /**
     * 4. Android System Share Sheet (WhatsApp, SMS, Email, etc.)
     */
    fun shareText(context: Context, text: String, title: String = "والدین کو پیغام بھیجیں") {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, title).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "شیئر کرنے میں مسئلہ ہوا: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 5. Copy Text to System Clipboard
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Parent Notification") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "پیغام کامیابی سے کاپی ہو گیا!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "کاپی کرنے میں مسئلہ ہوا: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

