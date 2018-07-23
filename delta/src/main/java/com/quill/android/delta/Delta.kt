package com.quill.android.delta

import com.quill.android.delta.json.DeltaJson
import com.quill.android.delta.utils.diff_match_patch
import kotlinx.serialization.Serializable
import kotlin.math.min


/**
 * Created by volser on 07.03.18.
 */
@Serializable
class Delta  {

    companion object {
        val NULL_CODE = 0
        val NULL_CHARACTER = NULL_CODE.toString()
    }

    var ops: MutableList<Op>

    constructor(ops: MutableList<Op> = ArrayList()) {
        this.ops = ops
    }

    constructor(delta: Delta) {
        this.ops = delta.ops
    }

    constructor(text: String) {
        val delta = DeltaJson.fromJson(text)
        if (delta != null) {
            this.ops = delta.ops
        } else {
            ops = ArrayList()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is Delta) {
            return ops == other.ops
        }
        return true
    }

    override fun hashCode(): Int {
        return ops.hashCode()
    }

    override fun toString(): String {
        return ops.toString()
    }

    fun insert(num: Int, attributes: OpAttributes? = null): Delta {
        return if (num <= 0) this else push(Op.insertOp(num, attributes))
    }

    fun insert(obj: Map<String, Any?>, attributes: OpAttributes? = null): Delta {
        return push(Op.insertOp(HashMap(obj), attributes))
    }

    fun insert(text: String, attributes: OpAttributes? = null): Delta {
        return if (text.isEmpty()) this else push(Op.insertOp(text, attributes))

    }

    fun delete(length: Int): Delta {
        return if (length <= 0) this else push(Op.deleteOp(length))

    }

    fun retain(retain: Int, attributes: OpAttributes? = null): Delta {
        return if (retain <= 0) this else push(Op.retainOp(retain, attributes))
    }

    fun push(newOp: Op): Delta {
        var index = this.ops.size
        var lastOp: Op? = if (index > 0) this.ops[index - 1] else null
        if (lastOp != null) {
            if (newOp.delete > 0 && lastOp.delete > 0) {
                lastOp.delete = lastOp.delete + newOp.delete
                return this
            }

            // Since it does not matter if we insert before or after deleting at the same index,
            // always prefer to insert first
            if (lastOp.delete > 0 && newOp.insert != null) {
                index -= 1
                lastOp = if (index > 0) this.ops[index - 1] else null
                if (lastOp == null) {
                    this.ops.add(0, newOp)
                    return this
                }
            }

            if (newOp.attributes == lastOp.attributes) {
                if (newOp.insert is String && lastOp.insert is String) {
                    this.ops[index - 1] = Op.insertOp((lastOp.insert as String) + (newOp.insert as String))
                    if (newOp.attributes != null) {
                        this.ops[index - 1].attributes = newOp.attributes
                    }
                    return this

                } else if (newOp.retain > 0 && lastOp.retain > 0) {
                    this.ops[index - 1] = Op.retainOp(lastOp.retain + newOp.retain)
                    if (newOp.attributes != null) {
                        this.ops[index - 1].attributes = newOp.attributes
                    }
                    return this
                }
            }

        }
        /*if (index == this.ops.size()) {
            this.ops.add(newOp);
        } else {
            this.ops.add(index, newOp);
        }*/
        this.ops.add(index, newOp)
        return this
    }

    fun chop(): Delta {
        if (ops.size > 0) {
            val lastOp = this.ops[ops.size - 1]
            if (lastOp.retain > 0 && (lastOp.attributes == null || lastOp.attributes!!.size == 0)) {
                ops.removeAt(ops.size - 1)
            }
        }

        return this
    }

    fun length(): Int {
        return ops.fold(0){ length, op -> length + Op.length(op) }
    }

    fun changeLength(): Int {
        return this.reduce(0){
            length, op -> if (op.insert != null) length + Op.length(op) else if (op.delete > 0) length - op.delete else length
        }
    }

    inline fun <R> reduce(initial: R, operation: (acc: R, Op) -> R): R {
        return ops.fold(initial, operation)
    }

    inline fun <R>  map(transform: (Op) -> R): List<R> {
        return this.ops.map(transform)
    }

    inline fun filter(predicate: (Op) -> Boolean): List<Op> {
        return this.ops.filter(predicate)
    }

    inline fun forEach(action: (Op) -> Unit) {
        return this.ops.forEach(action)
    }

    inline fun partition(predicate: (Op) -> Boolean): Pair<List<Op>, List<Op>> {
        val result = Pair<ArrayList<Op>, ArrayList<Op>>( arrayListOf(), arrayListOf())
        forEach {
            if (predicate(it)) result.first.add(it) else result.second.add(it)
        }
        return result
    }

    fun compose(other: Delta): Delta {
        val thisIter = Op.Iterator(this.ops)
        val otherIter = Op.Iterator(other.ops)
        val delta = Delta()

        while (thisIter.hasNext() || otherIter.hasNext()) {
            if (otherIter.peekType() === Op.Types.INSERT) {
                delta.push(otherIter.next())
            } else if (thisIter.peekType() === Op.Types.DELETE) {
                delta.push(thisIter.next())
            } else {
                val length = Math.min(thisIter.peekLength(), otherIter.peekLength())
                val thisOp = thisIter.next(length)
                val otherOp = otherIter.next(length)
                if (otherOp.retain > 0) {
                    val newOp = Op()
                    if (thisOp.retain > 0) {
                        newOp.retain = length
                    } else {
                        newOp.insert = thisOp.insert
                    }
                    // Preserve null when composing with a retain, otherwise remove it for inserts
                    val attributes = AttributesUtil.compose(thisOp.attributes, otherOp.attributes, thisOp.retain > 0)
                    //if (attributes) newOp.attributes = attributes;
                    newOp.attributes = attributes
                    delta.push(newOp)
                    // Other op should be delete, we could be an insert or retain
                    // Insert + delete cancels out
                } else if (otherOp.delete > 0 && thisOp.retain > 0) {
                    delta.push(otherOp)
                }
            }
        }

        return delta.chop()
    }

    fun diff(other: Delta): Delta {
        if (ops === other.ops) {
            return Delta()
        }

        val stringBuilder1 = StringBuilder()
        val stringBuilder2 = StringBuilder()

        ops.forEach { it ->
            if (it.insert != null) {
                if (it.insert is String) stringBuilder1.append(it.insert as String) else stringBuilder1.append(NULL_CHARACTER)
            } else
                throw kotlin.Exception("diff() called with non-document")
        }

        other.ops.forEach { it ->
            if (it.insert != null) {
                if (it.insert is String) stringBuilder2.append(it.insert as String) else stringBuilder2.append(NULL_CHARACTER)
            } else
                throw kotlin.Exception("diff() called on non-document")
        }


        val thisIter = Op.Iterator(this.ops)
        val otherIter = Op.Iterator(other.ops)
        val diffResult = diff_match_patch().diff_main(stringBuilder1.toString(), stringBuilder2.toString())
        val delta = Delta()

        diffResult.forEach {
            var length = it.text.length
            while (length > 0) {
                var opLength = 0
                when (it?.operation) {
                    diff_match_patch.Operation.INSERT -> {
                        opLength = min(otherIter.peekLength(), length)
                        delta.push(otherIter.next(opLength))
                    }
                    diff_match_patch.Operation.DELETE -> {
                        opLength = min(length, thisIter.peekLength())
                        thisIter.next(opLength)
                        delta.delete(opLength)
                    }
                    diff_match_patch.Operation.EQUAL -> {
                        opLength = min(min(thisIter.peekLength(), otherIter.peekLength()), length)
                        val thisOp = thisIter.next(opLength)
                        val otherOp = otherIter.next(opLength)
                        if (thisOp.insert == otherOp.insert) {
                            delta.retain(opLength, AttributesUtil.diff(thisOp.attributes, otherOp.attributes))
                        } else {
                            delta.push(otherOp).delete(opLength)
                        }
                    }
                }
                length -= opLength
            }

        }

        return delta.chop()
    }

    fun transform(other: Delta, priority: Boolean = false): Delta {
        val thisIter = Op.Iterator(this.ops)
        val otherIter = Op.Iterator(other.ops)
        val delta = Delta()
        while (thisIter.hasNext() || otherIter.hasNext()) {
            if (thisIter.peekType() === Op.Types.INSERT && (priority || otherIter.peekType() !== Op.Types.INSERT)) {
                delta.retain(Op.length(thisIter.next()))
            } else if (otherIter.peekType() === Op.Types.INSERT) {
                delta.push(otherIter.next())
            } else {
                val length = Math.min(thisIter.peekLength(), otherIter.peekLength())
                val thisOp = thisIter.next(length)
                val otherOp = otherIter.next(length)
                if (thisOp.delete > 0) {
                    // Our delete either makes their delete redundant or removes their retain
                    continue
                } else if (otherOp.delete > 0) {
                    delta.push(otherOp)
                } else {
                    // We retain either their retain or insert
                    delta.retain(length, AttributesUtil.transform(thisOp.attributes, otherOp.attributes, priority))
                }
            }
        }
        return delta.chop()
    }

    fun transformPosition(indexFrom: Int, priority: Boolean = false): Int {
        val thisIter = Op.Iterator(this.ops)
        var offset = 0
        var index = indexFrom
        while (thisIter.hasNext() && offset <= index) {
            val length = thisIter.peekLength()
            val nextType = thisIter.peekType()
            thisIter.next()
            if (nextType === Op.Types.DELETE) {
                index -= Math.min(length, index - offset)
                continue
            } else if (nextType === Op.Types.INSERT && (offset < index || !priority)) {
                index += length
            }
            offset += length
        }
        return index
    }

    fun concat(other: Delta): Delta {
        val delta = Delta(ArrayList(this.ops))
        if (other.ops.size > 0) {
            delta.push(other.ops[0])
            if (other.ops.size > 1) {
                delta.ops.addAll(other.ops.subList(1, other.ops.size))
            }
        }
        return delta
    }

    fun slice(start: Int = 0, end: Int = Int.MAX_VALUE): Delta {
        val ops = arrayListOf<Op>()
        val iter = Op.Iterator(this.ops)
        var index = 0
        while (index < end && iter.hasNext()) {
            var nextOp : Op
            if (index < start) {
                nextOp = iter.next(start - index)
            } else {
                nextOp = iter.next(end - index)
                ops.add(nextOp)
            }
            index += Op.length(nextOp)
        }

        return Delta(ops)
    }

    inline fun eachLine(action: (Delta, OpAttributes, Int) -> Boolean, newline: String = "\n") {
        val iter = Op.Iterator(this.ops)
        var line = Delta()
        var i = 0
        while (iter.hasNext()) {
            if (iter.peekType() !== Op.Types.INSERT) return
            val thisOp = iter.peek()
            val start = Op.length(thisOp) - iter.peekLength()
            val index = if (thisOp?.insert is String) (thisOp.insert as String).indexOf(newline, start) - start else -1
            when {
                index < 0 -> line.push(iter.next())
                index > 0 -> line.push(iter.next(index))
                else -> {
                    if (!action(line, iter.next(1).attributes ?: OpAttributes(), i)) {
                        return
                    }
                    i += 1
                    line = Delta()
                }
            }
        }
        if (line.length() > 0) {
            action(line, OpAttributes(), i)
        }
    }

}