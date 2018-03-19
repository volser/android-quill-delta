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

    @Test
    fun objectAttr() {
        val a = Delta().insert("A", attrOf("font" to attrOf("family" to "Helvetica", "size" to "15px")))
        val b = Delta().insert("A", attrOf("font" to attrOf("family" to "Helvetica", "size" to "15px")))
        val expected = Delta()

        Assert.assertEquals(expected, a.diff(b))
    }

    @Test
    fun embedIntegerMatch() {
        val a = Delta().insert(1)
        val b = Delta().insert(1)
        val expected = Delta()

        Assert.assertEquals(expected, a.diff(b))
    }

    @Test
    fun embedIntegerMismatch() {
        val a = Delta().insert(1)
        val b = Delta().insert(2)
        val expected = Delta().delete(1).insert(2)

        Assert.assertEquals(expected, a.diff(b))
    }

    @Test
    fun objectEmbedMatch() {
        val a = Delta().insert(hashMapOf("image" to "http://quilljs.com"))
        val b = Delta().insert(hashMapOf("image" to "http://quilljs.com"))
        val expected = Delta()

        Assert.assertEquals(expected, a.diff(b))
    }

    @Test
    fun objectEmbedMismatch() {
        val a = Delta().insert(hashMapOf("image" to "http://quilljs.com", "alt" to "Overwrite"))
        val b = Delta().insert(hashMapOf("image" to "http://quilljs.com"))
        val expected = Delta().insert(hashMapOf("image" to "http://quilljs.com")).delete(1)

        Assert.assertEquals(expected, a.diff(b))
    }

    @Test
    fun embedObjectChange() {
        val embed = attrOf("image" to "http://quilljs.com")
        val a = Delta().insert(embed)
        embed["image"] = "http://github.com"
        val b = Delta().insert(embed)
        val expected = Delta().insert(hashMapOf("image" to "http://github.com")).delete(1)

        Assert.assertEquals(expected, a.diff(b))
    }

    @Test
    fun embedFalsePositive() {
        val a = Delta().insert(1)
        val b = Delta().insert(Delta.NULL_CHARACTER)
        val expected = Delta().insert(Delta.NULL_CHARACTER).delete(1)

        Assert.assertEquals(expected, a.diff(b))
    }

    @Test
    fun errorNonDocument() {
        val a = Delta().insert("A")
        val b = Delta().retain(1).insert("B")

        try {
            a.diff(b)
            Assert.fail("should be exception here")
        } catch (e : Exception ) { }

        try {
            b.diff(a)
            Assert.fail("should be exception here")
        } catch (e : Exception ) { }
    }

    @Test
    fun inconvenientIndexes() {
        val a = Delta().insert("12", attrOf("bold" to true)).insert("34", attrOf("italic" to true))
        val b = Delta().insert("123", attrOf("color" to "red"))
        val expected = Delta().retain(2, attrOf("bold" to null ,"color" to "red")).retain(1, attrOf("italic" to null, "color" to "red")).delete(1)

        Assert.assertEquals(expected, a.diff(b))
    }

    @Test
    fun combination() {
        val a = Delta().insert("Bad", attrOf("color" to "red")).insert("cat", attrOf("color" to "blue"))
        val b = Delta().insert("Good", attrOf("bold" to true)).insert("dog", attrOf("italic" to true))
        val expected = Delta()
                .insert("Good", attrOf("bold" to true))
                .delete(2)
                .retain(1, attrOf("italic" to true, "color" to null))
                .delete(3)
                .insert("og", attrOf("italic" to true))

        Assert.assertEquals(expected, a.diff(b))
    }

    @Test
    fun sameDocument() {
        val a = Delta().insert("A").insert("B", attrOf("bold" to true))
        val expected = Delta()

        Assert.assertEquals(expected, a.diff(a))
    }

    @Test
    fun immutability() {
        val attr1 = attrOf("color" to "red")
        val attr2 = attrOf("color" to "red")
        val a1 = Delta().insert("A", attr1)
        val a2 = Delta().insert("A", attr1)
        val b1 = Delta().insert("A", attrOf("bold" to true)).insert("B")
        val b2 = Delta().insert("A", attrOf("bold" to true)).insert("B")
        val expected = Delta().retain(1, attrOf("bold" to true, "color" to null)).insert("B")

        Assert.assertEquals(expected, a1.diff(b1))
        Assert.assertEquals(a1, a2)
        Assert.assertEquals(b1, b2)
        Assert.assertEquals(attr1, attr2)
    }

    @Test
    fun nonDocument() {
        val a = Delta().insert("Test")
        val b = Delta().delete(4)

        try {
            a.diff(b)
            Assert.fail("should be exception here")
        } catch (e : Exception ) { }

    }
}