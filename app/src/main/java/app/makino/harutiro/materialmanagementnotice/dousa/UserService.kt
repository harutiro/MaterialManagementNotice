package app.makino.harutiro.materialmanagementnotice.dousa

import app.makino.harutiro.materialmanagementnotice.date.YahooApiJsonTable
import retrofit2.http.GET
import retrofit2.http.Query

interface UserService {

    @GET("ShoppingWebService/V3/itemSearch?appid=dj00aiZpPWlsTGlLcnVUbkdTbSZzPWNvbnN1bWVyc2VjcmV0Jng9NTc-")
    suspend fun getUser(@Query("jan_code") userId: String): YahooApiJsonTable
}