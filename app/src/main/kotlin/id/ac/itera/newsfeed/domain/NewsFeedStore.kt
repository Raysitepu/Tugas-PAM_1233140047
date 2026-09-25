package id.ac.itera.newsfeed.domain

import id.ac.itera.newsfeed.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface FeedStatus {
    data object Paused : FeedStatus
    data object Running : FeedStatus
    data class Error(val message: String) : FeedStatus
}

sealed interface DetailState {
    data object Closed : DetailState
    data class Loading(val news: News) : DetailState
    data class Ready(val detail: NewsDetail) : DetailState
    data class Error(val news: News, val message: String) : DetailState
}

/** Logika aplikasi bebas Android agar dapat diuji dengan virtual time. */
class NewsFeedStore(private val repository: NewsRepository, private val scope: CoroutineScope) {
    private val history = MutableStateFlow<List<News>>(emptyList())
    private val readIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category.asStateFlow()
    private val _readCount = MutableStateFlow(0)
    val readCount: StateFlow<Int> = _readCount.asStateFlow()
    private val _receivedCount = MutableStateFlow(0)
    val receivedCount: StateFlow<Int> = _receivedCount.asStateFlow()
    private val _status = MutableStateFlow<FeedStatus>(FeedStatus.Paused)
    val status: StateFlow<FeedStatus> = _status.asStateFlow()
    private val _detail = MutableStateFlow<DetailState>(DetailState.Closed)
    val detail: StateFlow<DetailState> = _detail.asStateFlow()
    private var feedJob: Job? = null
    private var detailJob: Job? = null

    // Filter diterapkan pada history agar mengganti kategori langsung menampilkan
    // berita yang telah diterima tanpa memulai ulang sumber Flow.
    val cards: StateFlow<List<NewsCard>> = combine(history, _category, readIds) { news, category, read ->
        news.asFlow()
            .filter { category == null || it.category == category }
            .map { NewsCard(it, it.title.toDisplayText(100), it.summary.toDisplayText(), it.id in read) }
            .toList()
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun selectCategory(category: Category?) { _category.value = category }

    fun start() {
        if (feedJob?.isActive == true) return
        _status.value = FeedStatus.Running
        feedJob = scope.launch {
            repository.newsStream()
                .onEach { _receivedCount.value += 1 } // Side effect, data asli tetap utuh.
                .catch { error ->
                    if (error is CancellationException) throw error
                    _status.value = FeedStatus.Error(error.message ?: "Berita gagal dimuat.")
                }
                .collect { news ->
                    history.update { (listOf(news) + it).take(100) }
                }
        }
    }

    fun pause() {
        feedJob?.cancel()
        feedJob = null
        if (_status.value is FeedStatus.Running) _status.value = FeedStatus.Paused
    }

    fun openDetail(news: News) {
        detailJob?.cancel()
        _detail.value = DetailState.Loading(news)
        detailJob = scope.launch {
            try {
                // Structured concurrency: kedua child dibatalkan jika salah satu gagal.
                val result = coroutineScope {
                    val body = async { repository.fetchBody(news) }
                    val author = async { repository.fetchAuthor(news) }
                    NewsDetail(news, body.await(), author.await())
                }
                readIds.update { it + news.id }
                _readCount.value = readIds.value.size
                _detail.value = DetailState.Ready(result)
            } catch (cancelled: CancellationException) {
                throw cancelled // Pembatalan bukan kegagalan untuk ditampilkan ke pengguna.
            } catch (error: Exception) {
                _detail.value = DetailState.Error(news, error.message ?: "Detail gagal dimuat.")
            }
        }
    }

    fun closeDetail() {
        detailJob?.cancel()
        detailJob = null
        _detail.value = DetailState.Closed
    }
}
