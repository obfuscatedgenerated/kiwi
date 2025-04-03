package codes.ollieg.kiwi.data.room

import androidx.lifecycle.LiveData

class WikisRepository(private val wikisDao: WikisDao) {
    val allWikis = wikisDao.getAll()

    suspend fun insert(wiki: Wiki): Long {
        return wikisDao.insert(wiki)
    }

    suspend fun update(wiki: Wiki): Int {
        return wikisDao.update(wiki)
    }

    suspend fun delete(wiki: Wiki): Int {
        return wikisDao.delete(wiki)
    }

    suspend fun deleteById(id: Long): Int {
        return wikisDao.deleteById(id)
    }

    suspend fun getById(id: Long): LiveData<Wiki?> {
        return wikisDao.getById(id)
    }
}
