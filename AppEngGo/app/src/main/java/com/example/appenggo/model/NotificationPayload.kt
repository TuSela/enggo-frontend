package com.example.appenggo.model

import android.os.Parcel
import android.os.Parcelable

data class NotificationPayload(
    val type: String,
    val fromUserId: Int,
    val fromUsername: String,
    val message: String,
    val requestId: Int?,
    // Thêm field cho PVP_INVITE
    val examTitle: String? = null,
    val examTopic: String? = null,
    val difficulty: String? = null,
    val questionCount: Int? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(type)
        dest.writeInt(fromUserId)
        dest.writeString(fromUsername)
        dest.writeString(message)
        dest.writeValue(requestId)
        dest.writeString(examTitle)
        dest.writeString(examTopic)
        dest.writeString(difficulty)
        dest.writeValue(questionCount)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<NotificationPayload> {
        override fun createFromParcel(parcel: Parcel) = NotificationPayload(parcel)
        override fun newArray(size: Int): Array<NotificationPayload?> = arrayOfNulls(size)
    }
}