package com.quill.android.delta.json

import com.google.gson.Gson
import com.quill.android.delta.Delta

public object DeltaJson {

    fun fromJson(text : String?): Delta? {
        return DataMapper.toDelta(Gson().fromJson(text, DeltaDto::class.java))
    }

    fun  toJson(delta: Delta?): String? {
        return Gson().toJson(DataMapper.toDeltaDto(delta))
    }
}