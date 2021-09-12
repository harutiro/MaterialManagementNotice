package app.makino.harutiro.materialmanagementnotice.date

import com.google.gson.annotations.SerializedName

data class YahooApiJsonTable (
    val name: String,
    //jsonからデータを持ってくるときに、変数名と名前が同じ時に持ってこれるけど、好きな名前を使いたいときに以下の通りにする
    @SerializedName("login") val userId: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    val following: Int,
    val followers: Int
        )