package fr.paulfgx.bdmobileproject.data.model

import com.google.firebase.database.Exclude

data class Task(
    var name: String,
    var isSelected: Boolean,
    var createdAt: String,
    var updatedAt: String,
    @set:Exclude @get:Exclude
    var firebaseId: String
)