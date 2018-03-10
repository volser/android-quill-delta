package com.quill.android.delta

import com.quill.android.delta.utils.mergeReduce
import kotlin.collections.hashMapOf

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
            return null
        }
    }
}