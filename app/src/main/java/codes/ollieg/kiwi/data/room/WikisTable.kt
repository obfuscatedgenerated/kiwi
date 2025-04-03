package codes.ollieg.kiwi.data.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// index provides UNIQUE constraint on the name
@Entity(tableName = "Wikis", indices = [Index(value=["name"], unique = true)])
data class Wiki (
    @PrimaryKey(autoGenerate = true) val id: Long,

    var name: String,
    var apiUrl: String,
    var authUsername: String,
    var authPassword: String,
)