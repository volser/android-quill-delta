package com.quill.android.delta.delta

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.times
import com.quill.android.delta.Delta
import com.quill.android.delta.Op
import com.quill.android.delta.OpAttributes
import com.quill.android.delta.utils.attrOf
import kotlinx.serialization.json.JSON
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

/**
 * Created by volser on 16.03.18.
 */
class HelpersTest {

    val delta = Delta().insert("Hello").insert(hashMapOf("image" to true)).insert("World!")

    private val deltaCaptor = argumentCaptor<Delta>()
    private val attrCaptor = argumentCaptor<OpAttributes>()
    private val intCaptor = argumentCaptor<Int>()
    private val opCaptor = argumentCaptor<Op>()


    @Mock
    private
    lateinit var predicate: (Delta, OpAttributes, Int) -> Boolean

    @Mock
    private
    lateinit var predicateFalse: (Delta, OpAttributes, Int) -> Boolean

    @Mock
    private
    lateinit var predicateOp: (Op) -> Unit

    private var count = 0

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(predicate(any(), any(), any())).thenReturn(true)
        Mockito.`when`(predicateFalse(any(), any(), any())).doAnswer {
            if (count == 1) return@doAnswer false
            count += 1
            return@doAnswer true
        }

    }


    @Test
    fun concat() {
        val delta = Delta().insert("Test")
        val concat = Delta()
        val expected = Delta().insert("Test")

        Assert.assertEquals(expected, delta.concat(concat))
    }

    @Test
    fun concatUnmergeable() {
        val delta = Delta().insert("Test")
        val original = JSON.parse<Delta>(JSON.stringify(delta))
        val concat = Delta().insert("!", attrOf("bold" to true))
        val expected = Delta().insert("Test").insert("!", attrOf("bold" to true))

        Assert.assertEquals(expected, delta.concat(concat))
        Assert.assertEquals(original, delta)
    }

    @Test
    fun concatMergeable() {
        val delta = Delta().insert("Test", attrOf("bold" to true))
        val original = JSON.parse<Delta>(JSON.stringify(delta))

        val concat = Delta().insert("!", attrOf("bold" to true)).insert("/n")
        val expected = Delta().insert("Test!", attrOf("bold" to true)).insert("/n")

        Assert.assertEquals(expected, delta.concat(concat))
        Assert.assertEquals(original, delta)
    }

    @Test
    fun chopRetain() {
        val delta = Delta().insert("Test").retain(4)
        val expected = Delta().insert("Test")

        Assert.assertEquals(expected, delta.chop())
    }

    @Test
    fun chopInsert() {
        val delta = Delta().insert("Test")
        val expected = Delta().insert("Test")

        Assert.assertEquals(expected, delta.chop())
    }

    @Test
    fun chopRetainFormatted() {
        val delta = Delta().insert("Test").retain(4, attrOf("bold" to true))
        val expected = Delta().insert("Test").retain(4, attrOf("bold" to true))

        Assert.assertEquals(expected, delta.chop())
    }

    @Test
    fun eachLineExpected() {
        val delta = Delta().insert("Hello\n\n")
                .insert("World", attrOf("bold" to true))
                .insert( hashMapOf("image" to "octocat.png"))
                .insert("\n", attrOf("align" to "right"))
                .insert("!")

        delta.eachLine(predicate)

        Mockito.verify(predicate, times(4))(deltaCaptor.capture(), attrCaptor.capture(), intCaptor.capture())

        Assert.assertEquals(4, deltaCaptor.allValues.size)
        Assert.assertEquals(Delta().insert("Hello"), deltaCaptor.allValues[0])
        Assert.assertEquals(Delta(), deltaCaptor.allValues[1])
        Assert.assertEquals(Delta().insert("World", attrOf("bold" to true)).insert( hashMapOf("image" to "octocat.png")), deltaCaptor.allValues[2])
        Assert.assertEquals(Delta().insert("!"), deltaCaptor.allValues[3])

        Assert.assertEquals(4, attrCaptor.allValues.size)
        Assert.assertEquals(attrOf(), attrCaptor.allValues[0])
        Assert.assertEquals(attrOf(), attrCaptor.allValues[1])
        Assert.assertEquals(attrOf("align" to "right"), attrCaptor.allValues[2])
        Assert.assertEquals(attrOf(), attrCaptor.allValues[3])

        Assert.assertEquals(4, intCaptor.allValues.size)
        Assert.assertEquals(0, intCaptor.allValues[0])
        Assert.assertEquals(1, intCaptor.allValues[1])
        Assert.assertEquals(2, intCaptor.allValues[2])
        Assert.assertEquals(3, intCaptor.allValues[3])


    }

    @Test
    fun eachLineTrailingNewline() {
        val delta = Delta().insert("Hello\nWorld!\n")

        delta.eachLine(predicate)

        Mockito.verify(predicate, times(2))(deltaCaptor.capture(), attrCaptor.capture(), intCaptor.capture())

        Assert.assertEquals(2, deltaCaptor.allValues.size)
        Assert.assertEquals(Delta().insert("Hello"), deltaCaptor.allValues[0])
        Assert.assertEquals(Delta().insert("World!"), deltaCaptor.allValues[1])


        Assert.assertEquals(2, attrCaptor.allValues.size)
        Assert.assertEquals(attrOf(), attrCaptor.allValues[0])
        Assert.assertEquals(attrOf(), attrCaptor.allValues[1])


        Assert.assertEquals(2, intCaptor.allValues.size)
        Assert.assertEquals(0, intCaptor.allValues[0])
        Assert.assertEquals(1, intCaptor.allValues[1])

    }

    @Test
    fun eachLineNonDocument() {
        val delta = Delta().retain(1).delete(2)

        delta.eachLine(predicate)

        Mockito.verify(predicate, Mockito.never())(deltaCaptor.capture(), attrCaptor.capture(), intCaptor.capture())

        Assert.assertEquals(0, deltaCaptor.allValues.size)
        Assert.assertEquals(0, attrCaptor.allValues.size)
        Assert.assertEquals(0, intCaptor.allValues.size)

    }

    @Test
    fun earlyReturn() {
        val delta = Delta().insert("Hello\nNew\nWorld!\n")

        delta.eachLine(predicateFalse)

        Mockito.verify(predicateFalse, times(2))(deltaCaptor.capture(), attrCaptor.capture(), intCaptor.capture())

        Assert.assertEquals(2, deltaCaptor.allValues.size)
        Assert.assertEquals(2, attrCaptor.allValues.size)
        Assert.assertEquals(2, intCaptor.allValues.size)

    }

    @Test
    fun filter() {
        val arr = delta.filter { it.insert is String }

        Assert.assertEquals(2, arr.size)
    }

    @Test
    fun forEach() {
        delta.forEach(predicateOp)

        Mockito.verify(predicateOp, times(3))(opCaptor.capture())

        Assert.assertEquals(3, opCaptor.allValues.size)
    }

    @Test
    fun map() {
        val arr = delta.map { if (it.insert is String) it.insert else "" }

        Assert.assertEquals(listOf("Hello", "", "World!"), arr)
    }

    @Test
    fun partition() {
        val arr = delta.partition { it.insert is String }

        Assert.assertEquals(listOf(delta.ops[0], delta.ops[2]), arr.first)
        Assert.assertEquals(listOf(delta.ops[1]), arr.second)
    }

    @Test
    fun lengthDocument() {
        val delta = Delta().insert("AB", attrOf("bold" to true)).insert(1)

        Assert.assertEquals(3, delta.length())
    }

    @Test
    fun lengthMixed() {
        val delta = Delta().insert("AB", attrOf("bold" to true)).insert(1).retain(2, attrOf("bold" to null)).delete(1)

        Assert.assertEquals(6, delta.length())
    }

    @Test
    fun changeLengthMixed() {
        val delta = Delta().insert("AB", attrOf("bold" to true)).retain(2, attrOf("bold" to null)).delete(1)

        Assert.assertEquals(1, delta.changeLength())
    }

    @Test
    fun sliceStart() {
        val slice = Delta().retain(2).insert("A").slice(2)
        val expected = Delta().insert("A")

        Assert.assertEquals(expected, slice)
    }

    @Test
    fun sliceStartAndEndChop() {
        val slice = Delta().insert("0123456789").slice(2, 7)
        val expected = Delta().insert("23456")

        Assert.assertEquals(expected, slice)
    }

    @Test
    fun sliceStartAndEndMultiChop() {
        val slice = Delta().insert("0123", attrOf("bold" to true)).insert("4567").slice(3, 5)
        val expected = Delta().insert("3", attrOf("bold" to true)).insert("4")

        Assert.assertEquals(expected, slice)
    }

    @Test
    fun sliceStartAndEnd() {
        val slice = Delta().retain(2).insert("A", attrOf("bold" to true)).insert("B").slice(2, 3)
        val expected = Delta().insert("A", attrOf("bold" to true))

        Assert.assertEquals(expected, slice)
    }

    @Test
    fun sliceNoParams() {
        val delta = Delta().retain(2).insert("A", attrOf("bold" to true)).insert("B")
        val expected = delta.slice()

        Assert.assertEquals(expected, delta)
    }

    @Test
    fun sliceSplitOps() {
        val slice = Delta().insert("AB", attrOf("bold" to true)).insert("C").slice(1, 2)
        val expected = Delta().insert("B", attrOf("bold" to true))

        Assert.assertEquals(expected, slice)
    }

    @Test
    fun sliceSplitOpsMultiTimes() {
        val slice = Delta().insert("ABC", attrOf("bold" to true)).insert("D").slice(1, 2)
        val expected = Delta().insert("B", attrOf("bold" to true))

        Assert.assertEquals(expected, slice)
    }
}