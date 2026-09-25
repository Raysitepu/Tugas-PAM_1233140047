package id.ac.itera.newsfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ac.itera.newsfeed.data.SimulatedNewsRepository
import id.ac.itera.newsfeed.domain.FeedStatus
import id.ac.itera.newsfeed.domain.NewsFeedStore

class NewsViewModel : ViewModel() {
    private val repository = SimulatedNewsRepository()
    val store = NewsFeedStore(repository, viewModelScope)
    private var manuallyPaused = false

    fun onForeground() {
        if (!manuallyPaused && store.status.value !is FeedStatus.Error) store.start()
    }
    fun onBackground() { store.pause() }
    fun toggleFeed() {
        manuallyPaused = store.status.value is FeedStatus.Running
        if (manuallyPaused) store.pause() else store.start()
    }
    fun retryFeed() { manuallyPaused = false; store.start() }
    fun simulateFeedError() { repository.failNextFeed() }
    fun simulateDetailError() { repository.failNextDetail() }
}
