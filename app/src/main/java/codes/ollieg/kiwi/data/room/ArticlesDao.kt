package codes.ollieg.kiwi.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ArticlesDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(article: Article): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun update(article: Article): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article): Long

    // TODO: dont allow insertion into the cache if it is missing content or any other nullable basics
    // maybe need to update ArticlesTable so that its nullable in code but not in the database

    @Delete
    suspend fun delete(article: Article): Int

    @Query("DELETE FROM Articles WHERE wikiId = :wikiId AND pageId = :pageId")
    suspend fun deleteById(wikiId: Long, pageId: Long): Int

    @Query("DELETE FROM Articles WHERE wikiId = :wikiId")
    suspend fun deleteAllByWikiId(wikiId: Long): Int

    @Query("SELECT * FROM Articles")
    fun getAllLive(): LiveData<List<Article>>

    @Query("SELECT * FROM Articles")
    suspend fun getAll(): List<Article>

    @Query("SELECT * FROM Articles WHERE wikiId = :wikiId AND pageId = :pageId")
    fun getByIdLive(wikiId: Long, pageId: Long): LiveData<Article?>

    @Query("SELECT * FROM Articles WHERE wikiId = :wikiId AND pageId = :pageId")
    suspend fun getById(wikiId: Long, pageId: Long): Article?

    @Query("SELECT * FROM Articles WHERE wikiId = :wikiId")
    fun getAllByWikiIdLive(wikiId: Long): LiveData<List<Article>>

    @Query("SELECT * FROM Articles WHERE wikiId = :wikiId")
    suspend fun getAllByWikiId(wikiId: Long): List<Article>

    @Query("SELECT * FROM Articles WHERE wikiId = :wikiId AND title LIKE '%' || :query || '%'")
    fun searchByTitleLive(wikiId: Long, query: String): LiveData<List<Article>>

    @Query("SELECT * FROM Articles WHERE wikiId = :wikiId AND title LIKE '%' || :query || '%'")
    suspend fun searchByTitle(wikiId: Long, query: String): List<Article>

    @Query("SELECT * FROM Articles WHERE wikiId = :wikiId AND starred = 1")
    suspend fun getStarredByWikiId(wikiId: Long): List<Article>

    @Query("SELECT * FROM Articles WHERE wikiId = :wikiId AND starred = 1")
    fun getStarredByWikiIdLive(wikiId: Long): LiveData<List<Article>>
}