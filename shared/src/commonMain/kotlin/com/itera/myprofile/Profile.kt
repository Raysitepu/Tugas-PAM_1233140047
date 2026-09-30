package com.itera.myprofile

/** Semua data awal profil ada di sini agar mudah disesuaikan sebelum dikumpulkan. */
data class Profile(
    val name: String,
    val bio: String,
    val email: String,
    val phone: String,
    val location: String,
)

// Data awal profil. Foto pribadi dapat diganti di composeResources/drawable.
val sampleProfile = Profile(
    name = "Ray Regan Sitepu",
    bio = "Mahasiswa Informatika ITERA yang antusias mempelajari mobile application development",
    email = "ray.123140047@student.itera.ac.id",
    phone = "082277520008",
    location = "Lampung Selatan, Indonesia",
)
