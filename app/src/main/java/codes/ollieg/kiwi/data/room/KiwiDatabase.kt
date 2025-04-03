package codes.ollieg.kiwi.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Wiki::class], version =1, exportSchema = false)
abstract class KiwiDatabase: RoomDatabase(){
    abstract fun wikisDao(): WikisDao

    companion object {
        @Volatile
        private var Instance: KiwiDatabase? = null
        fun getDatabase(context: Context): KiwiDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, KiwiDatabase::class.java, "kiwi")
                    .build().also {
                        // need a coroutine scope to run the insert in the background
                        CoroutineScope(Dispatchers.IO).launch {
                            // add default wiki entry for wikipedia
                            val wikisRepository = WikisRepository(it.wikisDao())
                            wikisRepository.insert(
                                Wiki(
                                    id = 0,
                                    name = "Wikipedia",
                                    apiUrl = "https://en.wikipedia.org/w/api.php",
                                    authUsername = null,
                                    authPassword = null
                                )
                            )
                        }

                        Instance = it
                    }
            }
        }
    }
}