package id.ac.itera.newsfeed.data

import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

interface NewsRepository {
    fun newsStream(): Flow<News>
    suspend fun fetchBody(news: News): String
    suspend fun fetchAuthor(news: News): String
}

/** Data fiktif, tidak melakukan request internet. Dispatcher dapat diganti saat tes. */
class SimulatedNewsRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NewsRepository {
    private val sequence = AtomicLong(0)
    private val feedFailure = AtomicBoolean(false)
    private val detailFailure = AtomicBoolean(false)

    fun failNextFeed() { feedFailure.set(true) }
    fun failNextDetail() { detailFailure.set(true) }

    // Cold Flow: timer baru bekerja ketika ada collector.
    override fun newsStream(): Flow<News> = flow {
        while (currentCoroutineContext().isActive) {
            delay(2_000)
            if (feedFailure.getAndSet(false)) throw IOException("Aliran berita terputus dalam simulasi.")
            val id = sequence.incrementAndGet()
            emit(samples[((id - 1) % samples.size).toInt()].copy(id = id))
        }
    }.flowOn(ioDispatcher)

    override suspend fun fetchBody(news: News): String = withContext(ioDispatcher) {
        delay(900)
        if (detailFailure.getAndSet(false)) throw IOException("Detail berita gagal dimuat dalam simulasi.")
        news.body
    }

    override suspend fun fetchAuthor(news: News): String = withContext(ioDispatcher) {
        delay(600)
        news.author
    }

    private val samples = listOf(
        article(Category.TECHNOLOGY, "Teknologi kecil, dampak besar", "Mahasiswa merancang aplikasi yang membuat aktivitas harian lebih sederhana.",
            "Sebuah tim mahasiswa mengembangkan prototipe aplikasi pengingat kegiatan dengan tampilan yang mudah digunakan. Mereka memulai dari masalah sederhana: jadwal yang tersebar di banyak tempat.\n\nPrototipe menggabungkan daftar kegiatan, pengingat, dan ringkasan harian. Pengujian awal menitikberatkan pada aksesibilitas dan penggunaan tanpa koneksi internet.", "Nadia Putri"),
        article(Category.CAMPUS, "Ruang belajar yang tumbuh bersama", "Komunitas kampus membuka sesi berbagi pengetahuan untuk semua mahasiswa.",
            "Sesi belajar bersama mempertemukan mahasiswa dari berbagai program studi. Setiap peserta dapat membawa pertanyaan, mendemonstrasikan proyek, atau membantu peserta lain.\n\nKegiatan simulasi ini menggambarkan bagaimana kolaborasi membuat proses belajar lebih terbuka. Topik berganti setiap pekan, mulai dari desain sampai pemrograman.", "Dimas Pratama"),
        article(Category.SPORT, "Satu lapangan, banyak cerita", "Latihan basket antarkelas menjadi tempat membangun kerja sama dan kebiasaan sehat.",
            "Latihan dimulai dengan pemanasan, dilanjutkan latihan teknik dan pertandingan singkat. Setiap peserta mendapat kesempatan bermain dalam tim yang berbeda.\n\nPelatih menekankan bahwa komunikasi dan konsistensi sama pentingnya dengan skor. Peserta juga diajak menyusun target latihan yang realistis.", "Raka Aditya"),
        article(Category.SCIENCE, "Membaca langit dari halaman kampus", "Pengamatan sederhana mengenalkan cara mencatat perubahan cuaca secara teratur.",
            "Kelompok pengamat menggunakan termometer dan catatan harian untuk merekam kondisi lingkungan. Data dikumpulkan pada waktu yang sama agar mudah dibandingkan.\n\nDari kegiatan ini, peserta mempelajari pentingnya pengukuran yang konsisten. Hasil pengamatan disajikan sebagai grafik untuk melihat perubahan dari hari ke hari.", "Alya Safitri"),
        article(Category.TECHNOLOGY, "Aplikasi responsif dimulai dari aliran data", "Proyek kecil memperlihatkan bagaimana berita dapat diperbarui tanpa membekukan layar.",
            "Simulator mengirim data secara bertahap sehingga pengguna tetap bisa membaca, memilih kategori, dan membuka detail. Proses pengambilan data tidak menghentikan interaksi pada layar.\n\nPendekatan ini membantu mahasiswa memahami pemrograman asynchronous melalui contoh yang dekat dengan aplikasi sehari-hari.", "Nadia Putri"),
        article(Category.CAMPUS, "Ide baru bertemu di meja diskusi", "Pameran proyek memberi ruang bagi mahasiswa untuk saling mencoba karya.",
            "Setiap tim menampilkan purwarupa dan menjelaskan masalah yang ingin diselesaikan. Pengunjung dapat mencoba aplikasi, memberi masukan, dan berdiskusi langsung dengan pembuatnya.\n\nCatatan pengunjung digunakan untuk menentukan perbaikan berikutnya. Fokus kegiatan adalah belajar dari umpan balik dan proses iterasi.", "Dimas Pratama"),
        article(Category.SPORT, "Langkah pertama menuju kebiasaan aktif", "Kelompok lari santai mengajak peserta berolahraga sesuai kemampuan masing-masing.",
            "Peserta memilih rute pendek atau panjang setelah melakukan pemanasan bersama. Tidak ada target kecepatan; kegiatan dirancang agar semua orang dapat menikmati prosesnya.\n\nSetelah berlari, peserta berbagi pengalaman dan merencanakan jadwal berikutnya. Istirahat dan hidrasi menjadi bagian dari kegiatan.", "Raka Aditya"),
        article(Category.SCIENCE, "Kebun kecil untuk eksperimen besar", "Pengamatan pertumbuhan tanaman mengenalkan metode eksperimen yang terukur.",
            "Peserta membandingkan pertumbuhan tanaman dengan kondisi pencahayaan yang berbeda. Jumlah air dan jenis media tanam dibuat sama untuk menjaga eksperimen tetap terarah.\n\nSetiap perubahan dicatat dalam jurnal. Kegiatan memperlihatkan bagaimana pertanyaan sederhana dapat diteliti melalui data.", "Alya Safitri"),
    )

    private fun article(category: Category, title: String, summary: String, body: String, author: String) =
        News(0, title, category, summary, body, author)
}
