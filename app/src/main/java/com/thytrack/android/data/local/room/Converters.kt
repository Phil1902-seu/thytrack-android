package com.thytrack.android.data.local.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thytrack.android.domain.model.Drug
import com.thytrack.android.domain.model.RefRange
import java.util.Date

/** Room 类型转换器：Date / Drug / 自定义参考范围 JSON。 */
object Converters {
    private val gson = Gson()
    private val refRangeMapType = object : TypeToken<Map<String, RefRange>>() {}.type

    @TypeConverter fun dateToLong(value: Date?): Long? = value?.time

    @TypeConverter fun longToDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter fun drugToString(value: Drug): String = value.name

    @TypeConverter fun stringToDrug(value: String): Drug = Drug.fromName(value)

    @TypeConverter
    fun customRefRangesToJson(map: Map<String, RefRange>?): String? =
        if (map.isNullOrEmpty()) null else gson.toJson(map, refRangeMapType)

    @TypeConverter
    fun jsonToCustomRefRanges(json: String?): Map<String, RefRange>? {
        if (json.isNullOrEmpty()) return null
        return runCatching { gson.fromJson<Map<String, RefRange>>(json, refRangeMapType) }
            .getOrElse { null }
    }
}
