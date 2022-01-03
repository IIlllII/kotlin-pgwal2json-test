import org.junit.Test

class TestClass {

    @Test
    fun testByteConversion() {
        val bytes  ="7570646174652077616920776169".chunked(2) {
            val oct = it.map { num -> num.digitToInt(16) }
            (16*oct[0] + oct[1]).toUByte().toByte()
        }
        println(String(bytes.toByteArray(), Charsets.UTF_8))
    }
}