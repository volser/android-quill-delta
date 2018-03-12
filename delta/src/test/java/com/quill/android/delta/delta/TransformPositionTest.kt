package com.quill.android.delta.delta

import com.quill.android.delta.Delta
import org.junit.Assert
import org.junit.Test

/**
 * Created by volser on 12.03.18.
 */
class TransformPositionTest {

    @Test
    fun insertBeforePosition() {
        val delta = Delta().insert("A")
        Assert.assertEquals(3, delta.transformPosition(2))
    }

    @Test
    fun insertAfterPosition() {
        val delta = Delta().retain(2).insert("A")
        Assert.assertEquals(1, delta.transformPosition(1))
    }

    @Test
    fun insertAtPosition() {
        val delta = Delta().retain(2).insert("A")
        Assert.assertEquals(2, delta.transformPosition(2, true))
        Assert.assertEquals(3, delta.transformPosition(2, false))
    }

    @Test
    fun deleteBeforePosition() {
        val delta = Delta().delete(2)
        Assert.assertEquals(2, delta.transformPosition(4))
    }

    @Test
    fun deleteAfterPosition() {
        val delta = Delta().retain(4).delete(2)
        Assert.assertEquals(2, delta.transformPosition(2))
    }

    @Test
    fun deleteAcrossPosition() {
        val delta = Delta().retain(1).delete(4)
        Assert.assertEquals(1, delta.transformPosition(2))
    }

    @Test
    fun insertDeleteBeforePosition() {
        val delta = Delta().retain(2).insert("A").delete(2)
        Assert.assertEquals(3, delta.transformPosition(4))
    }

    @Test
    fun insertBeforeDeleteAcrossPosition() {
        val delta = Delta().retain(2).insert("A").delete(4)
        Assert.assertEquals(3, delta.transformPosition(4))
    }

    @Test
    fun deleteBeforeDeleteAcrossPosition() {
        val delta = Delta().delete(1).retain(1).delete(4)
        Assert.assertEquals(1, delta.transformPosition(4))
    }
}