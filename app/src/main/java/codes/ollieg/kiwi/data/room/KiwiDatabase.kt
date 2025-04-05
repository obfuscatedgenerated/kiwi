package codes.ollieg.kiwi.data.room

import android.content.Context
import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val MIGRATION_2_3_NOT_NULL_AUTH = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // update nulls to empty strings
        database.execSQL("UPDATE Wikis SET authUsername = '' WHERE authUsername IS NULL;")
        database.execSQL("UPDATE Wikis SET authPassword = '' WHERE authPassword IS NULL;")

        // create new table with new not null constraints
        database.execSQL("CREATE TABLE IF NOT EXISTS `Wikis_new`(" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`name` TEXT NOT NULL, `apiUrl` TEXT NOT NULL," +
                "`authUsername` TEXT NOT NULL DEFAULT ''," +
                "`authPassword` TEXT NOT NULL DEFAULT ''" +
        ");")

        // copy data from old table to new table
        database.execSQL("INSERT INTO Wikis_new (id, name, apiUrl, authUsername, authPassword) " +
                "SELECT id, name, apiUrl, authUsername, authPassword FROM Wikis;")

        // drop old index
        database.execSQL("DROP INDEX IF EXISTS `index_Wikis_name`;")

        // drop old table
        database.execSQL("DROP TABLE Wikis;")

        // rename new table to old table name
        database.execSQL("ALTER TABLE Wikis_new RENAME TO Wikis;")

        // create new index
        database.execSQL("CREATE UNIQUE INDEX `index_Wikis_name` ON Wikis (name);")
    }
}

@Database(entities = [Wiki::class, Article::class], version = 5, autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 3, to = 4), AutoMigration(from = 4, to = 5)])
abstract class KiwiDatabase: RoomDatabase(){
    abstract fun wikisDao(): WikisDao
    abstract fun articlesDao(): ArticlesDao

    companion object {
        @Volatile
        private var Instance: KiwiDatabase? = null
        fun getDatabase(context: Context): KiwiDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, KiwiDatabase::class.java, "kiwi").addMigrations(MIGRATION_2_3_NOT_NULL_AUTH)
                    .build().also {
                        // need a coroutine scope to run the insert in the background
                        CoroutineScope(Dispatchers.IO).launch {
                            // add default wiki entry for wikipedia
                            val repo = WikisRepository(it.wikisDao())

                            // check if the database is empty
                            val wikis = repo.getAll()
                            if (wikis.isNotEmpty() == true) {
                                Log.i("KiwiDatabase", "Wikis already exist, skipping default entry.")
                                return@launch
                            }

                            // insert the default wikipedia entry
                            repo.insert(
                                Wiki(
                                    id = 1,
                                    name = "Wikipedia",
                                    apiUrl = "https://en.wikipedia.org/w/api.php",
                                    authUsername = "",
                                    authPassword = ""
                                )
                            )
                        }

                        Instance = it
                    }
            }
        }
    }
}