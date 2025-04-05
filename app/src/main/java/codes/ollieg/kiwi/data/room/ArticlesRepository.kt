package codes.ollieg.kiwi.data.room

import androidx.lifecycle.LiveData

// this repository serves to get info about articles and their content
// from both the mediawiki api and the local database as a cache first or for offline use
// (or explicitly from the cache if using the cache methods)

const val CACHE_TTL = 1000 * 60 * 5 // 5 minutes

class ArticlesRepository(private val articlesDao: ArticlesDao) {
    val allCachedArticlesLive: LiveData<List<Article>> = articlesDao.getAllLive()

    suspend fun getAllCached(): List<Article> {
        return articlesDao.getAll()
    }

    fun getByIdCachedLive(wiki: Wiki, pageId: Long): LiveData<Article?> {
        return articlesDao.getByIdLive(wiki.id, pageId)
    }

    suspend fun getByIdCached(wiki: Wiki, pageId: Long): Article? {
        return articlesDao.getById(wiki.id, pageId)
    }

    suspend fun getById(wiki: Wiki, pageId: Long, skipCache: Boolean = false): Article? {
        // check the cache for it
        val cached = getByIdCached(wiki, pageId)

        if (cached != null && !skipCache) {
            // check if the cache is still valid
            if (cached.updateTime != null && System.currentTimeMillis() - cached.updateTime!! < CACHE_TTL) {
                return cached
            }
        }

        // TODO: check for the latest revision id
        // TODO: if the revision id is the same, return the cached article
        // TODO: otherwise, fetch the article from the api and update the cache. make sure to update revisionId and updateTime
        // for now return the cached article
        return cached

        // TODO: give the user a way to pull to refresh the article on the article screen, which will do skipCache = true
    }

    fun getAllCachedByWikiLive(wiki: Wiki): LiveData<List<Article>> {
        return articlesDao.getAllByWikiIdLive(wiki.id)
    }

    suspend fun getAllCachedByWiki(wiki: Wiki): List<Article> {
        return articlesDao.getAllByWikiId(wiki.id)
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

    suspend fun deleteByIdFromCache(wiki: Wiki, pageId: Long): Int {
        return articlesDao.deleteById(wiki.id, pageId)
    }

    suspend fun deleteAllByWikiFromCache(wiki: Wiki): Int {
        return articlesDao.deleteAllByWikiId(wiki.id)
    }

    fun searchCacheLive(wiki: Wiki, query: String): LiveData<List<Article>> {
        return articlesDao.searchByTitleLive(wiki.id, query)
    }

    suspend fun searchCache(wiki: Wiki, query: String): List<Article> {
        return articlesDao.searchByTitle(wiki.id, query)
    }

    suspend fun search(wiki: Wiki, query: String, skipCache: Boolean = false): List<Article> {
        // TODO: if offline, search in local db. otherwise, search in api
        return searchCache(wiki, query)
    }

    // TODO: once api implemented here, remove use of the api from other classes
}
