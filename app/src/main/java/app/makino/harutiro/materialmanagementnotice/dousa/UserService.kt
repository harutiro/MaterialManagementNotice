package app.makino.harutiro.materialmanagementnotice.dousa

import app.makino.harutiro.materialmanagementnotice.date.YahooApiJsonTable
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {

    @GET("api/forecast/city/{userId}")
    suspend fun getUser(@Path("userId") userId: String): YahooApiJsonTable
}