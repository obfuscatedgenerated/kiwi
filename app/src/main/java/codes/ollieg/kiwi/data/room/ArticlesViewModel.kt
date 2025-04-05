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

    val allCachedArticles = repo.allCachedArticlesLive
    fun getAllCached(): List<Article> {
        return runBlocking {
            repo.getAllCached()
        }
    }

    fun getById(wiki: Wiki, pageId: Long, skipCache: Boolean = false): Article? {
        return runBlocking {
            repo.getById(wiki, pageId)
        }
    }

    fun getAllCachedByWiki(wiki: Wiki): List<Article> {
        return runBlocking {
            repo.getAllCachedByWiki(wiki)
        }
    }

    fun getAllCachedByWikiLive(wiki: Wiki): LiveData<List<Article>> {
        return repo.getAllCachedByWikiLive(wiki)
    }

    fun search(wiki: Wiki, query: String, skipCache: Boolean = false): List<Article> {
        return runBlocking {
            repo.search(wiki, query)
        }
    }

    fun insertIntoCache(article: Article): Long {
        return runBlocking {
            repo.insertIntoCache(article)
        }
    }

    fun updateInCache(article: Article): Int {
        return runBlocking {
            repo.updateInCache(article)
        }
    }

    fun upsertIntoCache(article: Article): Long {
        return runBlocking {
            repo.upsertIntoCache(article)
        }
    }

    fun deleteFromCache(article: Article): Int {
        return runBlocking {
            repo.deleteFromCache(article)
        }
    }

    fun deleteByIdFromCache(wiki: Wiki, pageId: Long): Int {
        return runBlocking {
            repo.deleteByIdFromCache(wiki, pageId)
        }
    }

    fun deleteAllByWikiFromCache(wiki: Wiki): Int {
        return runBlocking {
            repo.deleteAllByWikiFromCache(wiki)
        }
    }
}