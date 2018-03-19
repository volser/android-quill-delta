package com.quill.android.delta

import com.quill.android.delta.utils.mergeReduce

/**
 * Created by volser on 08.03.18.
 */
typealias OpAttributes = HashMap<String, Any?>

class AttributesUtil {
    companion object {
        fun compose(a: OpAttributes?, b: OpAttributes?, keepNull: Boolean = false): OpAttributes? {
            //ToDo optimize
            var result : OpAttributes?
            result = if (a== null && b == null) {
                OpAttributes()
            } else if (a == null) {
                OpAttributes(b)
            } else if (b == null) {
                OpAttributes(a)
            } else {
                OpAttributes(a.mergeReduce(b))
            }

            if (!keepNull) {
                result = OpAttributes(result.filter { it -> it.value !=null })
            }

            return if (result.isNotEmpty()) result else null

        }

        fun diff(a: OpAttributes?, b: OpAttributes?): OpAttributes? {
            val attr1 = if (a == null) OpAttributes() else OpAttributes(a)
            val attr2 = if (b == null) OpAttributes() else OpAttributes(b)
            val result = OpAttributes()

            val keys = mutableListOf<String>()
            keys.addAll(attr1.keys)
            keys.addAll(attr2.keys)

            keys.forEach {
                if (attr1[it] != attr2[it]) {
                    result[it] = if (attr2.containsKey(it)) attr2[it] else null
                }
            }

            return if (result.isNotEmpty()) result else null
        }

        fun transform(a: OpAttributes?, b: OpAttributes?, priority: Boolean): OpAttributes? {
            if (a == null) return b
            if (b == null) return null
            if (!priority) return b  // b simply overwrites us without priority
            val result = OpAttributes()
            b.keys.forEach {
                if (!a.containsKey(it)) {
                    result[it] = b[it] // null is a valid value
                }
            }

            return if (result.isNotEmpty()) result else null
        }
    }
}