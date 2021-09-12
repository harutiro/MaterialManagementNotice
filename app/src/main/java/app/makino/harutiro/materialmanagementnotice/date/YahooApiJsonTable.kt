package app.makino.harutiro.materialmanagementnotice.date

data class YahooApiJsonTable (
    val title: String,
    val forecasts:List<Forecasts>
        )

data class Forecasts(
    val telop:String
)