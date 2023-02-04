/*
 *   Copyright 2022 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.benoitletondor.pixelminimalwatchface.common.settings.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt

data class ComplicationColor(
    @ColorInt val color: Int,
    val label: String,
    val isDefault: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(color)
        parcel.writeString(label)
        parcel.writeByte(if (isDefault) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ComplicationColor> {
        override fun createFromParcel(parcel: Parcel): ComplicationColor {
            return ComplicationColor(parcel)
        }

        override fun newArray(size: Int): Array<ComplicationColor?> {
            return arrayOfNulls(size)
        }
    }
}