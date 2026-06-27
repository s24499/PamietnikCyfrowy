package pjatk.prm.pamietnikcyfrowy.data

fun mapPreviewUrl(
    lat: Double,
    lon: Double,
    zoom: Int = 13,
    width: Int = 240,
    height: Int = 140
): String {
    return "https://static-maps.yandex.ru/1.x/" +
            "?lang=en_US" +
            "&l=map" +
            "&z=$zoom" +
            "&size=${width},${height}" +
            "&ll=$lon,$lat" +
            "&pt=$lon,$lat,pm2rdm"
}