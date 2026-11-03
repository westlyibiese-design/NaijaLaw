package com.westly.naijalaw.core.data

/**
 * All the sections of one law, in file order, with a ready-made search.
 * The lower-case search texts are built once here, so each keystroke is cheap.
 */
class LawIndex(val sections: List<LawSection>) {

    val byId: Map<String, LawSection> = sections.associateBy { it.id }

    // Built once at load time (same order as [sections])
    private val searchText: Array<String> = Array(sections.size) { i ->
        val s = sections[i]
        "${s.number} ${s.displayNumber} ${s.title} ${s.pidgin} ${s.text}".lowercase()
    }
    private val numberLower: Array<String> = Array(sections.size) { i -> sections[i].number.lowercase() }
    private val displayNumberLower: Array<String> = Array(sections.size) { i -> sections[i].displayNumber.lowercase() }
    private val titleLower: Array<String> = Array(sections.size) { i -> sections[i].title.lowercase() }

    /**
     * Search rules:
     *  - the query is trimmed and lower-cased; an empty query returns every section in file order;
     *  - the query is split on spaces; the words section, sec, s, act are ignored only when another word remains;
     *  - a section matches when every remaining word is inside its search text;
     *  - order: (1) number or displayNumber equals a word exactly, (2) title contains all words, (3) the rest;
     *    inside each group the file order is kept.
     */
    fun search(query: String): List<LawSection> {
        val cleaned = query.trim().lowercase()
        if (cleaned.isEmpty()) return sections

        val allWords = cleaned.split(' ').filter { it.isNotEmpty() }
        if (allWords.isEmpty()) return sections
        val meaningful = allWords.filter { it !in FILLER_WORDS }
        val words = if (meaningful.isNotEmpty()) meaningful else allWords

        val exactNumber = ArrayList<LawSection>()
        val titleMatch = ArrayList<LawSection>()
        val others = ArrayList<LawSection>()

        for (i in sections.indices) {
            val text = searchText[i]
            if (!words.all { text.contains(it) }) continue

            val number = numberLower[i]
            val displayNumber = displayNumberLower[i]
            val title = titleLower[i]
            when {
                words.any { it == number || it == displayNumber } -> exactNumber.add(sections[i])
                words.all { title.contains(it) } -> titleMatch.add(sections[i])
                else -> others.add(sections[i])
            }
        }
        return exactNumber + titleMatch + others
    }

    private companion object {
        val FILLER_WORDS = setOf("section", "sec", "s", "act")
    }
}
