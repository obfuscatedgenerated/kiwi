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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(wiki: Wiki): Long

    // this method shouldn't be used by other classes, but can't make it private
    @Delete
    suspend fun deleteUnsafe(wiki: Wiki): Int

    suspend fun delete(wiki: Wiki): Int {
        if (wiki.id == 1L) {
            // require at least one wiki to be present as the app starts at wiki 1
            // if this does get deleted, the app will recreate wikipedia on startup, unclear behaviour
            throw IllegalArgumentException("Cannot delete the record with id = 1")
        }

        return deleteUnsafe(wiki)
    }

    // this method shouldn't be used by other classes, but can't make it private
    @Query("DELETE FROM Wikis WHERE id = :id")
    suspend fun deleteByIdUnsafe(id: Long): Int

    suspend fun deleteById(id: Long): Int {
        if (id == 1L) {
            // require at least one wiki to be present as the app starts at wiki 1
            // if this does get deleted, the app will recreate wikipedia on startup, unclear behaviour
            throw IllegalArgumentException("Cannot delete the record with id = 1")
        }

        return deleteByIdUnsafe(id)
    }

    @Query("SELECT * FROM Wikis")
    fun getAllLive(): LiveData<List<Wiki>>

    @Query("SELECT * FROM Wikis")
    suspend fun getAll(): List<Wiki>

    @Query("SELECT * FROM Wikis WHERE id = :id")
    fun getByIdLive(id: Long): LiveData<Wiki?>

    @Query("SELECT * FROM Wikis WHERE id = :id")
    suspend fun getById(id: Long): Wiki?
}