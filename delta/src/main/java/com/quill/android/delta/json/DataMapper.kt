package com.quill.android.delta.json

import com.quill.android.delta.Delta
import com.quill.android.delta.Op
import com.quill.android.delta.OpAttributes
import com.quill.android.delta.utils.attrOf


object DataMapper {

    fun toDelta(deltaDto: DeltaDto?): Delta {
        val delta = Delta()
        if (deltaDto?.ops != null) {
            delta.ops = ArrayList()
            for (opDto in deltaDto.ops!!) {
                delta.ops.add(toOp(opDto))
            }
        }

        return delta
    }

    fun toDeltaDto(delta: Delta?): DeltaDto {
        val deltaDto = DeltaDto()
        if (delta?.ops != null) {
            deltaDto.ops = ArrayList()
            deltaDto.ops = delta.ops.map { it -> toOpDto(it) }
        }

        return deltaDto
    }

    fun toOp(opDto: OpDto?): Op {
        val op = Op()

        if (opDto != null) {
            if (opDto.delete != null) {
                op.delete = opDto.delete!!
            }
            if (opDto.retain != null) {
                op.retain = opDto.retain!!
            }

            op.insert = opDto.insert

            if (opDto.attributes != null) {
                op.attributes =  OpAttributes(opDto.attributes)
            }
        }

        return op
    }

    fun toOpDto(op: Op?): OpDto {
        val opDto = OpDto()

        if (op != null) {
            if (op.delete > 0) {
                opDto.delete = op.delete
            }
            if (op.retain > 0) {
                opDto.retain = op.retain
            }

            opDto.insert = op.insert

            if (op.attributes != null) {
                opDto.attributes =  op.attributes
            }
        }

        return opDto
    }


}