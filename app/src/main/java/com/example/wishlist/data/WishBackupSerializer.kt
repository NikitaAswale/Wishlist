package com.example.wishlist.data

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Serializes the wishlist to / from a portable JSON backup file so users can
 * export their wishes to the device or restore them later.
 */
object WishBackupSerializer {

    private const val FORMAT_VERSION = 1
    private const val KEY_VERSION = "version"
    private const val KEY_EXPORTED_AT = "exportedAt"
    private const val KEY_WISHES = "wishes"
    private const val KEY_ID = "id"
    private const val KEY_TITLE = "title"
    private const val KEY_DESCRIPTION = "description"
    private const val KEY_TARGET_DATE = "targetDate"
    private const val KEY_CREATED_AT = "createdAt"
    private const val KEY_IS_FULFILLED = "isFulfilled"
    private const val KEY_PRIORITY = "priority"
    private const val KEY_CATEGORY = "category"

    fun wishesToJson(wishes: List<Wish>): String {
        val root = JSONObject()
        root.put(KEY_VERSION, FORMAT_VERSION)
        root.put(KEY_EXPORTED_AT, System.currentTimeMillis())
        val array = JSONArray()
        wishes.forEach { wish ->
            val obj = JSONObject()
            obj.put(KEY_ID, wish.id)
            obj.put(KEY_TITLE, wish.title)
            obj.put(KEY_DESCRIPTION, wish.description)
            obj.put(KEY_TARGET_DATE, wish.targetDate)
            obj.put(KEY_CREATED_AT, wish.createdAt)
            obj.put(KEY_IS_FULFILLED, wish.isFulfilled)
            obj.put(KEY_PRIORITY, wish.priority.name)
            obj.put(KEY_CATEGORY, wish.category)
            array.put(obj)
        }
        root.put(KEY_WISHES, array)
        return root.toString()
    }

    @Throws(JSONException::class)
    fun wishesFromJson(json: String): List<Wish> {
        val root = JSONObject(json)
        val array = root.optJSONArray(KEY_WISHES)
            ?: throw JSONException("Backup file is missing the wishes list")
        val result = ArrayList<Wish>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val priorityName = obj.optString(KEY_PRIORITY, "")
            result.add(
                Wish(
                    id = obj.optLong(KEY_ID, 0L),
                    title = obj.getString(KEY_TITLE),
                    description = obj.optString(KEY_DESCRIPTION, ""),
                    targetDate = obj.getLong(KEY_TARGET_DATE),
                    createdAt = obj.optLong(KEY_CREATED_AT, System.currentTimeMillis()),
                    isFulfilled = obj.optBoolean(KEY_IS_FULFILLED, false),
                    priority = Priority.entries.firstOrNull { it.name == priorityName }
                        ?: Priority.MEDIUM,
                    category = obj.optString(KEY_CATEGORY, "General")
                )
            )
        }
        return result
    }
}