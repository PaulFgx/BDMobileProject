package fr.paulfgx.bdmobileproject.ui.utils

import android.annotation.SuppressLint
import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@SuppressLint("SimpleDateFormat")
fun getCurrentDateTime(): String {
    val date = Date();
    val formatter = SimpleDateFormat("MMM dd yyyy HH:mma")
    return formatter.format(date)
}