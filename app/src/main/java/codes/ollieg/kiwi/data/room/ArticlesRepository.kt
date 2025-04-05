package codes.ollieg.kiwi.data.room

import androidx.lifecycle.LiveData

class ArticlesRepository(private val articlesDao: ArticlesDao) {
    val allArticlesLive: LiveData<List<Article>> = articlesDao.getAllLive()

    suspend fun getAll(): List<Article> {
        return articlesDao.getAll()
    }

    suspend fun insert(article: Article): Long {
        return articlesDao.insert(article)
    }

    suspend fun update(article: Article): Int {
        return articlesDao.update(article)
    }

    suspend fun upsert(article: Article): Long {
        return articlesDao.upsert(article)
    }

    suspend fun delete(article: Article): Int {
        return articlesDao.delete(article)
    }

    suspend fun deleteById(wikiId: Long, pageId: Long): Int {
        return articlesDao.deleteById(wikiId, pageId)
    }

    suspend fun deleteAllByWikiId(wikiId: Long): Int {
        return articlesDao.deleteAllByWikiId(wikiId)
    }

    fun getByIdLive(wikiId: Long, pageId: Long): LiveData<Article?> {
        return articlesDao.getByIdLive(wikiId, pageId)
    }

    suspend fun getById(wikiId: Long, pageId: Long): Article? {
        return articlesDao.getById(wikiId, pageId)
    }

    fun getAllByWikiIdLive(wikiId: Long): LiveData<List<Article>> {
        return articlesDao.getAllByWikiIdLive(wikiId)
    }

    suspend fun getAllByWikiId(wikiId: Long): List<Article> {
        return articlesDao.getAllByWikiId(wikiId)
    }

    fun searchLive(wiki: Wiki, query: String): LiveData<List<Article>> {
        // TODO: if offline, search in local db. otherwise, search in api
        return articlesDao.searchByTitleLive(wiki.id, query)
    }

    suspend fun search(wiki: Wiki, query: String): List<Article> {
        // TODO: if offline, search in local db. otherwise, search in api
        return articlesDao.searchByTitle(wiki.id, query)
    }

    // TODO: make this repository also do the api fetching to make it useful
}
