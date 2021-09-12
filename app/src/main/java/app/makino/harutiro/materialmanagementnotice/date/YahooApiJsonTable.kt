package app.makino.harutiro.materialmanagementnotice.date

data class YahooApiJsonTable (
    val hits: List<Hits>,
//    val forecasts:List<Forecasts>
        )

data class Hits(
    val name:String,
    val image:Image
)

data class Image(
    val medium: String
)