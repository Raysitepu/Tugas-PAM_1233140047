package id.ac.itera.newsfeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.ac.itera.newsfeed.data.*
import id.ac.itera.newsfeed.domain.*

private val Ink = Color(0xFF172E28)
private val Green = Color(0xFF176B50)
private val Paper = Color(0xFFF7F8F2)
private val Muted = Color(0xFF69756F)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Green, background = Paper,
                surface = Color.White, onSurface = Ink, onBackground = Ink)) {
                NewsApp()
            }
        }
    }
}

@Composable
private fun NewsApp(vm: NewsViewModel = viewModel()) {
    val store = vm.store
    val cards by store.cards.collectAsStateWithLifecycle()
    val category by store.category.collectAsStateWithLifecycle()
    val received by store.receivedCount.collectAsStateWithLifecycle()
    val read by store.readCount.collectAsStateWithLifecycle()
    val status by store.status.collectAsStateWithLifecycle()
    val detail by store.detail.collectAsStateWithLifecycle()
    var showTools by remember { mutableStateOf(false) }
    LifecycleStartEffect(vm) {
        vm.onForeground()
        onStopOrDispose { vm.onBackground() }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Paper) {
        Column(Modifier.safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = Green) {
                    Text("k.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 26.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("kabar", fontWeight = FontWeight.ExtraBold, fontSize = 23.sp)
                    Text("NEWS FEED SIMULATOR", fontSize = 9.sp, letterSpacing = 1.5.sp, color = Muted)
                }
                TextButton(onClick = { showTools = true }) { Text("Demo") }
            }
            if (detail != DetailState.Closed) {
                BackHandler { store.closeDetail() }
                DetailPage(detail, store::closeDetail, store::openDetail)
            } else {
                LazyColumn(contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                    item { Hero(status, vm::toggleFeed) }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Metric("BERITA MASUK", received.toString(), "selama sesi ini", Modifier.weight(1f))
                            Metric("SUDAH DIBACA", read.toString(), "berita berbeda", Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Pilihan kabarmu", fontSize = 21.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f))
                            Text("${cards.size} berita", fontSize = 12.sp, color = Muted)
                        }
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item { CategoryChip("Semua", category == null) { store.selectCategory(null) } }
                            items(Category.entries) { item ->
                                CategoryChip(item.label, category == item) { store.selectCategory(item) }
                            }
                        }
                    }
                    if (status is FeedStatus.Error) {
                        item { ErrorPanel((status as FeedStatus.Error).message, vm::retryFeed) }
                    }
                    if (cards.isEmpty()) {
                        item {
                            Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
                                Column(Modifier.fillMaxWidth().padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Belum ada kabar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text(if (status is FeedStatus.Running) "Berita baru datang setiap 2 detik.\nTunggu kabar untuk kategori ini."
                                        else "Lanjutkan aliran untuk menerima berita.", color = Muted, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    items(cards, key = { it.news.id }) { card -> NewsItem(card) { store.openDetail(card.news) } }
                    item {
                        Text("Dibuat untuk belajar, dibaca dengan santai.\nSemua berita adalah data simulasi • Maks. 100 berita terbaru.",
                            color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
                    }
                }
            }
        }
    }
    if (showTools) {
        AlertDialog(onDismissRequest = { showTools = false }, title = { Text("Coba penanganan error") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Jalankan simulasi kegagalan, lalu gunakan tombol Coba lagi untuk memulihkannya.")
                    OutlinedButton(onClick = { vm.simulateFeedError(); showTools = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Gagalkan kiriman berita berikutnya")
                    }
                    OutlinedButton(onClick = { vm.simulateDetailError(); showTools = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Gagalkan pemuatan detail berikutnya")
                    }
                    Text("Jika aliran dijeda, lanjutkan dahulu. Error detail terjadi saat berita berikutnya dibuka.", fontSize = 12.sp, color = Muted)
                }
            }, confirmButton = { TextButton(onClick = { showTools = false }) { Text("Tutup") } })
    }
}

@Composable
private fun Hero(status: FeedStatus, toggle: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(
        Brush.linearGradient(listOf(Color(0xFF123F31), Color(0xFF237657))), RoundedCornerShape(24.dp)).padding(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (status is FeedStatus.Running) "●  LIVE FEED" else if (status is FeedStatus.Error) "●  TERPUTUS" else "●  DIJEDA",
                color = Color(0xFFD4F2AA), fontSize = 10.sp, letterSpacing = 1.7.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("EDISI KAMPUS", color = Color(0xFFB4D2C1), fontSize = 9.sp, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text("Dunia bergerak.\nTetap terhubung.", fontSize = 32.sp, lineHeight = 36.sp,
            fontFamily = FontFamily.Serif, color = Color.White)
        Spacer(Modifier.height(10.dp))
        Text("Kabar baru setiap 2 detik.\nPilih topikmu, temukan ceritanya.", color = Color(0xFFD5E6DC), fontSize = 13.sp, lineHeight = 20.sp)
        Spacer(Modifier.height(16.dp))
        Button(onClick = toggle, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1F4BE), contentColor = Ink),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)) {
            Text(if (status is FeedStatus.Running) "Jeda aliran  Ⅱ" else "Lanjutkan aliran  →", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Metric(label: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Color.White, RoundedCornerShape(18.dp))
        .border(1.dp, Color(0xFFE5E9E0), RoundedCornerShape(18.dp)).padding(16.dp)) {
        Text(label, fontSize = 9.sp, letterSpacing = 1.sp, color = Muted, fontWeight = FontWeight.SemiBold)
        Text(value.padStart(2, '0'), fontSize = 31.sp, fontWeight = FontWeight.SemiBold, color = Green)
        Text(subtitle, fontSize = 11.sp, color = Muted)
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label, fontSize = 12.sp) },
        shape = RoundedCornerShape(50), colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Ink, selectedLabelColor = Color.White))
}

@Composable
private fun NewsItem(card: NewsCard, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(card.news.category.label.uppercase(), color = Green, letterSpacing = 1.sp,
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(if (card.isRead) "✓ Dibaca" else "● Baru", fontSize = 10.sp,
                    color = if (card.isRead) Muted else Green)
            }
            Spacer(Modifier.height(10.dp))
            Text(card.title, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 25.sp)
            Spacer(Modifier.height(8.dp))
            Text(card.summary, fontSize = 13.sp, color = Muted, lineHeight = 20.sp)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFEDF0E9))
            Spacer(Modifier.height(12.dp))
            Row {
                Text("KABAR / ${card.news.id.toString().padStart(3, '0')}", fontSize = 10.sp, color = Muted, modifier = Modifier.weight(1f))
                Text("Baca cerita  ↗", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ErrorPanel(message: String, retry: () -> Unit) {
    Surface(color = Color(0xFFFFEDE5), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text("Ada kendala", fontWeight = FontWeight.Bold, color = Color(0xFF9E4426))
            Text(message, fontSize = 13.sp)
            TextButton(onClick = retry) { Text("Coba lagi") }
        }
    }
}

@Composable
private fun DetailPage(state: DetailState, close: () -> Unit, retry: (News) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { TextButton(onClick = close) { Text("←  Kembali ke kabar") } }
        when (state) {
            is DetailState.Loading -> item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text(state.news.title, fontFamily = FontFamily.Serif, fontSize = 30.sp)
                    CircularProgressIndicator(color = Green)
                    Text("Menyiapkan cerita dan profil penulis…", color = Muted)
                }
            }
            is DetailState.Error -> item { ErrorPanel(state.message) { retry(state.news) } }
            is DetailState.Ready -> {
                val news = state.detail.news
                item {
                    Text(news.category.label.uppercase() + "  /  CERITA #${news.id}", fontSize = 11.sp, color = Green, letterSpacing = 1.3.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(news.title, fontFamily = FontFamily.Serif, fontSize = 34.sp, lineHeight = 40.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(news.summary, fontSize = 17.sp, color = Muted, lineHeight = 26.sp)
                }
                item {
                    HorizontalDivider()
                    Text("Oleh ${state.detail.author}  •  ✓ Sudah dibaca", modifier = Modifier.padding(vertical = 16.dp),
                        fontSize = 12.sp, color = Green)
                    HorizontalDivider()
                }
                item { Text(state.detail.body, fontSize = 17.sp, lineHeight = 29.sp) }
                item {
                    Surface(color = Color(0xFFE7EDDF), shape = RoundedCornerShape(14.dp)) {
                        Text("Tentang cerita ini\nBerita dan nama penulis merupakan data fiktif untuk keperluan praktikum.",
                            modifier = Modifier.padding(18.dp), fontSize = 12.sp, color = Muted)
                    }
                }
                item { Button(onClick = close, modifier = Modifier.fillMaxWidth()) { Text("Jelajahi kabar lainnya") } }
            }
            DetailState.Closed -> Unit
        }
    }
}
