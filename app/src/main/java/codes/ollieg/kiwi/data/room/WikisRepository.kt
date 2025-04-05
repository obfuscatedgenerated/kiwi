package codes.ollieg.kiwi.data.room

import androidx.lifecycle.LiveData

// TODO: make this repository useful by fetching live data (e.g. updating title perhaps if the user left it as the default)

class WikisRepository(private val wikisDao: WikisDao) {
    val allWikisLive = wikisDao.getAllLive()

    suspend fun getAll(): List<Wiki> {
        return wikisDao.getAll()
    }

    suspend fun insert(wiki: Wiki): Long {
        return wikisDao.insert(wiki)
    }

    suspend fun update(wiki: Wiki): Int {
        return wikisDao.update(wiki)
    }

    suspend fun upsert(wiki: Wiki): Long {
        return wikisDao.upsert(wiki)
    }

    suspend fun delete(wiki: Wiki): Int {
        return wikisDao.delete(wiki)
    }

    suspend fun deleteById(id: Long): Int {
        return wikisDao.deleteById(id)
    }

    fun getByIdLive(id: Long): LiveData<Wiki?> {
        return wikisDao.getByIdLive(id)
    }

    suspend fun getById(id: Long): Wiki? {
        return wikisDao.getById(id)
    }
}
