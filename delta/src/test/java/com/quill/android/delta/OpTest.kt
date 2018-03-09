package com.quill.android.delta

import org.junit.Test

import org.junit.Assert.*

class OpTest {

    @Test
    fun lengthDelete() {
        assertEquals(5, Op.length(Op.deleteOp(5)))
    }

    @Test
    fun lengthRetain() {
        assertEquals(2, Op.length(Op.retainOp(2)))
    }

    @Test
    fun lengthInsertText() {
        assertEquals(4, Op.length(Op.insertOp("Text")))
    }

    @Test
    fun lengthInsertEmbed() {
        assertEquals(1, Op.length(Op.insertOp(2)))
    }
}
