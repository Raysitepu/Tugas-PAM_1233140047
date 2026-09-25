package id.ac.itera.newsfeed.data

enum class Category(val label: String) {
    TECHNOLOGY("Teknologi"), CAMPUS("Kampus"), SPORT("Olahraga"), SCIENCE("Sains")
}

data class News(
    val id: Long,
    val title: String,
    val category: Category,
    val summary: String,
    val body: String,
    val author: String,
)

data class NewsCard(val news: News, val title: String, val summary: String, val isRead: Boolean)
data class NewsDetail(val news: News, val body: String, val author: String)

/** Extension function: rapikan spasi dan batasi ringkasan untuk kartu UI. */
fun String.toDisplayText(limit: Int = 140): String {
    val clean = trim().replace(Regex("\\s+"), " ")
    return if (clean.length <= limit) clean else clean.take(limit - 1).trimEnd() + "…"
}
