package com.quill.android.delta.delta

import com.quill.android.delta.Delta
import com.quill.android.delta.utils.attrOf
import org.junit.Assert
import org.junit.Test


/**
 * Created by volser on 13.03.18.
 */
class TransformTest {
    @Test
    fun insertPlusInsert() {
        val a1 = Delta().insert("A")
        val b1 = Delta().insert("B")
        val a2 = Delta(a1)
        val b2 = Delta(b1)

        val expected1 = Delta().retain(1).insert("B")
        val expected2 = Delta().insert("B")

        Assert.assertEquals(expected1, a1.transform(b1, true))
        Assert.assertEquals(expected2, a2.transform(b2, false))
    }

    @Test
    fun insertPlusRetain() {
        val a = Delta().insert("A")
        val b = Delta().retain(1, attrOf("bold" to true))

        val expected = Delta().retain(1).retain(1, attrOf("bold" to true))

        Assert.assertEquals(expected, a.transform(b, true))
    }

    @Test
    fun insertPlusDelete() {
        val a = Delta().insert("A")
        val b = Delta().delete(1)

        val expected = Delta().retain(1).delete(1)

        Assert.assertEquals(expected, a.transform(b, true))
    }

    @Test
    fun deletePlusInsert() {
        val a = Delta().delete(1)
        val b = Delta().insert("B")

        val expected = Delta().insert("B")

        Assert.assertEquals(expected, a.transform(b, true))
    }

    @Test
    fun deletePlusRetain() {
        val a = Delta().delete(1)
        val b = Delta().retain(1, attrOf("bold" to true, "color" to "red"))

        val expected = Delta()

        Assert.assertEquals(expected, a.transform(b, true))
    }

    @Test
    fun deletePlusDelete() {
        val a = Delta().delete(1)
        val b = Delta().delete(1)

        val expected = Delta()

        Assert.assertEquals(expected, a.transform(b, true))
    }

    @Test
    fun retainPlusInsert() {
        val a = Delta().retain(1, attrOf("color" to "blue"))
        val b = Delta().insert("B")

        val expected = Delta().insert("B")

        Assert.assertEquals(expected, a.transform(b, true))
    }

    @Test
    fun retainPlusRetain() {
        val a1 = Delta().retain(1, attrOf("color" to "blur"))
        val b1 = Delta().retain(1, attrOf("bold" to true, "color" to "red"))
        val a2 = Delta().retain(1, attrOf("color" to "blur"))
        val b2 = Delta().retain(1, attrOf("bold" to true, "color" to "red"))

        val expected1 = Delta().retain(1, attrOf("bold" to true))
        val expected2 = Delta()

        Assert.assertEquals(expected1, a1.transform(b1, true))
        Assert.assertEquals(expected2, b2.transform(a2, true))
    }

    @Test
    fun retainPlusRetainWithoutPriority() {
        val a1 = Delta().retain(1, attrOf("color" to "blur"))
        val b1 = Delta().retain(1, attrOf("bold" to true, "color" to "red"))
        val a2 = Delta().retain(1, attrOf("color" to "blur"))
        val b2 = Delta().retain(1, attrOf("bold" to true, "color" to "red"))

        val expected1 = Delta().retain(1, attrOf("bold" to true, "color" to "red"))
        val expected2 = Delta().retain(1, attrOf("color" to "blur"))

        Assert.assertEquals(expected1, a1.transform(b1, false))
        Assert.assertEquals(expected2, b2.transform(a2, false))
    }

    @Test
    fun retainPlusDelete() {
        val a = Delta().retain(1, attrOf("color" to "blue"))
        val b = Delta().delete(1)

        val expected = Delta().delete(1)

        Assert.assertEquals(expected, a.transform(b, true))
    }

    @Test
    fun alternatingEdits() {
        val a1 = Delta().retain(2).insert("si").delete(5)
        val b1 = Delta().retain(1).insert("e").delete(5).retain(1).insert("ow")
        val a2 = Delta(a1)
        val b2 = Delta(b1)

        val expected1 = Delta().retain(1).insert("e").delete(1).retain(2).insert("ow")
        val expected2 = Delta().retain(2).insert("si").delete(1)

        Assert.assertEquals(expected1, a1.transform(b1, false))
        Assert.assertEquals(expected2, b2.transform(a2, false))
    }

    @Test
    fun conflictingAppends() {
        val a1 = Delta().retain(3).insert("aa")
        val b1 = Delta().retain(3).insert("bb")
        val a2 = Delta(a1)
        val b2 = Delta(b1)

        val expected1 = Delta().retain(5).insert("bb")
        val expected2 = Delta().retain(3).insert("aa")

        Assert.assertEquals(expected1, a1.transform(b1, true))
        Assert.assertEquals(expected2, b2.transform(a2, false))
    }

    @Test
    fun prependAppends() {
        val a1 = Delta().insert("aa")
        val b1 = Delta().retain(3).insert("bb")
        val a2 = Delta(a1)
        val b2 = Delta(b1)

        val expected1 = Delta().retain(5).insert("bb")
        val expected2 = Delta().insert("aa")

        Assert.assertEquals(expected1, a1.transform(b1, false))
        Assert.assertEquals(expected2, b2.transform(a2, false))
    }

    @Test
    fun trailingDeletesWithDifferingLengths() {
        val a1 = Delta().retain(2).delete(1)
        val b1 = Delta().delete(3)
        val a2 = Delta(a1)
        val b2 = Delta(b1)

        val expected1 = Delta().delete(2)
        val expected2 = Delta()

        Assert.assertEquals(expected1, a1.transform(b1, false))
        Assert.assertEquals(expected2, b2.transform(a2, false))
    }

    @Test
    fun immutability() {
        val a1 = Delta().insert("A")
        val b1 = Delta().insert("B")
        val a2 = Delta().insert("A")
        val b2 = Delta().insert("B")

        val expected = Delta().retain(1).insert("B")

        Assert.assertEquals(expected, a1.transform(b1, true))
        Assert.assertEquals(a1, a2)
        Assert.assertEquals(b1, b2)
    }
}