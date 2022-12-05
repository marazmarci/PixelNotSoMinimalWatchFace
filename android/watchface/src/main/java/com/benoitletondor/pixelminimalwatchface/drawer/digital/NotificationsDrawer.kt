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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.benoitletondor.pixelminimalwatchface.helper.toBitmap
import com.benoitletondor.pixelminimalwatchface.PhoneNotifications
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.helper.dpToPx
import kotlin.math.min

interface NotificationsDrawer {
    fun drawNotifications(
        canvas: Canvas,
        paint: Paint,
        state: PhoneNotifications.NotificationState,
    )
    fun isTapOnNotifications(x: Int, y: Int): Boolean
}

class NotificationsDrawerImpl(
    context: Context,
    private val centerX: Float,
    private val notificationsRect: Rect,
) : NotificationsDrawer {
    private val notificationSpacePx = context.dpToPx(NOTIFICATION_SPACING_DP)
    private val notificationSizePx: Float = run {
        val defaultSize = context.dpToPx(NOTIFICATION_ICON_SIZE_DP)
        val availableHeight = notificationsRect.bottom - notificationsRect.top
        min(defaultSize.toFloat(), availableHeight.toFloat())
    }
    private val notificationTopPx = notificationsRect.top + (notificationsRect.bottom - notificationsRect.top - notificationSizePx) / 2f
    private val moreIconBitmap = ContextCompat
        .getDrawable(context, R.drawable.ic_more_horiz_24dp_wht)!!
        .toBitmap(notificationSizePx.toInt(), notificationSizePx.toInt())
    private val stateIconBitmap = ContextCompat
        .getDrawable(context, R.drawable.ic_baseline_question_mark_24)!!
        .toBitmap(notificationSizePx.toInt(), notificationSizePx.toInt())

    override fun drawNotifications(
        canvas: Canvas,
        paint: Paint,
        state: PhoneNotifications.NotificationState,
    ) {
        when(state) {
            is PhoneNotifications.NotificationState.DataReceived -> {
                var numberOfItems = state.icons.size + (if (state.hasMore) 1 else 0)
                var size = computeSize(numberOfItems)
                while( size > notificationsRect.width() && size > 0 ) {
                    numberOfItems--
                    size = computeSize(numberOfItems)
                }

                var x = centerX - size / 2f
                for(i in 0 until numberOfItems) {
                    val bitmap = if (i == numberOfItems -1 && state.hasMore) {
                        moreIconBitmap
                    } else {
                        state.icons[i]
                    }

                    canvas.drawBitmap(
                        bitmap,
                        null,
                        RectF(
                            x,
                            notificationTopPx,
                            x + notificationSizePx,
                            notificationTopPx + notificationSizePx
                        ),
                        paint,
                    )

                    x += notificationSizePx + notificationSpacePx
                }
            }
            is PhoneNotifications.NotificationState.Unknown -> {
                if (state.isStale(System.currentTimeMillis())) {
                    canvas.drawBitmap(
                        stateIconBitmap,
                        null,
                        RectF(
                            centerX - notificationSizePx / 2f,
                            notificationTopPx,
                            centerX + notificationSizePx / 2f,
                            notificationTopPx + notificationSizePx
                        ),
                        paint,
                    )
                }
            }
        }
    }

    override fun isTapOnNotifications(x: Int, y: Int): Boolean {
        return notificationsRect.contains(x, y)
    }

    private fun computeSize(numberOfItems: Int): Float {
        return (numberOfItems * notificationSizePx) + (notificationSpacePx * if (numberOfItems > 0) numberOfItems - 1 else 0)
    }

    companion object {
        private const val NOTIFICATION_ICON_SIZE_DP = 12
        private const val NOTIFICATION_SPACING_DP = 5
    }
}