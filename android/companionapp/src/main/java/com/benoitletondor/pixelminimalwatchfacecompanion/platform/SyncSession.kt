package com.benoitletondor.pixelminimalwatchfacecompanion.platform

import android.util.Log
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationColors
import com.benoitletondor.pixelminimalwatchface.common.settings.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.common.settings.model.InitialState
import com.benoitletondor.pixelminimalwatchface.common.settings.model.Parameter
import com.benoitletondor.pixelminimalwatchface.common.settings.model.SYNC_KEY_EDIT_COMPLICATION_PATH
import com.benoitletondor.pixelminimalwatchface.common.settings.model.SYNC_KEY_GET_INITIAL_STATE_ACK_PATH
import com.benoitletondor.pixelminimalwatchface.common.settings.model.SYNC_KEY_GET_INITIAL_STATE_PATH
import com.benoitletondor.pixelminimalwatchface.common.settings.model.SYNC_KEY_GET_VERSION_ACK_PATH
import com.benoitletondor.pixelminimalwatchface.common.settings.model.SYNC_KEY_GET_VERSION_PATH
import com.benoitletondor.pixelminimalwatchface.common.settings.model.SYNC_KEY_REQUEST_COMPLICATIONS_PERMISSION_ACK_PATH
import com.benoitletondor.pixelminimalwatchface.common.settings.model.SYNC_KEY_REQUEST_COMPLICATIONS_PERMISSION_PATH
import com.benoitletondor.pixelminimalwatchface.common.settings.model.SYNC_KEY_SEND_COMPLICATION_COLORS_ACK_PATH
import com.benoitletondor.pixelminimalwatchface.common.settings.model.SYNC_KEY_SEND_COMPLICATION_COLORS_PATH
import com.benoitletondor.pixelminimalwatchface.common.settings.model.SYNC_KEY_SEND_PARAMETER_ACK_PATH
import com.benoitletondor.pixelminimalwatchface.common.settings.model.SYNC_KEY_SEND_PARAMETER_PATH
import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.await
import com.google.android.gms.wearable.MessageClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.ByteBuffer

class SyncSession(
    private val messageClient: MessageClient,
    private val nodeId: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val parameterSetAckMutableSharedFlow = MutableSharedFlow<Parameter>(replay = 1) // Replay 1 as we might miss it if it happens too quickly
    private val versionAckSharedFlow = MutableSharedFlow<Int>()
    private val initialStateAckSharedFlow = MutableSharedFlow<InitialState>()
    private val complicationColorsAckSharedFlow = MutableSharedFlow<ComplicationColors>(replay = 1)
    private val complicationsPermissionRequestAckSharedFlow = MutableSharedFlow<Boolean>()
    private val messageListener = MessageClient.OnMessageReceivedListener { messageEvent ->
        if (DEBUG_LOGS) Log.d(TAG, "OnMessageReceived: ${messageEvent.requestId}: ${messageEvent.path}")

        if (messageEvent.sourceNodeId != nodeId) {
            return@OnMessageReceivedListener
        }

        when(messageEvent.path) {
            SYNC_KEY_SEND_PARAMETER_ACK_PATH -> scope.launch {
                try {
                    parameterSetAckMutableSharedFlow.emit(Parameter.fromJson(messageEvent.data.toString(Charsets.UTF_8)))
                } catch (e: Exception) {
                    if (e is CancellationException) { throw e }
                    Log.e(TAG, "Error while deserializing ParameterAck", e)
                }
            }
            SYNC_KEY_GET_VERSION_ACK_PATH -> scope.launch {
                try {
                    versionAckSharedFlow.emit(ByteBuffer.wrap(messageEvent.data).int)
                } catch (e: Exception) {
                    if (e is CancellationException) { throw e }
                    Log.e(TAG, "Error while deserializing VersionAck", e)
                }
            }
            SYNC_KEY_GET_INITIAL_STATE_ACK_PATH -> scope.launch {
                try {
                    initialStateAckSharedFlow.emit(InitialState.fromJson(messageEvent.data.toString(Charsets.UTF_8)))
                } catch (e: Exception) {
                    if (e is CancellationException) { throw e }
                    Log.e(TAG, "Error while deserializing InitialStateAck", e)
                }
            }
            SYNC_KEY_SEND_COMPLICATION_COLORS_ACK_PATH -> scope.launch {
                try {
                    complicationColorsAckSharedFlow.emit(ComplicationColors.fromJson(messageEvent.data.toString(Charsets.UTF_8)))
                } catch (e: Exception) {
                    if (e is CancellationException) { throw e }
                    Log.e(TAG, "Error while deserializing ComplicationColors ack", e)
                }
            }
            SYNC_KEY_REQUEST_COMPLICATIONS_PERMISSION_ACK_PATH -> scope.launch {
                try {
                    complicationsPermissionRequestAckSharedFlow.emit(ByteBuffer.wrap(messageEvent.data).int == 1)
                } catch (e: Exception) {
                    if (e is CancellationException) { throw e }
                    Log.e(TAG, "Error while deserializing ComplicationsPermissionRequest ack", e)
                }
            }
        }
    }

    init {
        messageClient.addListener(messageListener)
    }

    fun close() {
        scope.cancel()
        messageClient.removeListener(messageListener)
    }

    suspend fun getWatchNodeVersion(): Int {
        return withTimeoutOrNull(WATCH_RESPONSE_TIMEOUT_MS) {
            messageClient.sendMessage(nodeId, SYNC_KEY_GET_VERSION_PATH, null).await()
            versionAckSharedFlow.first()
        } ?: throw Exception("Timeout while waiting for watch response")
    }

    suspend fun getInitialState(): InitialState {
        return withTimeoutOrNull(WATCH_RESPONSE_TIMEOUT_MS) {
            messageClient.sendMessage(nodeId, SYNC_KEY_GET_INITIAL_STATE_PATH, null).await()
            initialStateAckSharedFlow.first()
        } ?: throw Exception("Timeout while waiting for watch response")
    }

    suspend fun setParameter(key: String, value: Any) {
        withTimeoutOrNull(WATCH_RESPONSE_TIMEOUT_MS) {
            val parameter = Parameter(key, value)
            messageClient.sendMessage(nodeId, SYNC_KEY_SEND_PARAMETER_PATH, parameter.toJson().toByteArray()).await()
            parameterSetAckMutableSharedFlow.first { it == parameter }
        } ?: throw Exception("Timeout while waiting for watch response")
    }

    suspend fun setComplicationColors(colors: ComplicationColors) {
        withTimeoutOrNull(WATCH_RESPONSE_TIMEOUT_MS) {
            messageClient.sendMessage(nodeId, SYNC_KEY_SEND_COMPLICATION_COLORS_PATH, colors.toJsonString().toByteArray()).await()
            complicationColorsAckSharedFlow.first { it == colors }
        } ?: throw Exception("Timeout while waiting for watch response")
    }

    suspend fun requestComplicationsPermission(): Boolean {
        return withTimeoutOrNull(WATCH_RESPONSE_TIMEOUT_MS * 3) {
            messageClient.sendMessage(nodeId, SYNC_KEY_REQUEST_COMPLICATIONS_PERMISSION_PATH, null).await()
            complicationsPermissionRequestAckSharedFlow.first()
        } ?: throw Exception("Timeout while waiting for watch response")
    }

    suspend fun editComplication(complicationLocation: ComplicationLocation) {
        messageClient.sendMessage(
            nodeId,
            SYNC_KEY_EDIT_COMPLICATION_PATH,
            ByteBuffer.allocate(Int.SIZE_BYTES).putInt(complicationLocation.ordinal).array(),
        ).await()
    }

    companion object {
        private const val WATCH_RESPONSE_TIMEOUT_MS = 5000L
        private const val TAG = "SyncSession"
        private val DEBUG_LOGS = BuildConfig.DEBUG
    }
}