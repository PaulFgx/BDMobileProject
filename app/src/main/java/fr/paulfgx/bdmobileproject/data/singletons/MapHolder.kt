package fr.paulfgx.bdmobileproject.data.singletons

fun getPositionWithFirebaseId(id: String) = MapHolder.mapIdToPosition[id]

object MapHolder {

    var mapIdToPosition = mutableMapOf<String, Int>()
}