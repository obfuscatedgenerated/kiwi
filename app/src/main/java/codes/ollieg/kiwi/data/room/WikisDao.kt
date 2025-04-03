package codes.ollieg.kiwi.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WikisDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(wiki: Wiki): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun update(wiki: Wiki): Int

    suspend fun insertOrUpdate(wiki: Wiki): Long {
        return try {
            insert(wiki)
        } catch (e: Exception) {
            update(wiki)
            wiki.id
        }
    }

    @Delete
    suspend fun delete(wiki: Wiki): Int

    @Query("DELETE FROM Wikis WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM Wikis")
    fun getAllLive(): LiveData<List<Wiki>>

    @Query("SELECT * FROM Wikis")
    suspend fun getAll(): List<Wiki>

    @Query("SELECT * FROM Wikis WHERE id = :id")
    fun getByIdLive(id: Long): LiveData<Wiki?>

    @Query("SELECT * FROM Wikis WHERE id = :id")
    suspend fun getById(id: Long): Wiki?
}