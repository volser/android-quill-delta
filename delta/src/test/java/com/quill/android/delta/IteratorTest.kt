package com.quill.android.delta

import com.quill.android.delta.utils.attrOf
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Created by volser on 07.03.18.
 */
class IteratorTest {

    lateinit var delta: Delta

    @Before
    fun data() {
        this.delta = Delta()
                .insert("Hello", attrOf("bold" to true))
                .retain(3)
                .insert(2, attrOf("src" to "http://quilljs.com/"))
                .delete(4);
    }

    @Test
    fun hasNextTrue() {
        var iter = Op.Iterator(this.delta.ops)
        Assert.assertEquals(true, iter.hasNext())
    }

    @Test
    fun hasNextFalse() {
        var iter = Op.Iterator(mutableListOf())
        Assert.assertEquals(false, iter.hasNext())
    }

    @Test
    fun peekLength() {
        var iter = Op.Iterator(this.delta.ops)
        Assert.assertEquals(5, iter.peekLength())
        iter.next()
        Assert.assertEquals(3, iter.peekLength())
        iter.next()
        Assert.assertEquals(1, iter.peekLength())
        iter.next()
        Assert.assertEquals(4, iter.peekLength())
    }

    @Test
    fun peekLengthOffset() {
        var iter = Op.Iterator(this.delta.ops)
        iter.next(2)
        Assert.assertEquals(5-2, iter.peekLength())
    }

    @Test
    fun peekLengthNoOps() {
        var iter = Op.Iterator(mutableListOf())
        Assert.assertEquals(Op.Iterator.INFINITY, iter.peekLength())
    }

    @Test
    fun peekType() {
        var iter = Op.Iterator(this.delta.ops)
        Assert.assertEquals(Op.Types.INSERT, iter.peekType())
        iter.next()
        Assert.assertEquals(Op.Types.RETAIN, iter.peekType())
        iter.next()
        Assert.assertEquals(Op.Types.INSERT, iter.peekType())
        iter.next()
        Assert.assertEquals(Op.Types.DELETE, iter.peekType())
        iter.next()
        Assert.assertEquals(Op.Types.RETAIN, iter.peekType())
    }

    @Test
    fun next() {
        var iter = Op.Iterator(this.delta.ops)
        for (op in this.delta.ops) {
            Assert.assertEquals(op, iter.next())
        }
        Assert.assertEquals(Op.retainOp(Op.Iterator.INFINITY), iter.next())
        Assert.assertEquals(Op.retainOp(Op.Iterator.INFINITY), iter.next(4))
        Assert.assertEquals(Op.retainOp(Op.Iterator.INFINITY), iter.next())

    }

    @Test
    fun nextLength() {
        var iter = Op.Iterator(this.delta.ops)

        Assert.assertEquals(Op.insertOp("He", attrOf("bold" to true)), iter.next(2))
        Assert.assertEquals(Op.insertOp("llo", attrOf("bold" to true)), iter.next(10))
        Assert.assertEquals(Op.retainOp(1), iter.next(1))
        Assert.assertEquals(Op.retainOp(2), iter.next(2))

    }
}