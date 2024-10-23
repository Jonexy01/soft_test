package com.example.softtest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.softtest.app.AppUtils.getSampleUserData
import com.example.softtest.ui.theme.SoftTestTheme
import com.google.gson.Gson
import com.netpluspay.contactless.sdk.utils.ContactlessReaderResult
import com.netpluspay.nibssclient.models.UserData
import com.netpluspay.nibssclient.service.NetposPaymentClient
import com.pixplicity.easyprefs.library.Prefs
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.danbamitale.epmslib.entities.CardData
import com.danbamitale.epmslib.entities.clearPinKey
import com.example.softtest.app.AppUtils.CONFIG_DATA
import com.example.softtest.app.AppUtils.KEY_HOLDER
import com.example.softtest.app.AppUtils.getSavedKeyHolder
import com.example.softtest.models.CardResult
import com.netpluspay.contactless.sdk.start.ContactlessSdk

class MainActivity : ComponentActivity() {

    private val gson: Gson = Gson()
    private var userData: UserData = getSampleUserData(this)
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    var netposPaymentClient: NetposPaymentClient = NetposPaymentClient

    private val makePaymentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == ContactlessReaderResult.RESULT_OK) {
                data?.let { i ->
                    val cardReadData = i.getStringExtra("data")!!
                    val cardResult = gson.fromJson(cardReadData, CardResult::class.java)
//                    makeCardPayment(cardResult, amountToPay.toLong())
                }
            }
            if (result.resultCode == ContactlessReaderResult.RESULT_ERROR) {
                data?.let { i ->
                    val error = i.getStringExtra("data")
                    error?.let {
//                        _result?.error("CARD_ERROR", "Card read error", it)
//                        Timber.d("ERROR_TAG===>%s", it)
//                        resultViewerTextView.text = it
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoftTestTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(200.dp))
                        Text(
                            text = "Tap to read card",
                            fontSize = 18.sp,
                            color = Color.Blue
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Button(onClick = {
                            doCardTransaction("1000")
                        }) {
                            Text(text = "Proceed")
                        }
                    }
//                    Greeting("Android")
                }
            }
        }
        configureTerminal()
    }

    private fun configureTerminal() {
        compositeDisposable.add(
            netposPaymentClient.init(this, Gson().toJson(userData))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    data?.let { response ->
                        val keyHolder = response.first
                        val configData = response.second
                        val pinKey = keyHolder?.clearPinKey
                        if (pinKey != null) {
                            Prefs.putString(KEY_HOLDER, gson.toJson(keyHolder))
                            Prefs.putString(CONFIG_DATA, gson.toJson(configData))
                            // Return success to Flutter
//                            _result?.success("Terminal configured successfully")
                        }
                    }
                }, { error ->
                    // Return error to Flutter
//                    _result?.error("CONFIG_FAILED", "Terminal configuration failed", error.localizedMessage)
//                    Timber.d("%s%s", ERROR_TAG, error.localizedMessage)
                })
        )
    }

    private fun doCardTransaction(amount: String) {
        try {
            launchContactless(makePaymentResultLauncher, amount.toDouble())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun launchContactless(
        launcher: ActivityResultLauncher<Intent>,
        amountToPay: Double,
        cashBackAmount: Double = 0.0,
    ) {
        val savedKeyHolder = getSavedKeyHolder()

        savedKeyHolder?.run {
            ContactlessSdk.readContactlessCard(
                this@MainActivity,
                launcher,
                this.clearPinKey, // "86CBCDE3B0A22354853E04521686863D" // pinKey
                amountToPay, // amount
                cashBackAmount, // cashbackAmount(optional)
            )
        } ?: run {
//            _result?.error("NO_CONFIG", "Terminal not configured", "Terminal not configured")
//            Timber.d("%s%s", ERROR_TAG, "Terminal not configured")
            configureTerminal()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SoftDisplay() {
    SoftTestTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(200.dp))
                Text(
                    text = "Tap to read card",
                    fontSize = 18.sp,
                    color = Color.Blue
                )
                Spacer(modifier = Modifier.height(30.dp))
                Button(onClick = {
                    //
                }) {
                    Text(text = "Proceed")
                }
            }
//                    Greeting("Android")
        }
    }
}