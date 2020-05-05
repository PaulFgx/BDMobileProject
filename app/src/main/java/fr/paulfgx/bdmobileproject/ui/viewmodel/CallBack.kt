package fr.paulfgx.bdmobileproject.ui.viewmodel

typealias OnSuccess<T> = (T) -> Unit
typealias OnFinish = () -> Unit
typealias OnRefresh = () -> Unit
typealias OnRefreshAtPosition = (Int) -> Unit