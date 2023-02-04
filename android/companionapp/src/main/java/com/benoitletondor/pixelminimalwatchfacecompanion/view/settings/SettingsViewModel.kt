package com.benoitletondor.pixelminimalwatchfacecompanion.view.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.MutableLiveFlow
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import com.google.android.gms.wearable.Node
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sync: Sync,
) : ViewModel() {
    private val stateMutableFlow = MutableStateFlow<State>(State.Loading)
    val stateFlow: StateFlow<State> = stateMutableFlow

    private val eventMutableFlow = MutableLiveFlow<Event>()
    val eventFlow: Flow<Event> = eventMutableFlow

    init {
        loadNodes()
    }

    private fun loadNodes() {
        if (DEBUG_LOGS) Log.d(TAG, "loadNodes: start")

        stateMutableFlow.value = State.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nodes = sync.getConnectedNodesWithWatchFaceCapability()

                if (DEBUG_LOGS) Log.d(TAG, "loadNodes: nodes: $nodes")

                if (nodes.isEmpty()) {
                    stateMutableFlow.value = State.NoNodesAvailable
                } else {
                    stateMutableFlow.value = State.NodesAvailable(nodes)
                }
            } catch (e: Exception) {
                if (e is CancellationException) { throw e }

                Log.e(TAG, "Error loading nodes", e)
                stateMutableFlow.value = State.ErrorLoadingNodes(e)
            }
        }
    }

    fun onRetryButtonPressed() {
        loadNodes()
    }

    fun onInstallWatchFaceButtonPressed() {
        viewModelScope.launch {
            eventMutableFlow.emit(Event.NavigateToInstallWatchFaceScreen)
        }
    }

    fun onNodeSelected(node: Node) {
        viewModelScope.launch {
            eventMutableFlow.emit(Event.NavigateToNodeView(node))
        }
    }

    sealed class State {
        object Loading : State()
        object NoNodesAvailable : State()
        class NodesAvailable(val nodes: Set<Node>) : State()
        class ErrorLoadingNodes(val error: Exception) : State()
    }

    sealed class Event {
        class NavigateToNodeView(val node: Node) : Event()
        object NavigateToInstallWatchFaceScreen : Event()
    }

    companion object {
        private const val TAG = "SettingsViewModel"
        private val DEBUG_LOGS = BuildConfig.DEBUG
    }
}