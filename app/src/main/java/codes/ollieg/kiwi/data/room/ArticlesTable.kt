package codes.ollieg.kiwi.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

// index used to speed up searching by title
// composite p.key of wikiId and pageId
// foreign key to Wiki table
@Entity(
    tableName = "Articles",
    indices = [Index(value = ["title"])],
    primaryKeys = ["wikiId", "pageId"],
    foreignKeys = [
        ForeignKey(
            entity = Wiki::class,
            parentColumns = ["id"],
            childColumns = ["wikiId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)

data class Article(
    var wikiId: Long, // the wiki this article belongs to, stored in kiwi
    var pageId: Long, // the page id used by the mediawiki api

    @ColumnInfo(defaultValue = "FALSE") var starred: Boolean = false, // whether the user has added the article to their starred list

    var title: String,
    var parsedSnippet: String? = null, // snippet of the article that doesn't contain html, null if not downloaded yet
    var parsedContent: String? = null, // content of the article that doesn't contain html, null if not downloaded yet

    var thumbnail: ByteArray? = null, // thumbnail of the article, null if not downloaded yet

    var revisionId: Long? = null, // revision id of the article, null if not downloaded yet
    var updateTime: Long? = null, // when the revision id was last checked (in ms since epoch)
)
