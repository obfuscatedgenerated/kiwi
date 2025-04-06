package codes.ollieg.kiwi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

class ConnectionChangeReceiver : BroadcastReceiver() {
    var lastOnlineValue: Boolean? = null

    fun setInitialValue(context: Context) {
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
    var connChangeReceiver = ConnectionChangeReceiver()

    override fun onResume() {
        super.onResume()

        // register receiver to show toast when device connects to or loses connection to the internet
        // TODO: use newer callback if approved
        val connChangeReceiverFlags = ContextCompat.RECEIVER_NOT_EXPORTED
        val connChangeFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        ContextCompat.registerReceiver(this, connChangeReceiver, connChangeFilter, connChangeReceiverFlags)
    }

    override fun onPause() {
        super.onPause()

        // unregister receiver to avoid memory leaks
        unregisterReceiver(connChangeReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connChangeReceiver.setInitialValue(this.applicationContext)

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
