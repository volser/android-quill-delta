package com.quill.android.delta


/**
 * Created by volser on 07.03.18.
 */
class Delta  {

    var ops: MutableList<Op>

    constructor(ops: MutableList<Op> = ArrayList()) {
        this.ops = ops
    }

    constructor(delta: Delta) {
        this.ops = delta.ops
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

    fun insert(obj: Any, attributes: OpAttributes? = null): Delta {
        return push(Op.insertOp(obj, attributes))
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
                    lastOp.insert = (lastOp.insert as String) + (newOp.insert as String)
                    return this
                } else if (newOp.retain > 0 && lastOp.retain > 0) {
                    lastOp.retain = lastOp.retain + newOp.retain
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

}