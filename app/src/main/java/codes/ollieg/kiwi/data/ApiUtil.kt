package codes.ollieg.kiwi.data

import android.content.Context
import android.net.ConnectivityManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

fun checkOnline(context: Context): Boolean {
    // check if the device is connected to the internet
    // this api is deprecated, but i couldn't find another way that works nicely with broadcast receivers
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

// The helper function that sends a GET request to the URL, returns the response in JSON string
fun fetch(url: String, headers: Map<String, String> = emptyMap()): String {
    var result = ""
    var conn: HttpsURLConnection? = null
    try {
        // create the connection
        val request = URL(url)
        conn = request.openConnection() as
                HttpsURLConnection

        // set the request method
        conn.requestMethod = "GET"

        // set the request headers
        for ((key, value) in headers) {
            conn.setRequestProperty(key, value)
        }

        // send the request
        conn.connect()

        // read the response
        val inStrea: InputStream =
            conn.inputStream
        result =
            convertInputStreamToString(inStrea)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        conn?.disconnect()
    }
    return result //returns the fetched JSON string
}

fun fetch(url: URL, headers: Map<String, String> = emptyMap()): String {
    return fetch(url.toString(), headers)
}

// The helper function that converts the input stream to String
@Throws(IOException::class)
private fun convertInputStreamToString(inS: InputStream): String {
    val bufferedReader = BufferedReader(InputStreamReader(inS))
    val result = StringBuilder()
    var line: String?

    // Read out the input stream buffer line by line until it's empty
    while (bufferedReader.readLine().also { line = it } != null) {
        result.append(line)
    }
    // Close the input stream and return
    inS.close()
    return result.toString()
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
        val urlEncodedValue = URLEncoder.encode(it.value, "UTF-8")
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
