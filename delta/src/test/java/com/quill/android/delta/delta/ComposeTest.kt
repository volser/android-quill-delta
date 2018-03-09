package com.quill.android.delta.delta

import com.quill.android.delta.Delta
import com.quill.android.delta.utils.attrOf
import org.junit.Assert
import org.junit.Test



/**
 * Created by volser on 08.03.18.
 */
class ComposeTest {

    @Test
    fun insertPlusInsert() {
        val a = Delta().insert("A")
        val b = Delta().insert("B")
        val expected = Delta().insert("B").insert("A")

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun insertPlusRetain() {
        val a = Delta().insert("A")
        val b = Delta().retain(1, attrOf("bold" to true, "color" to "red", "font" to null))
        val expected = Delta().insert("A", attrOf("bold" to true, "color" to "red"))

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun insertPlusDelete() {
        val a = Delta().insert("A")
        val b = Delta().delete(1)
        val expected = Delta()

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun deletePlusInsert() {
        val a = Delta().delete(1)
        val b = Delta().insert("B")
        val expected = Delta().insert("B").delete(1)

        Assert.assertEquals(expected, a.compose(b))
    }
}