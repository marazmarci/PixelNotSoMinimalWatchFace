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
package com.benoitletondor.pixelminimalwatchface.drawer.digital

import android.graphics.Canvas
import android.graphics.Paint
import java.util.*

interface SecondsRingDrawer {
    fun drawSecondRing(
        canvas: Canvas,
        calendar: Calendar,
        paint: Paint,
        useSweepingMotion: Boolean
    )
}

class SecondRingDrawerImpl(
    private val screenWidth: Int,
    private val screenHeight: Int,
) : SecondsRingDrawer {
    override fun drawSecondRing(
        canvas: Canvas,
        calendar: Calendar,
        paint: Paint,
        useSweepingMotion: Boolean
    ) {
        val endAngle = calculateEndAngle(useSweepingMotion, calendar)
        canvas.drawArc(0F, 0F, screenWidth.toFloat(), screenHeight.toFloat(), 270F, endAngle, false, paint)
    }

    private fun calculateEndAngle(useSweepingMotion: Boolean, calendar: Calendar) = if (useSweepingMotion) {
        calculateSweepingEndRotation(calendar)
    } else {
        calculateTickingEndRotation(calendar)
    }

    private fun calculateTickingEndRotation(calendar: Calendar) = (calendar.get(Calendar.SECOND) * 6).toFloat()

    private fun calculateSweepingEndRotation(calendar: Calendar): Float {
        val epochMillisOfDay = calendar.timeInMillis
        val millisPerSecondRingRotation = 1000 * 60
        return epochMillisOfDay.rem(millisPerSecondRingRotation) * 360.0f / millisPerSecondRingRotation
    }
}
