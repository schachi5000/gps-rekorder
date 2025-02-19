package pro.schacher.gpsrekorder.shared.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pro.schacher.gpsrekorder.shared.AppLogger
import pro.schacher.gpsrekorder.shared.location.LocationDataSource
import pro.schacher.gpsrekorder.shared.model.Session
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PlaybackRepository(private val locationDataSource: LocationDataSource) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow<RecordingState?>(null)

    val state = this._state.asStateFlow()

    private var playbackJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    fun startPlayback(session: Session) {
        _state.update {
            RecordingState(session, 0)
        }

        this.playbackJob = this.scope.launch(Dispatchers.IO) {
            while (this.isActive) {
                session.path.forEachIndexed { index, location ->
                    if (this.isActive) {
                        _state.update {
                            it?.copy(currentIndex = index)
                        }

                        locationDataSource.updateTestLocation(location)

                        val delay = session.path.getOrNull(index + 1)?.let {
                            (it.timestamp - location.timestamp).milliseconds
                        } ?: 1.seconds

                        AppLogger.d { "Playback delay: $delay" }
                        delay(delay)
                    }
                }
            }

            stopPlayback()
        }
    }

    fun stopPlayback() {
        this.playbackJob = null
        _state.update { null }
    }
}

data class RecordingState(
    val session: Session,
    val currentIndex: Int
)