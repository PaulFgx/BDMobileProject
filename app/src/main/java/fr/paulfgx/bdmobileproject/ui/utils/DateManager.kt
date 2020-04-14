package fr.paulfgx.bdmobileproject.ui.utils

import android.annotation.SuppressLint
import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@SuppressLint("SimpleDateFormat")
fun getCurrentDateTime(): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")
        return current.format(formatter)
    } else {
        val date = Date();
        val formatter = SimpleDateFormat("MMM dd yyyy HH:mma")
        return formatter.format(date)
    }
}