package app.makino.harutiro.materialmanagementnotice.dousa

class JapaneseChange {

    fun converted(str: String): String {
        val buf = StringBuffer()
        for (element in str) {
            if (element.code in 0x3041..0x3093) {
                buf.append((element.code + 0x60).toChar())
            } else {
                buf.append(element)
            }
        }
        return buf.toString()
    }
}