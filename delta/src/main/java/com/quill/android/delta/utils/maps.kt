package com.quill.android.delta.utils

import com.quill.android.delta.OpAttributes

/**
 * Created by volser on 08.03.18.
 */
fun <K, V> Map<K, V>.mergeReduce(other: Map<K, V>, reduce: (V, V) -> V = { _, b -> b }): Map<K, V> {
    val result = HashMap<K, V>(this.size + other.size)
    result.putAll(this)
    other.forEach { e ->
        val existing = result[e.key]

        if (existing == null) {
            result[e.key] = e.value
        }
        else {
            result[e.key] = reduce(e.value, existing)
        }
    }

    return result
}

fun OpAttributes.mergeReduce(other: OpAttributes, reduce: (Any?, Any?) -> Any? = { _, b -> b }): OpAttributes {
    val result = OpAttributes(this.size + other.size)
    result.putAll(this)
    other.forEach { e ->
        val existing = result[e.key]

        if (existing == null) {
            result[e.key] = e.value
        }
        else {
            result[e.key] = reduce(existing, e.value)
        }
    }

    return result
}

fun attrOf(vararg pairs: Pair<String, Any?>): OpAttributes
        = OpAttributes(mapCapacity(pairs.size)).apply { putAll(pairs) }

fun mapCapacity(expectedSize: Int): Int {
    if (expectedSize < 3) {
        return expectedSize + 1
    }
    if (expectedSize < Int.MAX_VALUE / 2 + 1) {
        return expectedSize + expectedSize / 3
    }
    return Int.MAX_VALUE // any large value
}
