package codes.ollieg.kiwi.data.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.runBlocking

class WikisViewModel (application: Application) : AndroidViewModel(application) {
    private val repo: WikisRepository

    init {
        val wikisDao = KiwiDatabase.getDatabase(application).wikisDao()
        repo = WikisRepository(wikisDao)
    }

    val allWikis = repo.allWikisLive
    fun getAll(): List<Wiki> {
        return runBlocking {
            repo.getAll()
        }
    }

    fun getById(id: Long): Wiki? {
        return runBlocking {
            repo.getById(id)
        }
    }

    fun getByIdLive(id: Long): LiveData<Wiki?> {
        return repo.getByIdLive(id)
    }

    suspend fun insert(wiki: Wiki): Long {
        return repo.insert(wiki)
    }

    suspend fun update(wiki: Wiki): Int {
        return repo.update(wiki)
    }
}