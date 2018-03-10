package com.quill.android.delta.delta

import com.quill.android.delta.Delta
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
}