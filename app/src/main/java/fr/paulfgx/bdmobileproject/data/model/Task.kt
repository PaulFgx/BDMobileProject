package fr.paulfgx.bdmobileproject.data.model

import com.google.firebase.database.Exclude
import fr.paulfgx.bdmobileproject.ui.utils.unAccent

data class Task(
    var name: String,
    var isSelected: Boolean,
    var createdAt: String,
    var updatedAt: String,
    @set:Exclude @get:Exclude
    var firebaseId: String,
    @set:Exclude @get:Exclude
    var isExpanded: Boolean = false
) {
    fun matches(value: String): Boolean = name.unAccent().contains(value.unAccent(), ignoreCase = true)
}