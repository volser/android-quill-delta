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

    val delta = Delta().insert("Hello").insert(hashMapOf("image" to true)).insert("World!")

    @Test
    fun concat() {
        val delta = Delta().insert("Test")
        val concat = Delta()
        val expected = Delta().insert("Test")

        Assert.assertEquals(expected, delta.concat(concat))
    }

    @Test
    fun concatUnmergeable() {
        val delta = Delta().insert("Test")
        val original = JSON.parse<Delta>(JSON.stringify(delta))
        val concat = Delta().insert("!", attrOf("bold" to true))
        val expected = Delta().insert("Test").insert("!", attrOf("bold" to true))

        Assert.assertEquals(expected, delta.concat(concat))
        Assert.assertEquals(original, delta)
    }

    @Test
    fun concatMergeable() {
        val delta = Delta().insert("Test", attrOf("bold" to true))
        val original = JSON.parse<Delta>(JSON.stringify(delta))

        val concat = Delta().insert("!", attrOf("bold" to true)).insert("/n")
        val expected = Delta().insert("Test!", attrOf("bold" to true)).insert("/n")

        Assert.assertEquals(expected, delta.concat(concat))
        Assert.assertEquals(original, delta)
    }

    @Test
    fun chopRetain() {
        val delta = Delta().insert("Test").retain(4)
        val expected = Delta().insert("Test")

        Assert.assertEquals(expected, delta.chop())
    }

    @Test
    fun chopInsert() {
        val delta = Delta().insert("Test")
        val expected = Delta().insert("Test")

        Assert.assertEquals(expected, delta.chop())
    }

    @Test
    fun chopRetainFormatted() {
        val delta = Delta().insert("Test").retain(4, attrOf("bold" to true))
        val expected = Delta().insert("Test").retain(4, attrOf("bold" to true))

        Assert.assertEquals(expected, delta.chop())
    }

    @Test
    fun filter() {
        val arr = delta.filter { it.insert is String }

        Assert.assertEquals(2, arr.size)
    }

    @Test
    fun map() {
        val arr = delta.map { if (it.insert is String) it.insert else "" }

        Assert.assertEquals(listOf("Hello", "", "World!"), arr)
    }

    @Test
    fun partition() {
        val arr = delta.partition { it.insert is String }

        Assert.assertEquals(listOf(delta.ops[0], delta.ops[2]), arr.first)
        Assert.assertEquals(listOf(delta.ops[1]), arr.second)
    }

    @Test
    fun lengthDocument() {
        val delta = Delta().insert("AB", attrOf("bold" to true)).insert(1)

        Assert.assertEquals(3, delta.length())
    }

    @Test
    fun lengthMixed() {
        val delta = Delta().insert("AB", attrOf("bold" to true)).insert(1).retain(2, attrOf("bold" to null)).delete(1)

        Assert.assertEquals(6, delta.length())
    }

    @Test
    fun changeLengthMixed() {
        val delta = Delta().insert("AB", attrOf("bold" to true)).retain(2, attrOf("bold" to null)).delete(1)

        Assert.assertEquals(1, delta.changeLength())
    }

    @Test
    fun sliceStart() {
        val slice = Delta().retain(2).insert("A").slice(2)
        val expected = Delta().insert("A")

        Assert.assertEquals(expected, slice)
    }

    @Test
    fun sliceStartAndEndChop() {
        val slice = Delta().insert("0123456789").slice(2, 7)
        val expected = Delta().insert("23456")

        Assert.assertEquals(expected, slice)
    }

    @Test
    fun sliceStartAndEndMultiChop() {
        val slice = Delta().insert("0123", attrOf("bold" to true)).insert("4567").slice(3, 5)
        val expected = Delta().insert("3", attrOf("bold" to true)).insert("4")

        Assert.assertEquals(expected, slice)
    }

    @Test
    fun sliceStartAndEnd() {
        val slice = Delta().retain(2).insert("A", attrOf("bold" to true)).insert("B").slice(2, 3)
        val expected = Delta().insert("A", attrOf("bold" to true))

        Assert.assertEquals(expected, slice)
    }

    @Test
    fun sliceNoParams() {
        val delta = Delta().retain(2).insert("A", attrOf("bold" to true)).insert("B")
        val expected = delta.slice()

        Assert.assertEquals(expected, delta)
    }

    @Test
    fun sliceSplitOps() {
        val slice = Delta().insert("AB", attrOf("bold" to true)).insert("C").slice(1, 2)
        val expected = Delta().insert("B", attrOf("bold" to true))

        Assert.assertEquals(expected, slice)
    }

    @Test
    fun sliceSplitOpsMultiTimes() {
        val slice = Delta().insert("ABC", attrOf("bold" to true)).insert("D").slice(1, 2)
        val expected = Delta().insert("B", attrOf("bold" to true))

        Assert.assertEquals(expected, slice)
    }
}