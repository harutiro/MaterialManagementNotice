package app.makino.harutiro.materialmanagementnotice.date

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class TagDateClass (
    @PrimaryKey open var id: String? = UUID.randomUUID().toString(),
    open var copyId: String? = ""

    ): RealmObject()