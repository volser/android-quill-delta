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
        val b = Delta().retain(1, attrOf(("bold" to true) as Pair<String, Any?>, "color" to "red", "font" to null))
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

    @Test
    fun deletePlusRetain() {
        val a = Delta().delete(1)
        val b = Delta().retain(1, attrOf("bold" to true, "color" to "red"))
        val expected = Delta().delete(1).retain(1, attrOf("bold" to true, "color" to "red"))

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun deletePlusDelete() {
        val a = Delta().delete(1)
        val b = Delta().delete(1)
        val expected = Delta().delete(2)

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun retainPlusInsert() {
        val a = Delta().retain(1, attrOf("color" to "blue"))
        val b = Delta().insert("B")
        val expected = Delta().insert("B").retain(1, attrOf("color" to "blue"))

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun retainPlusRetain() {
        val a = Delta().retain(1, attrOf("color" to "blue"))
        val b = Delta().retain(1, attrOf("bold" to true, "color" to "red", "font" to null))
        val expected = Delta().retain(1, attrOf("bold" to true, "color" to "red", "font" to null))

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun retainPlusDelete() {
        val a = Delta().retain(1, attrOf("color" to "blue"))
        val b = Delta().delete(1)
        val expected = Delta().delete(1)

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun insertInMiddle() {
        val a = Delta().insert("Hello")
        val b = Delta().retain(3).insert("X")
        val expected = Delta().insert("HelXlo")

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun insertAndDeleteOrdering() {
        val a = Delta().insert("Hello")
        val b = Delta().insert("Hello")
        val insertFirst = Delta().retain(3).insert("X").delete(1)
        val deleteFirst = Delta().retain(3).delete(1).insert("X")
        val expected = Delta().insert("HelXo")

        Assert.assertEquals(expected, a.compose(insertFirst))
        Assert.assertEquals(expected, b.compose(deleteFirst))
    }

    @Test
    fun insertEmbed() {
        val a = Delta().insert(1, attrOf("src" to "http://quilljs.com/image.png"))
        val b = Delta().retain(1, attrOf("alt" to "logo"))
        val expected = Delta().insert(1, attrOf("src" to "http://quilljs.com/image.png", "alt" to "logo"))

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun deleteEntireText() {
        val a = Delta().retain(4).insert("Hello")
        val b = Delta().delete(9)
        val expected = Delta().delete(4)

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun retainMoreThanLength() {
        val a = Delta().insert("Hello")
        val b = Delta().retain(10)
        val expected = Delta().insert("Hello")

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun retainEmptyEmbed() {
        val a = Delta().insert(1)
        val b = Delta().retain(1)
        val expected = Delta().insert(1)

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun removeAllAttr() {
        val a = Delta().insert("A", attrOf("bold" to true))
        val b = Delta().retain(1, attrOf("bold" to null))
        val expected = Delta().insert("A")

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun removeAllEmbedAttr() {
        val a = Delta().insert(2, attrOf("bold" to true))
        val b = Delta().retain(1, attrOf("bold" to null))
        val expected = Delta().insert(2)

        Assert.assertEquals(expected, a.compose(b))
    }

    @Test
    fun immutability() {
        val attr1 = attrOf("bold" to true)
        val attr2 = attrOf("bold" to true)
        val a1 = Delta().insert("Test", attr1)
        val a2 = Delta().insert("Test", attr1)
        val b1 = Delta().retain(1, attrOf("color" to "red")).delete(2)
        val b2 = Delta().retain(1, attrOf("color" to "red")).delete(2)

        val expected = Delta().insert("T", attrOf("color" to "red", "bold" to true)).insert("t", attr1)

        Assert.assertEquals(expected, a1.compose(b1))
        Assert.assertEquals(a1, a2)
        Assert.assertEquals(b1, b2)
        Assert.assertEquals(attr1, attr2)
    }
}