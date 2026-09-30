package com.itera.myprofile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import myprofileapp.shared.generated.resources.Res
import myprofileapp.shared.generated.resources.avatar
import org.jetbrains.compose.resources.painterResource

private val Purple = Color(0xFF67509D)
private val Blue = Color(0xFF2400E8)
private val Ink = Color(0xFF171717)
private val Muted = Color(0xFF858585)
private val Background = Color(0xFFFAFAFA)
private val ContactBackground = Color(0xFFE6E1EB)

private val ProfileSaver = listSaver<Profile, String>(
    save = { listOf(it.name, it.bio, it.email, it.phone, it.location) },
    restore = { Profile(it[0], it[1], it[2], it[3], it[4]) },
)

@Composable
fun App() {
    var profile by rememberSaveable(stateSaver = ProfileSaver) { mutableStateOf(sampleProfile) }
    var editing by rememberSaveable { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Purple, onPrimary = Color.White, background = Background,
            surface = Background, onSurface = Ink, onBackground = Ink,
            surfaceVariant = ContactBackground, onSurfaceVariant = Muted,
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            ProfileScreen(profile = profile, onEdit = { editing = true })
        }
        if (editing) {
            EditProfileDialog(
                profile = profile,
                onDismiss = { editing = false },
                onSave = { profile = it; editing = false },
            )
        }
    }
}

/** Satu kolom sederhana; bisa digulir pada layar kecil atau ukuran font besar. */
@Composable
fun ProfileScreen(profile: Profile, onEdit: () -> Unit) {
    val contactVisible = remember { MutableTransitionState(false).apply { targetState = true } }
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileHeader(profile)
            Spacer(Modifier.height(26.dp))
            // Bonus tugas: kartu kontak masuk dengan animasi saat halaman dibuka.
            AnimatedVisibility(
                visibleState = contactVisible,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            ) {
                ProfileCard {
                    InfoItem(ProfileIcons.Mail, "Email", profile.email)
                    InfoItem(ProfileIcons.Phone, "Phone", profile.phone)
                    InfoItem(ProfileIcons.Location, "Location", profile.location)
                }
            }
            Spacer(Modifier.height(26.dp))
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = CircleShape,
            ) {
                Text("Edit Profile", fontSize = 16.sp, modifier = Modifier.padding(vertical = 3.dp))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** Header reusable dengan avatar melingkar, nama, dan bio rata tengah. */
@Composable
fun ProfileHeader(profile: Profile, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(144.dp).background(Color(0xFFE7F2FB), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.avatar),
                contentDescription = "Avatar profil ${profile.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(124.dp).border(2.dp, Blue, CircleShape)
                    .padding(2.dp).clip(CircleShape),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(profile.name, fontSize = 24.sp, fontWeight = FontWeight.Bold,
            color = Ink, textAlign = TextAlign.Center, lineHeight = 30.sp)
        Spacer(Modifier.height(18.dp))
        Text(profile.bio.ifBlank { "Belum ada bio." }, fontSize = 16.sp,
            color = Muted, textAlign = TextAlign.Center, lineHeight = 24.sp)
    }
}

/** Kartu reusable: isi diterima melalui slot composable. */
@Composable
fun ProfileCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ContactBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            content = content,
        )
    }
}

/** Ikon di kiri, label dan nilai di kanan. Nilai panjang dapat membungkus. */
@Composable
fun InfoItem(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Blue, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, fontSize = 12.sp, color = Muted)
            Text(value.ifBlank { "Belum diisi" }, fontSize = 16.sp,
                fontWeight = FontWeight.Medium, color = Ink, lineHeight = 23.sp)
        }
    }
}

@Composable
private fun EditProfileDialog(profile: Profile, onDismiss: () -> Unit, onSave: (Profile) -> Unit) {
    var name by rememberSaveable { mutableStateOf(profile.name) }
    var bio by rememberSaveable { mutableStateOf(profile.bio) }
    var email by rememberSaveable { mutableStateOf(profile.email) }
    var phone by rememberSaveable { mutableStateOf(profile.phone) }
    var location by rememberSaveable { mutableStateOf(profile.location) }
    val validEmail = email.trim().matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
    val valid = name.isNotBlank() && validEmail

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit profil", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Perubahan berlaku selama aplikasi terbuka.", fontSize = 12.sp, color = Muted)
                OutlinedTextField(name, { name = it }, label = { Text("Nama") },
                    singleLine = true, isError = name.isBlank(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(bio, { bio = it }, label = { Text("Bio singkat") },
                    minLines = 3, maxLines = 5, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(email, { email = it }, label = { Text("Email") },
                    singleLine = true, isError = !validEmail,
                    supportingText = { if (!validEmail) Text("Masukkan alamat email yang valid.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(phone, { phone = it }, label = { Text("Nomor telepon (opsional)") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(location, { location = it }, label = { Text("Lokasi (opsional)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(enabled = valid, onClick = {
                onSave(profile.copy(name = name.trim(), bio = bio.trim(), email = email.trim(),
                    phone = phone.trim(), location = location.trim()))
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
