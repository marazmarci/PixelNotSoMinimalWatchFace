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

enum class ComplicationLocation : Parcelable {
    LEFT, MIDDLE, RIGHT, BOTTOM, ANDROID_12_TOP_LEFT, ANDROID_12_TOP_RIGHT, ANDROID_12_BOTTOM_LEFT, ANDROID_12_BOTTOM_RIGHT;

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ComplicationLocation> {
        override fun createFromParcel(parcel: Parcel): ComplicationLocation {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<ComplicationLocation?> {
            return arrayOfNulls(size)
        }
    }
}