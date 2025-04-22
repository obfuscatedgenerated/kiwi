package codes.ollieg.kiwi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import codes.ollieg.kiwi.data.checkOnline
import codes.ollieg.kiwi.data.room.WikisViewModel
import codes.ollieg.kiwi.ui.KiwiNavDrawer
import codes.ollieg.kiwi.ui.KiwiTopBar
import codes.ollieg.kiwi.ui.theme.KiWiTheme


enum class AppScreens {
    WikiHome,
    Article,
    ManageWikis,
    ManageStorage,
    OtherSettings,
}

class ConnectionChangeReceiver(context: Context) {
    var initialValue: Boolean? = null
    var lastAirplaneMode: Boolean = false

    fun setInitialValues(context: Context) {
        this.initialValue = checkOnline(context)
        this.lastAirplaneMode = isAirplaneModeOn(context)
    }

    private fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
    }


    private val connectionChangeCallback = object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {
            super.onAvailable(network)

            if (initialValue == true) {
                // ignore this event once, as sometimes the callback recieves an event as soon as the app starts
                initialValue = null
                return
            }

            // if it was airplane mode that changed, ignore this event and let the broadcast receiver handle it
            if (isAirplaneModeOn(context) != lastAirplaneMode) {
                lastAirplaneMode = isAirplaneModeOn(context)
                return
            }

            Toast.makeText(context, context.getString(R.string.back_online), Toast.LENGTH_LONG)
                .show()
        }

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)

            if (initialValue == false) {
                // ignore this event once, as sometimes the callback recieves an event as soon as the app starts
                initialValue = null
                return
            }

            // if it was airplane mode that changed, ignore this event and let the broadcast receiver handle it
            if (isAirplaneModeOn(context) != lastAirplaneMode) {
                lastAirplaneMode = isAirplaneModeOn(context)
                return
            }

            Toast.makeText(context, context.getString(R.string.device_offline), Toast.LENGTH_LONG)
                .show()
        }
    }

    fun getCallback(): ConnectivityManager.NetworkCallback {
        return connectionChangeCallback
    }
}

class AirplaneModeReciever : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            val isAirplaneModeOn = intent.getBooleanExtra("state", false)
            if (isAirplaneModeOn) {
                Toast.makeText(context, context.getString(R.string.airplane_mode_on), Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(context, context.getString(R.string.airplane_mode_off), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
        .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    val connChangeReceiver = ConnectionChangeReceiver(this)
    val networkCallback = connChangeReceiver.getCallback()

    val airplaneModeReceiver = AirplaneModeReciever()

    override fun onResume() {
        super.onResume()

        // register receiver for airplane mode changes
        val connChangeReceiverFlags = ContextCompat.RECEIVER_NOT_EXPORTED
        val connChangeFilter = IntentFilter("android.intent.action.AIRPLANE_MODE")
        ContextCompat.registerReceiver(this, airplaneModeReceiver, connChangeFilter, connChangeReceiverFlags)

        // set network callback
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    override fun onPause() {
        super.onPause()

        // unregister receiver to avoid memory leaks
        unregisterReceiver(airplaneModeReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // now we can access system services, so set the initial value of the connection change receiver
        connChangeReceiver.setInitialValues(this)

        setContent {
            val navController = rememberNavController()

            val wikisViewModel = WikisViewModel(this.application)

            // part of docs on how to use the drawer, heavily adapted to a component
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            // composing the ui from the custom components for a very readable main activity!
            KiWiTheme {
                KiwiNavDrawer (
                    navController = navController,
                    wikisViewModel = wikisViewModel,
                    drawerState = drawerState
                ) {
                    Scaffold(
                        topBar = {
                            KiwiTopBar(
                                navController = navController,
                                wikisViewModel = wikisViewModel,
                                drawerState = drawerState,
                                scope = scope
                            )
                        }
                    ) { padding ->
                        KiwiNavHost(
                            navController = navController,
                            padding = padding
                        )
                    }
                }
            }
        }
    }
}
