package com.quill.android.delta.json

import com.google.gson.annotations.SerializedName


class OpDto {

    @SerializedName("insert")
    var insert: Any? = null

    @SerializedName("delete")
    var delete: Int? = null

    @SerializedName("retain")
    var retain: Int? = null

    @SerializedName("attributes")
    var attributes: HashMap<String, Any?>? = null
}