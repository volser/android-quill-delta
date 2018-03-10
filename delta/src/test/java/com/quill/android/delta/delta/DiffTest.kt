package com.quill.android.delta.delta

import com.quill.android.delta.Delta
import com.quill.android.delta.utils.attrOf
import org.junit.Assert
import org.junit.Test

/**
 * Created by volser on 10.03.18.
 */
class DiffTest {

    @Test
    fun insert() {
        val a = Delta().insert("A")
        val b = Delta().insert("AB")
        val expected = Delta().retain(1).insert("B")

        Assert.assertEquals(expected, a.diff(b))
    }

    @Test
    fun delete() {
        val a = Delta().insert("AB")
        val b = Delta().insert("A")
        val expected = Delta().retain(1).delete(1)

        Assert.assertEquals(expected, a.diff(b))
    }

    @Test
    fun retain() {
        val a = Delta().insert("A")
        val b = Delta().insert("A")
        val expected = Delta()

        Assert.assertEquals(expected, a.diff(b))
    }

    @Test
    fun format() {
        val a = Delta().insert("A")
        val b = Delta().insert("A", attrOf("bold" to true))
        val expected = Delta().retain(1, attrOf("bold" to true))

        Assert.assertEquals(expected, a.diff(b))
    }
}