package codes.ollieg.kiwi.data.room

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

    var title: String,
    var snippetHtml: String? = null, // snippet of the article, null if not downloaded yet
    var contentHtml: String? = null, // html content of the article, null if not downloaded yet
)
