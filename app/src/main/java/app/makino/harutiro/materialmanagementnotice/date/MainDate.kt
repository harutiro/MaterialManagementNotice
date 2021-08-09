package app.makino.harutiro.materialmanagementnotice.date

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class MainDate(
    @PrimaryKey open var id: String? = UUID.randomUUID().toString(),
    open var icon: String = "",
    open var mainText: String = "",
    open var tagList: RealmList<TagDateClass>? = null,
    open var memoText: String = "",
    open var dayText: String = "",
    open var archive:Boolean = false,
    open var stockDayList:RealmList<StockDayDate>? =null,
    open var leadTime:Double = 0.0,
    open var alertDay:String = ""
        ): RealmObject()