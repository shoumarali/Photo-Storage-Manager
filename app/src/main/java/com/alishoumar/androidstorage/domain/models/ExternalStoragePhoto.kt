package com.alishoumar.androidstorage.domain.models

import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable


data class ExternalStoragePhoto(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    val uri: Uri
) : Parcelable {

    val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()

    constructor(parcel: Parcel): this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(Uri::class.java.classLoader, Uri::class.java) ?: Uri.EMPTY
        } else {
            @Suppress("DEPRECATION")
            parcel.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeParcelable(uri, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ExternalStoragePhoto> {
        override fun createFromParcel(parcel: Parcel): ExternalStoragePhoto {
            return ExternalStoragePhoto(parcel)
        }

        override fun newArray(size: Int): Array<ExternalStoragePhoto?> {
            return arrayOfNulls(size)
        }
    }
}
