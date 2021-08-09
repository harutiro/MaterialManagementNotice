package app.makino.harutiro.materialmanagementnotice.date

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class OriginTagDateClass (
    @PrimaryKey open var id: String? = UUID.randomUUID().toString(),
    open var icon:Int = 0,
    open var name:String = "",
    open var color:String = "",
    open var mojiColor:String = "",
    open var copyId: String? = ""

    ): RealmObject()