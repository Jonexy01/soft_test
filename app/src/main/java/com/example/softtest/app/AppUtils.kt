package com.example.softtest.app


import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.danbamitale.epmslib.entities.KeyHolder
import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs
import java.lang.reflect.Method
import com.netpluspay.nibssclient.models.UserData
import android.provider.Settings
import android.telephony.TelephonyManager

object AppUtils {
    const val KEY_HOLDER = "KEY_HOLDER"
    const val CONFIG_DATA = "CONFIG_DATA"
    const val ERROR_TAG = "ERROR_TAG===>"
    const val GENERATED_SN = "GENERATED_SN"
    const val TAG_MAKE_PAYMENT = "TAG_MAKE_PAYMENT"
    const val TAG_CHECK_BALANCE = "TAG_CHECK_BALANCE"
    const val PAYMENT_SUCCESS_DATA_TAG = "PAYMENT_SUCCESS_DATA_TAG"
    const val PAYMENT_ERROR_DATA_TAG = "PAYMENT_ERROR_DATA_TAG"
    const val TAG_TERMINAL_CONFIGURATION = "TAG_TERMINAL_CONFIGURATION"
    const val CARD_HOLDER_NAME = "CUSTOMER"
    const val POS_ENTRY_MODE = "051"

    @SuppressLint("PrivateApi")
    fun getDeviceSerialNumber(context: Context): String {
        var serialNumber: String?
        try {
            val c = Class.forName("android.os.SystemProperties")
            val get: Method = c.getMethod("get", String::class.java)
            serialNumber = get.invoke(c, "gsm.sn1")?.toString()
            if (serialNumber == "") serialNumber = get.invoke(c, "ril.serialnumber")?.toString()
            if (serialNumber == "") serialNumber = get.invoke(c, "ro.serialno")?.toString()
            if (serialNumber == "") serialNumber = get.invoke(c, "sys.serialnumber")?.toString()
            if (serialNumber == "") serialNumber = Build.SERIAL
            if (serialNumber == "unknown") serialNumber = ""
            if (serialNumber == "") if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    serialNumber = Build.getSerial()
                } catch (ex: Exception) {}
            }
            if (serialNumber == "") try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (ex: Exception) {}
            if (serialNumber == "") {
                var generatedSn = Prefs.getString(GENERATED_SN)
                if (generatedSn == null || generatedSn.isEmpty()) {
                    generatedSn = generate12DigitNumber()
                    Prefs.putString(GENERATED_SN, generatedSn)
                }
                serialNumber = generatedSn
            }

            // If none of the methods above worked
            if (serialNumber == "") serialNumber = null
        } catch (e: Exception) {
            e.printStackTrace()
            serialNumber = null
        }
        return serialNumber ?: "12345678901234"
    }

    fun generate12DigitNumber(): String {
        // Get the current timestamp in milliseconds
        val timestamp = System.currentTimeMillis()

        // Convert to a 12-digit number (truncate if necessary)
        val twelveDigitNumber = (timestamp % 1_000_000_000_000).toString()

        // Pad with leading zeros if it's shorter than 12 digits
        return twelveDigitNumber.padStart(12, '0')
    }

    fun getSampleUserData(context: Context) = UserData(
        "Netplus",
        "Netplus",
        "752a873f-ccf5-45fa-bc2c-36331912f283",
        "2033ALZP",
//        "0123456789ABC",
        getDeviceSerialNumber(context),
        "Lekki Lagos",
        "Ellington",
        "2033LAGPOOO7885",
        "",
        "",
    )

//    fun getSampleUserData() = UserData(
//        "Netplus",
//        "Netplus",
//        "5de231d9-1be0-4c31-8658-6e15892f2b83",
//        "2033ALZP",
//        "0123456789ABC", // getDeviceSerialNumber(),
//        // getDeviceSerialNumber(),
//        "Marwa Lagos",
//        "Test Account",
//        "",
//        "",
//        "",
//    )

    fun getSavedKeyHolder(): KeyHolder? {
        val savedKeyHolderInStringFormat = Prefs.getString(KEY_HOLDER)
        return Gson().fromJson(savedKeyHolderInStringFormat, KeyHolder::class.java)
    }
}