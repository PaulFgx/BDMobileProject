package fr.paulfgx.bdmobileproject.ui.viewmodel

enum class Event { INSERT, UPDATE, DELETE, FIRST_LOADING }

typealias OnSuccess<T> = (T) -> Unit
typealias OnFinish = () -> Unit
typealias OnRefresh = () -> Unit
typealias OnRefreshAtPosition = (Int) -> Unit
typealias OnEventAtPosition = (Int, Event) -> Unit