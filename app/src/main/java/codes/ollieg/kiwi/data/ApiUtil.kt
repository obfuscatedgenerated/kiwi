package codes.ollieg.kiwi.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.cookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

// 3rd party library used in this file: ktor
// purpose: to make http requests with a cookie store in a cleaner way than httpsurlconnection
// https://ktor.io/docs/welcome.html
// https://github.com/ktorio/ktor/blob/main/LICENSE

private val client = HttpClient(CIO) {
    // throw errors for non ok responses
    expectSuccess = true

    install(Logging) {
        logger = object: Logger {
            override fun log(message: String) {
                Log.d("HttpClient", message)
            }
        }

        level = LogLevel.BODY
    }

    install(HttpCookies)
}

fun checkOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

// simpler wrapper around the ktor client
suspend fun fetch(url: String, headers: Map<String, String> = emptyMap(), httpMethod: HttpMethod = HttpMethod.Get, body: Any? = null): String {
    val response = client.request(url) {
        method = httpMethod

        headers.forEach { (key, value) ->
            header(key, value)
        }

        if (body != null) {
            setBody(body)
        }
    }

    return response.body()
}

suspend fun fetch(url: URL, headers: Map<String, String> = emptyMap(), httpMethod: HttpMethod = HttpMethod.Get, body: Any? = null): String {
    return fetch(url.toString(), headers, httpMethod, body)
}

suspend fun fetchBytes(url: String, headers: Map<String, String> = emptyMap(), httpMethod: HttpMethod = HttpMethod.Get, body: Any? = null): ByteArray {
    val response = client.request(url) {
        method = httpMethod

        headers.forEach { (key, value) ->
            header(key, value)
        }

        if (body != null) {
            setBody(body)
        }
    }

    return response.body()
}

suspend fun fetchBytes(url: URL, headers: Map<String, String> = emptyMap(), httpMethod: HttpMethod = HttpMethod.Get, body: Any? = null): ByteArray {
    return fetchBytes(url.toString(), headers, httpMethod, body)
}

// uses the client to log in to a mediawiki and persists the session in memory
suspend fun logInToMediawiki(apiUrl: String, username: String, password: String) {
    // (not using try catch here, just have it throw an error if anything is wrong)

    // get a login token
    val tokenUrl = fromApiBase(apiUrl, "?action=query&meta=tokens&type=login&format=json")
    val loginToken = fetch(tokenUrl, withDefaultHeaders())

    // parse the json
    val data = JSONObject(loginToken)
    val token = data.getJSONObject("query").getJSONObject("tokens").getString("logintoken")

    // log in with the token
    val loginBody = FormDataContent(Parameters.build {
        append("lgname", username)
        append("lgpassword", password)
        append("lgtoken", token)
    })

    val loginUrl = fromApiBase(apiUrl, "?action=login&format=json")
    val loginResponse = fetch(loginUrl, withDefaultHeaders(), HttpMethod.Post, loginBody)

    // parse the json
    val loginData = JSONObject(loginResponse)
    val loginResult = loginData.getJSONObject("login").getString("result")

    if (loginResult != "Success") {
        throw Exception("Login failed: $loginResult")
    }

    // success!
}

suspend fun isLoggedInToMediawiki(apiUrl: String, username: String): Boolean {
    // check the cookie store for the login session
    client.cookies(apiUrl).forEach { cookie ->
        // different mediawiki versions and instances have different cookie names, so just search for the username in the values
        if (cookie.value == username) {
            return true
        }
    }

    // if no cookie was found, return false
    return false
}

// sets some default headers needed for mediawiki but allows for more to be added
fun withDefaultHeaders(extraHeaders: Map<String, String> = emptyMap()): Map<String, String> {
    val headers = extraHeaders.toMutableMap()

    // set json headers
    headers["Accept"] = "application/json"
    headers["Content-Type"] = "application/json"

    // set user agent for mediawiki ettiquette
    headers["User-Agent"] = "KiWiApp/1.0 (Android)"

    return headers
}

// safely appends a path to a base url
fun fromApiBase(base: String, path: String): URL {
    val baseUrl = URL(base)

    // remove leading slash from path to ensure it is appended correctly
    val safePath = if (path.startsWith("/")) {
        path.substring(1)
    } else {
        path
    }

    // append with the url class
    return URL(baseUrl, safePath)
}

// gets the query parameters from the url as a mutable map
fun getQueryParameters(url: URL): MutableMap<String, String> {
    val query = url.query ?: return mutableMapOf()

    // split each separate parameter by &
    val params = query.split("&")
    val result = mutableMapOf<String, String>()

    for (param in params) {
        // get the key and value separated by =
        val pair = param.split("=")

        if (pair.size == 2) {
            val key = pair[0]
            val value = pair[1]
            result[key] = value
        }
    }

    return result
}

// replaces query parameters in the url with the given map
fun replaceQueryParameters(url: URL, params: Map<String, String>): URL {
    if (params.isEmpty()) {
        // remove query part
        return URL(url.protocol, url.host, url.port, url.path)
    }

    // join the parameters with &key=value (url encoded)
    val query = params.entries.joinToString("&") {
        val urlEncodedKey = URLEncoder.encode(it.key, "UTF-8")
        var urlEncodedValue = URLEncoder.encode(it.value, "UTF-8")

        // bit of a hack, but mediawiki insists on actual | and not the url escaped %257C in the values
        // this function can cause this to happen, so replace it back
        urlEncodedValue = urlEncodedValue.replace("%257C", "|")

        "${urlEncodedKey}=${urlEncodedValue}"
    }

    // replace the query part
    return URL(url.protocol, url.host, url.port, "${url.path}?$query")
}

// updates a single query parameter in the url safely
fun setQueryParameter(url: URL, key: String, value: String): URL {
    val params = getQueryParameters(url)
    params[key] = value

    return replaceQueryParameters(url, params)
}

data class SiteInfo(
    val siteName: String,
    val extensions: List<String>
)

suspend fun getSiteInfo(apiUrl: String): SiteInfo {
    val url = fromApiBase(apiUrl, "?action=query&meta=siteinfo&siprop=general|extensions&format=json")
    val response = fetch(url, withDefaultHeaders())

    // parse the json
    val data = JSONObject(response)

    // get site name and extension list
    val siteName = data.getJSONObject("query").getJSONObject("general").getString("sitename")
    val extensions = data.getJSONObject("query").getJSONArray("extensions")
    val extensionList = mutableListOf<String>()
    for (i in 0 until extensions.length()) {
        val extension = extensions.getJSONObject(i)
        val name = extension.getString("name")
        extensionList.add(name)
    }

    return SiteInfo(siteName, extensionList)
}
