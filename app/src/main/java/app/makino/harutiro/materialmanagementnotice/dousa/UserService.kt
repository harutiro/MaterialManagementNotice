package app.makino.harutiro.materialmanagementnotice.dousa

import app.makino.harutiro.materialmanagementnotice.date.YahooApiJsonTable
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {

    @GET("user/{janCode}")
    suspend fun getUser(@Path("janCode")userId: String):YahooApiJsonTable
}