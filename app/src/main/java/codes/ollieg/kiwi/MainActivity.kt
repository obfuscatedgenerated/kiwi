package codes.ollieg.kiwi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import codes.ollieg.kiwi.data.room.WikisViewModel
import codes.ollieg.kiwi.ui.NavDrawer
import codes.ollieg.kiwi.ui.TopBar
import codes.ollieg.kiwi.ui.theme.KiWiTheme


enum class AppScreens {
    WikiHome,
    Article,
    ManageWikis,
    ManageStorage,
    OtherSettings,
}

class ConnectionChangeReceiver : BroadcastReceiver {
    var lastOnlineValue: Boolean? = null

    fun checkOnline(context: Context): Boolean {
        // check if the device is connected to the internet
        // this api is deprecated, but i couldn't find another way that works nicely with broadcast receivers
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    constructor(context: Context) : super() {
        // set initial value for lastOnlineValue
        lastOnlineValue = checkOnline(context)
        Log.i("ConnectionChangeReceiver", "Initial connection state: $lastOnlineValue")
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onReceive(context: Context, intent: Intent) {
        val isOnline = checkOnline(context)
        Log.i("ConnectionChangeReceiver", "Connection state event: $isOnline")

        if (isOnline == lastOnlineValue) {
            // no change in connection state, do nothing
            Log.i("ConnectionChangeReceiver", "No change in connection state, ignoring event.")
            return
        }

        lastOnlineValue = isOnline

        if (!isOnline) {
            Toast.makeText(context, "Your device just went offline.\n" +
                    "Only offline articles are available.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Back online!", Toast.LENGTH_LONG).show()
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // register receiver to show toast when device connects to or loses connection to the internet
        val connChangeReceiver = ConnectionChangeReceiver(this)
        val connChangeReceiverFlags = ContextCompat.RECEIVER_NOT_EXPORTED
        val connChangeFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        ContextCompat.registerReceiver(this, connChangeReceiver, connChangeFilter, connChangeReceiverFlags)

        setContent {
            val navController = rememberNavController()

            val wikisViewModel = WikisViewModel(this.application)

            // part of docs on how to use the drawer, heavily adapted to a component
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            // composing the ui from the custom components for a very readable main activity!
            KiWiTheme {
                NavDrawer (
                    navController = navController,
                    wikisViewModel = wikisViewModel,
                    drawerState = drawerState
                ) {
                    Scaffold(
                        topBar = {
                            TopBar(
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
