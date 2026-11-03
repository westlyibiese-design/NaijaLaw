package com.westly.naijalaw.core.data

/** The laws shown on the Home screen, in this order. */
object LawCatalog {

    const val CRIMINAL_CODE_ID = "criminal-code-act"
    const val CRIMINAL_CODE_ASSET = "laws/criminal_code_sections.json"

    val laws: List<LawInfo> = listOf(
        LawInfo(
            id = CRIMINAL_CODE_ID,
            name = "Criminal Code Act",
            shortName = "Criminal Code",
            iconKey = "scales",
            assetPath = CRIMINAL_CODE_ASSET,
            available = true,
        ),
        LawInfo(
            id = "penal-code",
            name = "Penal Code",
            shortName = "Penal Code",
            iconKey = "gavel",
            assetPath = null,
            available = false,
        ),
        LawInfo(
            id = "acja-2015",
            name = "ACJA 2015",
            shortName = "ACJA 2015",
            iconKey = "document",
            assetPath = null,
            available = false,
        ),
        LawInfo(
            id = "evidence-act",
            name = "Evidence Act",
            shortName = "Evidence Act",
            iconKey = "magnifier",
            assetPath = null,
            available = false,
        ),
    )

    fun byId(id: String): LawInfo? = laws.firstOrNull { it.id == id }
}
