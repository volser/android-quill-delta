package com.quill.android.delta

import kotlinx.serialization.*
import kotlinx.serialization.json.JSON
/**
 * Created by volser on 06.03.18.
 */
@Serializable
class Op {

    enum class Types {
        INSERT, RETAIN, DELETE
    }

    var insert: Any? = null
    var delete: Int = 0
    var retain: Int = 0
    var attributes: OpAttributes? = null

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true
        if (other == null)
            return false
        if (other !is Op)
            return false
        if (delete != other.delete)
            return false
        if (retain != other.retain)
            return false
        if (insert != other.insert)
            return false
        if(attributes != other.attributes && !((attributes == null || attributes?.size == 0) && (other.attributes == null || other.attributes?.size == 0))
        )
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = insert?.hashCode() ?: 0
        result = 31 * result + delete
        result = 31 * result + retain
        result = 31 * result + (attributes?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "{ insert: $insert, delete: $delete, retain: $retain, attr: $attributes }"
    }

    fun getAttributeValue(attributeName: String): Any? {
        return if (attributes != null) {
            attributes!![attributeName]
        } else null
    }

    class Iterator(private val ops: List<Op>?) {
        private var index = 0
        private var offset = 0

        private val current: Op
            get() = ops!![index]

        private fun notEmptyOrEnd(): Boolean {
            return ops != null && ops.size > index
        }

        fun peekLength(): Int {
            return if (notEmptyOrEnd()) {
                // Should never return 0 if our index is being managed correctly
                Op.length(current) - this.offset
            } else {
                INFINITY
            }
        }

        fun peek(): Op? {
            return if (notEmptyOrEnd()) current else null
        }

        operator fun hasNext(): Boolean {
            return peekLength() < INFINITY
        }

        fun peekType(): Types {
            if (notEmptyOrEnd()) {
                return when {
                    current.delete > 0 -> Types.DELETE
                    current.retain > 0 -> Types.RETAIN
                    else -> Types.INSERT
                }
            }
            return Types.RETAIN
        }

        operator fun next(): Op {
            return this.next(null)
        }

        fun next(len: Int?): Op {
            var length = len
            if (length == null) length = INFINITY
            val nextOp = peek()
            if (nextOp != null) {
                val offset = this.offset
                val opLength = Op.length(nextOp)
                if (length >= opLength - offset) {
                    length = opLength - offset
                    this.index += 1
                    this.offset = 0
                } else {
                    this.offset += length
                }
                return if (nextOp.delete > 0) {
                    Op.deleteOp(length)
                } else {
                    val retOp = Op()
                    retOp.attributes = nextOp.attributes
                    if (nextOp.retain  > 0) {
                        retOp.retain = length
                    } else if (nextOp.insert != null && nextOp.insert is String) {
                        retOp.insert = (nextOp.insert!! as String).substring(offset, offset + length)
                    } else {
                        retOp.insert = nextOp.insert
                    }
                    retOp
                }
            } else {
                return Op.retainOp(INFINITY, null)
            }
        }

        companion object {

            var INFINITY = Integer.MAX_VALUE
        }

    }

    /*class OpAttributes : HashMap<String, Any> {

        constructor() : super() {}

        constructor(attributes: OpAttributes) : super(attributes) {}

        constructor(attributes: Map<String, Any>) : super(attributes) {}

        companion object {

            fun compose(a: OpAttributes?, b: OpAttributes?, keepNull: Boolean = false): OpAttributes? {
                /*var a = a
                var b = b
                if (a == null)
                    a = OpAttributes()
                if (b == null)
                    b = OpAttributes()
                var attributes = OpAttributes(b)

                if (!keepNull) {
                    attributes = OpAttributes(Maps.filterEntries(attributes, object : Predicate<Entry<String, Any>>() {
                        fun apply(input: Entry<String, Any>): Boolean {
                            return input.value != null
                        }
                    }))

                }
                for (key in a.keySet()) {
                    val value = a[key]
                    if (value != null && !b.containsKey(key)) {
                        attributes.put(key, value)
                    }
                }

                return if (attributes.keySet().size() > 0) attributes else null*/

                return null

            }
        }

    }*/

    companion object {

        fun insertOp(insert: Any): Op {
            return this.insertOp(insert, null)
        }

        fun insertOp(insert: Any, attributes: OpAttributes?): Op {
            val op = Op()
            op.insert = insert
            op.attributes = attributes
            return op
        }

        fun deleteOp(delete: Int): Op {
            val op = Op()
            op.delete = delete
            return op
        }

        fun retainOp(retain: Int): Op {
            return this.retainOp(retain, null)
        }

        fun retainOp(retain: Int, attributes: OpAttributes?): Op {
            val op = Op()
            op.retain = retain
            op.attributes = attributes
            return op
        }

        fun length(op: Op?): Int {
            if (op == null) {
                return 0
            }
            return when {
                op.delete > 0 -> op.delete
                op.retain > 0 -> op.retain
                op.insert !is String -> 1
                else -> (op.insert as String).length
            }

        }
    }

}