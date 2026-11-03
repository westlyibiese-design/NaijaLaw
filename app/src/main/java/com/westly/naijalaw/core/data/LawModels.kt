package com.westly.naijalaw.core.data

/** One section of a law. Phases 5 to 9 read this. */
data class LawSection(
    val id: String,            // "cca-316", "cca-62a", "cca-act-1"
    val number: String,        // "316", "62A"
    val displayNumber: String, // same as number, but "A1", "A1A" for the Act's own sections
    val title: String,
    val chapter: String,       // "Chapter 27: Homicide; ..." or "The Act"
    val chapterNumber: String?,// "27", "6A", or null for "The Act"
    val isAct: Boolean,        // true when id starts with "cca-act-"
    val text: String,          // original law text, paragraphs separated by "\n"
    val pidgin: String,        // easy Pidgin explanation (never null in the app)
)

/** One law shown on the Home screen. */
data class LawInfo(
    val id: String,
    val name: String,
    val shortName: String,
    val iconKey: String,       // "scales", "gavel", "document", "magnifier"
    val assetPath: String?,    // "laws/criminal_code_sections.json" or null
    val available: Boolean,
)

/** The state of loading the law file. */
sealed interface LawLoadState {
    data object Loading : LawLoadState
    class Ready(val index: LawIndex) : LawLoadState
    data object Failed : LawLoadState
}
