package codes.ollieg.kiwi.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Wikis")
data class Wiki (
    @PrimaryKey(autoGenerate = true) val id: Long,

    val name: String,
    val apiUrl: String,
    val authUsername: String?,
    val authPassword: String?,
)