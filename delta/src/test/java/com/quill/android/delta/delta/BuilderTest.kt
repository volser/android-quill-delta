package com.quill.android.delta.delta

import com.quill.android.delta.OpAttributes
import com.quill.android.delta.Op
import org.junit.Assert
import org.junit.Test
import com.quill.android.delta.Delta
import com.quill.android.delta.utils.attrOf


/**
 * Created by volser on 07.03.18.
 */
class BuilderTest {

    private var ops = mutableListOf(
            Op.insertOp("abc"),
            Op.retainOp(1, attrOf("color" to "red")),
            Op.deleteOp(4),
            Op.insertOp("def", attrOf("bold" to true)),
            Op.retainOp(6)
    )


    @Test
    fun empty() {
        val delta = Delta()
        Assert.assertNotNull(delta.ops)
        Assert.assertEquals(0, delta.ops.size)
    }

    @Test
    fun emptyOps() {
        val delta = Delta().insert("").delete(0).retain(0)
        Assert.assertNotNull(delta.ops)
        Assert.assertEquals(0, delta.ops.size)
    }

    @Test
    fun arrayOfOps() {
        val delta = Delta(ops)
        Assert.assertEquals(ops, delta.ops)
    }

    @Test
    fun delta() {
        val original = Delta(ops)
        val delta = Delta(original)
        Assert.assertEquals(original.ops, delta.ops)
        Assert.assertEquals(ops, delta.ops)
    }

    @Test
    fun insertText() {
        val delta = Delta().insert("test")
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.insertOp("test"), delta.ops[0])
    }

    @Test
    fun insertTextNull() {
        val delta = Delta().insert("test", null)
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.insertOp("test"), delta.ops[0])
    }

    @Test
    fun insertEmbed() {
        val delta = Delta().insert(1)
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.insertOp(1), delta.ops[0])
    }

    @Test
    fun insertEmbedNotInteger() {
        val embed = hashMapOf("url" to "http://quilljs.com")
        val attr = attrOf("alt" to "Quill")

        val delta = Delta().insert(embed, attr)
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.insertOp(embed, attr), delta.ops[0])
    }

    @Test
    fun insertTextAttr() {
        val delta = Delta().insert("test", attrOf("bold" to true))
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.insertOp("test", attrOf("bold" to true)), delta.ops[0])
    }

    @Test
    fun insertTextAfterDelete() {
        val delta = Delta().delete(1).insert("a")
        val expected = Delta().insert("a").delete(1)
        Assert.assertEquals(expected, delta)
    }

    @Test
    fun insertTextAfterDeleteWithMerge() {
        val delta = Delta().insert("a").delete(1).insert("b")
        val expected = Delta().insert("ab").delete(1)
        Assert.assertEquals(expected, delta)
    }

    @Test
    fun insertTextAfterDeleteNoMerge() {
        val delta = Delta().insert(1).delete(1).insert("a")
        val expected = Delta().insert(1).insert("a").delete(1)
        Assert.assertEquals(expected, delta)
    }

    @Test
    fun insertTextAttrEmpty() {
        val delta = Delta().insert("a", OpAttributes())
        val expected = Delta().insert("a")
        Assert.assertEquals(expected, delta)
    }

    @Test
    fun delete() {
        val delta = Delta().delete(0)
        Assert.assertEquals(0, delta.ops.size)
    }

    @Test
    fun deletePositive() {
        val delta = Delta().delete(1)
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.deleteOp(1), delta.ops[0])

    }

    @Test
    fun retain() {
        val delta = Delta().retain(0)
        Assert.assertEquals(0, delta.ops.size)
    }

    @Test
    fun retainLength() {
        val delta = Delta().retain(2)
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.retainOp(2), delta.ops[0])
    }

    @Test
    fun retainLengthNull() {
        val delta = Delta().retain(2, null)
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.retainOp(2), delta.ops[0])
    }

    @Test
    fun retainLengthAttr() {
        val delta = Delta().retain(2, hashMapOf("bold" to true))
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.retainOp(2, hashMapOf("bold" to true)), delta.ops[0])
    }

    @Test
    fun retainLengthEmptyAttr() {
        val delta = Delta().retain(2, OpAttributes()).delete(1)
        val expected = Delta().retain(2).delete(1)
        Assert.assertEquals(expected, delta)
    }

    @Test
    fun pushIntoEmpty() {
        val delta = Delta()
        delta.push(Op.insertOp("test"))
        Assert.assertEquals(1, delta.ops.size)
    }

    @Test
    fun pushConsecutiveDelete() {
        val delta = Delta().delete(2)
        delta.push(Op.deleteOp(3))
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.deleteOp(5), delta.ops[0])
    }

    @Test
    fun pushConsecutiveText() {
        val delta = Delta().insert("a")
        delta.push(Op.insertOp("b"))
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.insertOp("ab"), delta.ops[0])
    }

    @Test
    fun pushConsecutiveTextWithMatchingAttr() {
        val delta = Delta().insert("a", attrOf("bold" to true))
        delta.push(Op.insertOp("b", attrOf("bold" to true)))
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.insertOp("ab", attrOf("bold" to true)), delta.ops[0])
    }

    @Test
    fun pushConsecutiveRetainWithMatchingAttr() {
        val delta = Delta().retain(1, attrOf("bold" to true))
        delta.push(Op.retainOp(3, attrOf("bold" to true)))
        Assert.assertEquals(1, delta.ops.size)
        Assert.assertEquals(Op.retainOp(4, attrOf("bold" to true)), delta.ops[0])
    }

    @Test
    fun pushConsecutiveTextWithMismatchingAttr() {
        val delta = Delta().insert("a", attrOf("bold" to true))
        delta.push(Op.insertOp("b"))
        Assert.assertEquals(2, delta.ops.size)
    }

    @Test
    fun pushConsecutiveRetainWithMismatchingAttr() {
        val delta = Delta().retain(1, attrOf("bold" to true))
        delta.push(Op.retainOp(3))
        Assert.assertEquals(2, delta.ops.size)
    }

    @Test
    fun pushConsecutiveEmbedWithMatchingAttr() {
        val delta = Delta().insert(1, attrOf("alt" to "Description"))
        delta.push(Op.insertOp(attrOf("url" to "http://quilljs.com"), attrOf("alt" to "Description")))
        Assert.assertEquals(2, delta.ops.size)
    }

}