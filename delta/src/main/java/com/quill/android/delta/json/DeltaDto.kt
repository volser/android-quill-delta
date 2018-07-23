package com.quill.android.delta.json

import com.google.gson.annotations.SerializedName


class DeltaDto {

    @SerializedName("ops")
    var ops: List<OpDto>? = null
}