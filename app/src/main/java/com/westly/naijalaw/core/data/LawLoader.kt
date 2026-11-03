package com.westly.naijalaw.core.data

import android.content.Context
import org.json.JSONObject

/**
 * Reads a law file from the app's assets with Android's built-in org.json.
 * Call it from a background thread only (AppData does that).
 * Throws an exception when the file is missing or wrong; AppData turns that into the Failed state.
 */
object LawLoader {

    private const val ACT_ID_PREFIX = "cca-act-"

    fun load(context: Context, assetPath: String): LawIndex {
        val json = context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(json)
        val total = root.getInt("totalSections")
        val items = root.getJSONArray("sections")
        if (items.length() != total) {
            throw IllegalStateException("totalSections is $total but the file has ${items.length()} sections")
        }

        val list = ArrayList<LawSection>(items.length())
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val id = item.getString("id")
            val number = item.getString("number")
            val chapter = item.getString("chapter")
            val isAct = id.startsWith(ACT_ID_PREFIX)
            list.add(
                LawSection(
                    id = id,
                    number = number,
                    displayNumber = if (isAct) "A$number" else number,
                    title = item.getString("title"),
                    chapter = chapter,
                    chapterNumber = chapterNumberOf(chapter),
                    isAct = isAct,
                    text = item.getString("text"),
                    pidgin = if (item.isNull("pidgin")) "" else item.optString("pidgin", "").trim(),
                ),
            )
        }
        return LawIndex(list)
    }

    /** "Chapter 27: Homicide; ..." gives "27", "Chapter 6A: ..." gives "6A", "The Act" gives null. */
    private fun chapterNumberOf(chapter: String): String? {
        if (!chapter.startsWith("Chapter ")) return null
        val number = chapter.substringAfter("Chapter ").substringBefore(':').trim()
        return number.ifEmpty { null }
    }
}
