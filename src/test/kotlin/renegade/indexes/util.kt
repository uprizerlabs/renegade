package renegade.indexes

import java.util.zip.GZIPInputStream

/**
 * Created by ian on 7/11/17.
 */
object Data {
    fun englishWords(): List<String> {
        return GZIPInputStream(this.javaClass.getResourceAsStream("wiki-100k.txt.gz"))
                .bufferedReader().use {
            it.lineSequence()
                    .filter { !it.startsWith("#") }
                    .toList()
        }
    }
}