package id.ac.itera.newsfeed

import id.ac.itera.newsfeed.data.*
import id.ac.itera.newsfeed.domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsFeedTest {
    private data class Fixture(val repository: SimulatedNewsRepository, val store: NewsFeedStore)
    private fun TestScope.fixture(): Fixture {
        val repository = SimulatedNewsRepository(StandardTestDispatcher(testScheduler))
        return Fixture(repository, NewsFeedStore(repository, backgroundScope))
    }

    @Test fun `cold flow emits only after collection and every two seconds`() = runTest {
        val repository = SimulatedNewsRepository(StandardTestDispatcher(testScheduler))
        val stream = repository.newsStream()
        advanceTimeBy(10_000) // Chua collect: chua tao tin nao.
        val result = async { stream.take(3).toList() }
        runCurrent()
        advanceTimeBy(1_999)
        assertFalse(result.isCompleted)
        advanceTimeBy(4_001)
        runCurrent()
        assertEquals(listOf(1L, 2L, 3L), result.await().map { it.id })
        assertEquals(16_000L, currentTime)
    }

    @Test fun `start is idempotent and pause stops new emissions`() = runTest {
        val (_, store) = fixture()
        store.start()
        store.start()
        runCurrent()
        advanceTimeBy(2_000); runCurrent()
        assertEquals(1, store.receivedCount.value)
        store.pause()
        advanceTimeBy(10_000); runCurrent()
        assertEquals(1, store.receivedCount.value)
        assertEquals(FeedStatus.Paused, store.status.value)
        store.start()
        runCurrent()
        advanceTimeBy(2_000); runCurrent()
        assertEquals(listOf(2L, 1L), store.cards.value.map { it.news.id })
    }

    @Test fun `category changes filter existing news without restarting producer`() = runTest {
        val (_, store) = fixture()
        store.start(); runCurrent()
        advanceTimeBy(8_000); runCurrent()
        assertEquals(4, store.cards.value.size)
        store.selectCategory(Category.SCIENCE); runCurrent()
        assertEquals(listOf(Category.SCIENCE), store.cards.value.map { it.news.category })
        store.selectCategory(null); runCurrent()
        assertEquals(4, store.cards.value.size)
        assertEquals(4, store.receivedCount.value)
    }

    @Test fun `selected category is applied to new arrivals too`() = runTest {
        val (_, store) = fixture()
        store.selectCategory(Category.TECHNOLOGY)
        store.start(); runCurrent()
        advanceTimeBy(10_000); runCurrent()
        assertEquals(listOf(5L, 1L), store.cards.value.map { it.news.id })
        assertEquals(5, store.receivedCount.value)
    }

    @Test fun `detail uses parallel children and counts unique successful reads`() = runTest {
        val (_, store) = fixture()
        store.start(); runCurrent()
        advanceTimeBy(2_000); runCurrent()
        store.pause()
        val news = store.cards.value.single().news
        val started = currentTime
        store.openDetail(news); runCurrent()
        assertTrue(store.detail.value is DetailState.Loading)
        assertEquals(0, store.readCount.value)
        advanceTimeBy(899); runCurrent()
        assertTrue(store.detail.value is DetailState.Loading)
        advanceTimeBy(1); runCurrent()
        assertTrue(store.detail.value is DetailState.Ready)
        assertEquals(900L, currentTime - started) // Paralel: max(900,600), bukan 1500 ms.
        assertEquals(1, store.readCount.value)
        assertTrue(store.cards.value.single().isRead)
        store.closeDetail()
        store.openDetail(news); runCurrent()
        advanceTimeBy(900); runCurrent()
        assertEquals(1, store.readCount.value)
    }

    @Test fun `reading different articles increments the count`() = runTest {
        val (_, store) = fixture()
        store.start(); runCurrent()
        advanceTimeBy(4_000); runCurrent()
        store.pause()
        store.cards.value.forEach { card ->
            store.openDetail(card.news); runCurrent()
            advanceTimeBy(900); runCurrent()
        }
        assertEquals(2, store.readCount.value)
        assertTrue(store.cards.value.all { it.isRead })
    }

    @Test fun `failed detail does not count as read and can be retried`() = runTest {
        val (repository, store) = fixture()
        store.start(); runCurrent()
        advanceTimeBy(2_000); runCurrent()
        store.pause()
        val news = store.cards.value.single().news
        repository.failNextDetail()
        store.openDetail(news); runCurrent()
        advanceTimeBy(900); runCurrent()
        assertTrue(store.detail.value is DetailState.Error)
        assertEquals(0, store.readCount.value)
        store.openDetail(news); runCurrent()
        advanceTimeBy(900); runCurrent()
        assertTrue(store.detail.value is DetailState.Ready)
        assertEquals(1, store.readCount.value)
    }

    @Test fun `closing a pending detail cancels it without counting or showing error`() = runTest {
        val (_, store) = fixture()
        store.start(); runCurrent()
        advanceTimeBy(2_000); runCurrent()
        store.pause()
        store.openDetail(store.cards.value.single().news); runCurrent()
        advanceTimeBy(200)
        store.closeDetail()
        advanceTimeBy(2_000); runCurrent()
        assertEquals(DetailState.Closed, store.detail.value)
        assertEquals(0, store.readCount.value)
    }

    @Test fun `replacing pending detail prevents stale result`() = runTest {
        val (_, store) = fixture()
        store.start(); runCurrent()
        advanceTimeBy(4_000); runCurrent()
        store.pause()
        val news = store.cards.value.map { it.news }
        store.openDetail(news[0]); runCurrent()
        advanceTimeBy(200)
        store.openDetail(news[1]); runCurrent()
        advanceTimeBy(900); runCurrent()
        assertEquals(news[1].id, (store.detail.value as DetailState.Ready).detail.news.id)
        assertEquals(1, store.readCount.value)
    }

    @Test fun `feed catch preserves history and retry continues with unique ids`() = runTest {
        val (repository, store) = fixture()
        store.start(); runCurrent()
        advanceTimeBy(2_000); runCurrent()
        repository.failNextFeed()
        advanceTimeBy(2_000); runCurrent()
        assertTrue(store.status.value is FeedStatus.Error)
        assertEquals(1, store.cards.value.size)
        assertEquals(1, store.receivedCount.value)
        store.start(); runCurrent()
        advanceTimeBy(2_000); runCurrent()
        assertEquals(FeedStatus.Running, store.status.value)
        assertEquals(listOf(2L, 1L), store.cards.value.map { it.news.id })
    }

    @Test fun `feed retains only newest hundred while total remains accurate`() = runTest {
        val (_, store) = fixture()
        store.start(); runCurrent()
        advanceTimeBy(204_000); runCurrent()
        assertEquals(102, store.receivedCount.value)
        assertEquals(100, store.cards.value.size)
        assertEquals(102L, store.cards.value.first().news.id)
        assertEquals(3L, store.cards.value.last().news.id)
    }

    @Test fun `display transformation cleans whitespace and truncates long summaries`() {
        assertEquals("Kabar hari ini", "  Kabar\n hari   ini  ".toDisplayText())
        val summary = "a".repeat(200).toDisplayText()
        assertEquals(140, summary.length)
        assertTrue(summary.endsWith("…"))
    }
}
