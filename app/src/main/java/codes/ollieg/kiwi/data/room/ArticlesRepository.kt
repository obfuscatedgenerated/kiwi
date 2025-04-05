package codes.ollieg.kiwi.data.room

import androidx.lifecycle.LiveData

// this repository serves to get info about articles and their content
// from both the mediawiki api and the local database as a cache first or for offline use
// (or explicitly from the cache if using the cache methods)

class ArticlesRepository(private val articlesDao: ArticlesDao) {
    val allCachedArticlesLive: LiveData<List<Article>> = articlesDao.getAllLive()

    suspend fun getAllCached(): List<Article> {
        return articlesDao.getAll()
    }

    fun getByIdCachedLive(wiki: Wiki, pageId: Long): LiveData<Article?> {
        return articlesDao.getByIdLive(wiki.id, pageId)
    }

    fun getByIdLive(wiki: Wiki, pageId: Long): LiveData<Article?> {
        // TODO: if available in offline cache (and not outdated), get from local db. otherwise, get from api
        return getByIdCachedLive(wiki, pageId)
    }

    suspend fun getByIdCached(wiki: Wiki, pageId: Long): Article? {
        return articlesDao.getById(wiki.id, pageId)
    }

    suspend fun getById(wiki: Wiki, pageId: Long): Article? {
        // TODO: if available in offline cache (and not outdated), get from local db. otherwise, get from api
        return getByIdCached(wiki, pageId)
    }

    fun getAllCachedByWikiIdLive(wikiId: Long): LiveData<List<Article>> {
        return articlesDao.getAllByWikiIdLive(wikiId)
    }

    suspend fun getAllCachedByWikiId(wikiId: Long): List<Article> {
        return articlesDao.getAllByWikiId(wikiId)
    }

    suspend fun insertIntoCache(article: Article): Long {
        return articlesDao.insert(article)
    }

    suspend fun updateInCache(article: Article): Int {
        return articlesDao.update(article)
    }

    suspend fun upsertIntoCache(article: Article): Long {
        return articlesDao.upsert(article)
    }

    suspend fun deleteFromCache(article: Article): Int {
        return articlesDao.delete(article)
    }

    suspend fun deleteByIdFromCache(wikiId: Long, pageId: Long): Int {
        return articlesDao.deleteById(wikiId, pageId)
    }

    suspend fun deleteAllByWikiIdFromCache(wikiId: Long): Int {
        return articlesDao.deleteAllByWikiId(wikiId)
    }

    fun searchCacheLive(wiki: Wiki, query: String): LiveData<List<Article>> {
        return articlesDao.searchByTitleLive(wiki.id, query)
    }

    fun searchLive(wiki: Wiki, query: String): LiveData<List<Article>> {
        // TODO: if offline, search in local db. otherwise, search in api
        return searchCacheLive(wiki, query)
    }

    suspend fun searchCache(wiki: Wiki, query: String): List<Article> {
        return articlesDao.searchByTitle(wiki.id, query)
    }

    suspend fun search(wiki: Wiki, query: String): List<Article> {
        // TODO: if offline, search in local db. otherwise, search in api
        return searchCache(wiki, query)
    }

    // TODO: once api implemented here, remove use of the api from other classes
}
