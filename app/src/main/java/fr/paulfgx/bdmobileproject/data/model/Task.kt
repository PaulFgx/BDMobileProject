package fr.paulfgx.bdmobileproject.data.model

import com.google.firebase.database.Exclude

data class Task(
    var name: String,
    var isSelected: Boolean,
    @set:Exclude @get:Exclude
    var firebaseId: String
)