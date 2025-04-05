package codes.ollieg.kiwi.data.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.runBlocking

class ArticlesViewModel (application: Application) : AndroidViewModel(application) {
    private val repo: ArticlesRepository

    init {
        val articlesDao = KiwiDatabase.getDatabase(application).articlesDao()
        repo = ArticlesRepository(articlesDao)
    }

    val allArticles = repo.allArticlesLive
    fun getAll(): List<Article> {
        return runBlocking {
            repo.getAll()
        }
    }

    fun getById(wikiId: Long, pageId: Long): Article? {
        return runBlocking {
            repo.getById(wikiId, pageId)
        }
    }

    fun getByIdLive(wikiId: Long, pageId: Long): LiveData<Article?> {
        return repo.getByIdLive(wikiId, pageId)
    }

    fun getAllByWikiId(wikiId: Long): List<Article> {
        return runBlocking {
            repo.getAllCachedByWikiId(wikiId)
        }
    }

    fun getAllByWikiIdLive(wikiId: Long): LiveData<List<Article>> {
        return repo.getAllCachedByWikiIdLive(wikiId)
    }

    fun searchByTitle(wikiId: Long, query: String): List<Article> {
        return runBlocking {
            repo.search(wikiId, query)
        }
    }

    fun searchByTitleLive(wikiId: Long, query: String): LiveData<List<Article>> {
        return repo.searchLive(wikiId, query)
    }

    fun insert(article: Article): Long {
        return runBlocking {
            repo.insertIntoCache(article)
        }
    }

    fun update(article: Article): Int {
        return runBlocking {
            repo.updateInCache(article)
        }
    }

    fun upsert(article: Article): Long {
        return runBlocking {
            repo.upsertIntoCache(article)
        }
    }

    fun delete(article: Article): Int {
        return runBlocking {
            repo.deleteFromCache(article)
        }
    }

    fun deleteById(wikiId: Long, pageId: Long): Int {
        return runBlocking {
            repo.deleteByIdFromCache(wikiId, pageId)
        }
    }

    fun deleteAllByWikiId(wikiId: Long): Int {
        return runBlocking {
            repo.deleteAllByWikiIdFromCache(wikiId)
        }
    }
}