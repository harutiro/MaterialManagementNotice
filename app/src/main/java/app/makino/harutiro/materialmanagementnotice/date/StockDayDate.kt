package app.makino.harutiro.materialmanagementnotice.date

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class StockDayDate (
    @PrimaryKey open var id: String? = UUID.randomUUID().toString(),
    open var day:String = "",
    open var state:String = "",
    open var interval:Long = 0L

): RealmObject()