package com.quill.android.delta.delta

import com.quill.android.delta.Delta
import com.quill.android.delta.utils.attrOf
import kotlinx.serialization.json.JSON
import org.junit.Assert
import org.junit.Test

/**
 * Created by volser on 16.03.18.
 */
class HelpersTest {

    @Test
    fun concat() {
        val delta = Delta().insert("Test")
        val concat = Delta()
        val expected = Delta().insert("Test")

        Assert.assertEquals(expected, delta.concat(concat))
    }

    @Test
    fun unmergeable() {
        val delta = Delta().insert("Test")
        val original = JSON.parse<Delta>(JSON.stringify(delta))
        val concat = Delta().insert("!", attrOf("bold" to true))
        val expected = Delta().insert("Test").insert("!", attrOf("bold" to true))

        Assert.assertEquals(expected, delta.concat(concat))
        Assert.assertEquals(original, delta)
    }

    @Test
    fun mergeable() {
        val delta = Delta().insert("Test", attrOf("bold" to true))
        val original = JSON.parse<Delta>(JSON.stringify(delta))

        val concat = Delta().insert("!", attrOf("bold" to true)).insert("/n")
        val expected = Delta().insert("Test!", attrOf("bold" to true)).insert("/n")

        Assert.assertEquals(expected, delta.concat(concat))
        Assert.assertEquals(original, delta)
    }
}