package codes.ollieg.kiwi.data.room

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.map
import codes.ollieg.kiwi.data.checkOnline
import codes.ollieg.kiwi.data.fetch
import codes.ollieg.kiwi.data.fetchBytes
import codes.ollieg.kiwi.data.fromApiBase
import codes.ollieg.kiwi.data.setQueryParameter
import codes.ollieg.kiwi.data.withDefaultHeaders
import org.json.JSONObject

// this repository serves to get info about articles and their content
// from both the mediawiki api and the local database as a cache first or for offline use
// (or explicitly from the cache if using the cache methods)

const val CACHE_TTL = 1000 * 60 * 5 // 5 minutes
const val THUMB_SIZE = 500

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
        // TODO: handle redirects

        // check the cache for it
        val cached = getByIdCached(wiki, pageId)

        if (cached != null && !skipCache) {
            // check if the cache is still valid
            if (cached.updateTime != null && System.currentTimeMillis() - cached.updateTime!! < CACHE_TTL) {
                Log.i(
                    "ArticlesRepository",
                    "cached article is still within ttl, returning cached article"
                )
                return cached
            }
        }

        // check the latest revision id from the api
        var revUrl = fromApiBase(
            wiki.apiUrl,
            "?action=query&prop=revisions&utf8=&format=json&formatversion=2&rvlimit=1&rvprop=ids"
        )
        revUrl = setQueryParameter(revUrl, "pageids", pageId.toString())

        Log.i("ArticlesRepository", "revUrl: $revUrl")

        var latestRevId: Long? = null
        var title: String? = null
        try {
            val revRes = fetch(revUrl, withDefaultHeaders())
            Log.i("ArticlesRepository", "revRes: $revRes")

            // parse the json
            val data = JSONObject(revRes)
            val revisions = data.getJSONObject("query").getJSONArray("pages")

            // if there is a revision, try to get the latest revision id and title
            if (revisions.length() > 0) {
                val page = revisions.getJSONObject(0)
                latestRevId = page.getJSONArray("revisions").getJSONObject(0).getLong("revid")
                title = page.getString("title")
            } else {
                Log.e("ArticlesRepository", "no revisions found for page $pageId")
            }
        } catch (e: Exception) {
            Log.e("ArticlesRepository", "Error fetching latest revision id", e)
        }

        if (latestRevId == null) {
            Log.e("ArticlesRepository", "latestRevId is null, returning cached article")
            return cached
        }

        // if the revision id is the same, return the cached article
        if (cached != null && latestRevId == cached.revisionId) {
            Log.i("ArticlesRepository", "cached article is up to date, returning cached article")
            // TODO: handle title change
            return cached
        }

        // otherwise, fetch the article from the api
        // this uses the textextracts extension, so the add wiki check needs to check special:version to see if it's installed
        // this request also gets the url with the info prop
        var requestUrl = fromApiBase(
            wiki.apiUrl,
            "?action=query&prop=extracts|info|pageimages&explaintext=1&inprop=url&pilicense=any&formatversion=2&format=json"
        )
        requestUrl = setQueryParameter(requestUrl, "pageids", pageId.toString())
        requestUrl = setQueryParameter(requestUrl, "pithumbsize", THUMB_SIZE.toString())

        Log.i("ArticlesRepository", "articleUrl: $requestUrl")

        var articleText: String? = null
        var articlePageUrl: String? = null
        var articleThumbUrl: String? = null
        try {
            val articleRes = fetch(requestUrl, withDefaultHeaders())
            Log.i("ArticlesRepository", "articleRes: $articleRes")

            // parse the json
            val data = JSONObject(articleRes)
            val queryData = data.getJSONObject("query")
            val pagesData = queryData.getJSONArray("pages")

            // if there is a page, try to get the extract
            if (pagesData.length() > 0) {
                val page = pagesData.getJSONObject(0)
                articleText = page.getString("extract")
                articlePageUrl = page.getString("fullurl")
                articleThumbUrl = page.getJSONObject("thumbnail").getString("source")
            } else {
                Log.e("ArticlesRepository", "no pages found for page $pageId")
            }
        } catch (e: Exception) {
            Log.e("ArticlesRepository", "Error fetching article", e)
        }

        // if we don't already have the title, just set it to something. it should be updated later when ttl expires anyway
        // TODO: retry the fetch? would it be worth it?
        if (title == null) {
            Log.e("ArticlesRepository", "title is null, using pageId as title")
            title = "Article $pageId"
        }

        // search snippet is centered around the query provided. for the snippets here (for home page),
        // just use the first chars of content, which will be more relevant and doesn't require additional queries
        val snippet = articleText?.take(100) ?: ""

        // if there is no cached thumbnail, fetch the url provided from pageimages
        var thumbnail: ByteArray? = cached?.thumbnail
        if (thumbnail == null) {
            try {
                if (articleThumbUrl != null) {
                    thumbnail = fetchBytes(articleThumbUrl, withDefaultHeaders())
                }
            } catch (e: Exception) {
                Log.e("ArticlesRepository", "Error fetching thumbnail", e)
            }
        }

        // update the cache, making sure to update the revision id and update time
        val article = Article(
            wikiId = wiki.id,
            pageId = pageId,

            title = title,

            parsedSnippet = snippet,
            parsedContent = articleText,
            thumbnail = thumbnail,

            pageUrl = articlePageUrl,

            revisionId = latestRevId,
            updateTime = System.currentTimeMillis()
        )

        upsertIntoCache(article)
        Log.i("ArticlesRepository", "upserted article: $article")

        return article
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

    suspend fun deleteAllFromCache(): Int {
        return articlesDao.deleteAll()
    }

    fun searchCacheLive(wiki: Wiki, query: String, limit: Int?): LiveData<List<Article>> {
        return articlesDao.searchByTitleLive(wiki.id, query, limit)
    }

    suspend fun searchCache(wiki: Wiki, query: String, limit: Int?): List<Article> {
        return articlesDao.searchByTitle(wiki.id, query, limit)
    }

    suspend fun searchOnline(wiki: Wiki, query: String, limit: Int?): List<Article> {
        // build the search request url safely
        var searchUrl = fromApiBase(wiki.apiUrl, "?action=query&list=search&utf8=&format=json")
        searchUrl = setQueryParameter(searchUrl, "srsearch", query)

        if (limit != null) {
            searchUrl = setQueryParameter(searchUrl, "srlimit", limit.toString())
        }

        Log.i("ArticlesRepository", "searchUrl: $searchUrl")

        return try {
            val searchRes = fetch(searchUrl, withDefaultHeaders())
            Log.i("ArticlesRepository", "searchRes: $searchRes")

            // parse the json
            val data = JSONObject(searchRes)
            val searchData = data.getJSONObject("query").getJSONArray("search")
            val articles = mutableListOf<Article>()

            // iterate over the search results and create article objects
            for (i in 0 until searchData.length()) {
                val entry = searchData.getJSONObject(i)
                val pageId = entry.getLong("pageid")
                val title = entry.getString("title")

                var snippetHtml: String? = null
                try {
                    snippetHtml = entry.getString("snippet")
                } catch (e: Exception) {
                    Log.e("ArticlesRepository", "Error getting snippet (or null)", e)
                }

                var parsedSnippet = AnnotatedString.fromHtml(snippetHtml ?: "")

                val article = Article(
                    wikiId = wiki.id,
                    pageId = pageId,
                    title = title,
                    parsedSnippet = parsedSnippet.toString(),
                )

                articles.add(article)
            }

            // return the search results
            return articles
        } catch (e: Exception) {
            Log.e("ArticlesRepository", "Error fetching search results", e)
            return emptyList()
        }
    }

    suspend fun search(wiki: Wiki, query: String, context: Context, limit: Int?): List<Article> {
        if (query.isEmpty()) {
            return emptyList()
        }

        if (checkOnline(context)) {
            Log.i("ArticlesRepository", "device is online, searching online")
            return searchOnline(wiki, query, limit)
        } else {
            Log.i("ArticlesRepository", "device is offline, searching cache")
            return searchCache(wiki, query, limit)
        }
    }

    fun getStarredByWikiLive(wiki: Wiki): LiveData<List<Article>> {
        return articlesDao.getStarredByWikiIdLive(wiki.id)
    }

    suspend fun getStarredByWiki(wiki: Wiki): List<Article> {
        return articlesDao.getStarredByWikiId(wiki.id)
    }

    data class StorageUsageEstimate(val bytes: Long, val count: Int)

    suspend fun estimateOfflineStorageUsageForWiki(wiki: Wiki): StorageUsageEstimate {
        val articles = getAllCachedByWiki(wiki)
        val bytes = articles.sumOf { article ->
            5L + // 4 longs and a bool TODO: use int values to determine actual bytes used
                    article.title.length +
                    (article.parsedContent?.length ?: 0) +
                    (article.parsedSnippet?.length ?: 0) +
                    (article.pageUrl?.length ?: 0) +
                    (article.thumbnail?.size ?: 0)
        }
        return StorageUsageEstimate(bytes, articles.size)
    }

    fun estimateOfflineStorageUsageForWikiLive(wiki: Wiki): LiveData<StorageUsageEstimate> {
        val result = MediatorLiveData<StorageUsageEstimate>()

        val articles = getAllCachedByWikiLive(wiki)
        val bytes = articles.map { articles ->
            articles.sumOf { article ->
                5L + // 4 longs and a bool TODO: use int values to determine actual bytes used
                        article.title.length +
                        (article.parsedContent?.length ?: 0) +
                        (article.parsedSnippet?.length ?: 0) +
                        (article.pageUrl?.length ?: 0) +
                        (article.thumbnail?.size ?: 0)
            }
        }

        val count = articles.map { it.size }

        result.addSource(bytes) { b ->
            result.value = StorageUsageEstimate(b, count.value ?: 0)
        }

        result.addSource(count) { c ->
            result.value = StorageUsageEstimate(bytes.value ?: 0, c)
        }

        return result
    }
}
