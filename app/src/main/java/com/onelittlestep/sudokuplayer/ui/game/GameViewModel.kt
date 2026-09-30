package com.onelittlestep.sudokuplayer.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.onelittlestep.sudokuplayer.model.Difficulty
import com.onelittlestep.sudokuplayer.model.GameState
import com.onelittlestep.sudokuplayer.model.GameStatus
import com.onelittlestep.sudokuplayer.model.Move
import com.onelittlestep.sudokuplayer.repository.GameRepository
import com.onelittlestep.sudokuplayer.repository.SettingsRepository
import com.onelittlestep.sudokuplayer.repository.StatisticsRepository
import com.onelittlestep.sudokuplayer.sudoku.SudokuGenerator
import com.onelittlestep.sudokuplayer.sudoku.SudokuValidator
import com.onelittlestep.sudokuplayer.utils.Event
import com.onelittlestep.sudokuplayer.utils.SoundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val gameRepository = GameRepository(application)
    private val statisticsRepository = StatisticsRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val soundManager = SoundManager(application)

    private var state: GameState = GameState.newGame(Difficulty.MEDIUM, IntArray(81), IntArray(81))
    private var timerJob: Job? = null
    private var initialized = false

    private val _uiState = MutableLiveData<GameUiState>()
    val uiState: LiveData<GameUiState> = _uiState

    private val _elapsedSeconds = MutableLiveData<Long>()
    val elapsedSeconds: LiveData<Long> = _elapsedSeconds

    private val _events = MutableLiveData<Event<GameEvent>>()
    val events: LiveData<Event<GameEvent>> = _events

    private val _isGenerating = MutableLiveData(false)
    val isGenerating: LiveData<Boolean> = _isGenerating

    val currentDifficulty: Difficulty get() = state.difficulty

    /** Loads a resumable saved game, or generates a fresh one. Safe to call more than once. */
    fun start(requestedDifficulty: Difficulty?, forceNewGame: Boolean) {
        if (initialized) return
        initialized = true

        val saved = if (forceNewGame) null else gameRepository.loadGame()
        if (saved != null && saved.status != GameStatus.COMPLETED) {
            state = saved
            if (state.status == GameStatus.PLAYING) startTimer()
            publish()
        } else {
            generateNewGame(requestedDifficulty ?: saved?.difficulty ?: Difficulty.MEDIUM)
        }
    }

    fun generateNewGame(difficulty: Difficulty) {
        timerJob?.cancel()
        _isGenerating.value = true
        viewModelScope.launch {
            val generated = withContext(Dispatchers.Default) {
                SudokuGenerator.generate(difficulty)
            }
            state = GameState.newGame(difficulty, generated.puzzle, generated.solution)
            statisticsRepository.recordGameStarted(difficulty)
            _isGenerating.value = false
            startTimer()
            saveAndPublish()
        }
    }

    fun restart() {
        val old = state
        timerJob?.cancel()
        state = GameState.newGame(old.difficulty, old.puzzle, old.solution)
        startTimer()
        saveAndPublish()
    }

    fun selectCell(index: Int) {
        if (state.status != GameStatus.PLAYING || index !in 0 until GameState.SIZE) return
        state.selectedIndex = index
        publish()
    }

    fun toggleNotesMode() {
        if (state.status != GameStatus.PLAYING) return
        state.notesMode = !state.notesMode
        publish()
    }

    fun inputNumber(value: Int) {
        if (state.status != GameStatus.PLAYING) return
        val index = state.selectedIndex
        if (index !in 0 until GameState.SIZE) return
        if (state.given[index] || index in state.hintCells) return

        val settings = settingsRepository.getSettings()

        if (state.notesMode) {
            if (state.board[index] != 0) return
            val prevNotes = state.notes[index].toSet()
            val newNotes = prevNotes.toMutableSet()
            if (value in newNotes) newNotes.remove(value) else newNotes.add(value)
            state.notes[index] = newNotes
            pushMove(Move(index, 0, 0, prevNotes, newNotes, false, false, 0))
            soundManager.playTap(settings.soundEnabled)
            saveAndPublish()
            return
        }

        if (state.board[index] == value) return

        val prevValue = state.board[index]
        val prevNotes = state.notes[index].toSet()
        state.board[index] = value
        state.checkedErrorCells.remove(index)

        var mistakeDelta = 0
        val correct = value == state.solution[index]
        if (!correct) {
            state.mistakes++
            mistakeDelta = 1
            soundManager.playError(settings.soundEnabled)
            soundManager.vibrate(settings.vibrationEnabled)
        } else {
            soundManager.playTap(settings.soundEnabled)
        }

        var newNotes: Set<Int> = prevNotes
        if (settings.autoRemoveNotes) {
            state.notes[index] = mutableSetOf()
            newNotes = emptySet()
            for (peer in SudokuValidator.peersOf(index)) {
                state.notes[peer].remove(value)
            }
        }

        pushMove(Move(index, prevValue, value, prevNotes, newNotes, false, false, mistakeDelta))
        finishTurn()
    }

    fun eraseCell() {
        if (state.status != GameStatus.PLAYING) return
        val index = state.selectedIndex
        if (index !in 0 until GameState.SIZE) return
        if (state.given[index] || index in state.hintCells) return

        val prevValue = state.board[index]
        val prevNotes = state.notes[index].toSet()
        if (prevValue == 0 && prevNotes.isEmpty()) return

        if (prevValue != 0) {
            state.board[index] = 0
            state.checkedErrorCells.remove(index)
            pushMove(Move(index, prevValue, 0, prevNotes, prevNotes, false, false, 0))
        } else {
            state.notes[index] = mutableSetOf()
            pushMove(Move(index, 0, 0, prevNotes, emptySet(), false, false, 0))
        }
        saveAndPublish()
    }

    fun undo() {
        if (state.undoStack.isEmpty()) return
        val move = state.undoStack.removeAt(state.undoStack.size - 1)
        state.board[move.cellIndex] = move.prevValue
        state.notes[move.cellIndex] = move.prevNotes.toMutableSet()
        if (move.wasHint) state.hintCells.add(move.cellIndex) else state.hintCells.remove(move.cellIndex)
        state.mistakes = (state.mistakes - move.mistakeDelta).coerceAtLeast(0)
        state.checkedErrorCells.remove(move.cellIndex)
        state.redoStack.add(move)
        if (state.status == GameStatus.GAME_OVER || state.status == GameStatus.COMPLETED) {
            state.status = GameStatus.PLAYING
            startTimer()
        }
        saveAndPublish()
    }

    fun redo() {
        if (state.redoStack.isEmpty()) return
        val move = state.redoStack.removeAt(state.redoStack.size - 1)
        state.board[move.cellIndex] = move.newValue
        state.notes[move.cellIndex] = move.newNotes.toMutableSet()
        if (move.isHint) state.hintCells.add(move.cellIndex) else state.hintCells.remove(move.cellIndex)
        state.mistakes += move.mistakeDelta
        state.undoStack.add(move)
        finishTurn()
    }

    fun hint() {
        if (state.status != GameStatus.PLAYING) return
        val emptyIndices = (0 until GameState.SIZE).filter { state.board[it] == 0 }
        if (emptyIndices.isEmpty()) return

        val index = emptyIndices.random()
        val prevValue = state.board[index]
        val prevNotes = state.notes[index].toSet()
        val revealed = state.solution[index]

        state.board[index] = revealed
        state.notes[index] = mutableSetOf()
        state.hintCells.add(index)
        state.hintsUsed++
        state.selectedIndex = index

        val settings = settingsRepository.getSettings()
        soundManager.playTap(settings.soundEnabled)

        pushMove(Move(index, prevValue, revealed, prevNotes, emptySet(), false, true, 0))
        finishTurn()
    }

    fun check() {
        val wrong = (0 until GameState.SIZE).filter {
            state.board[it] != 0 && state.board[it] != state.solution[it]
        }
        state.checkedErrorCells = wrong.toMutableSet()
        _events.value = Event(GameEvent.CheckResult(wrong.size))
        saveAndPublish()
    }

    fun pause() {
        if (state.status != GameStatus.PLAYING) return
        state.status = GameStatus.PAUSED
        timerJob?.cancel()
        saveAndPublish()
    }

    fun resume() {
        if (state.status != GameStatus.PAUSED) return
        state.status = GameStatus.PLAYING
        startTimer()
        saveAndPublish()
    }

    fun persistState() {
        gameRepository.saveGame(state)
    }

    private fun pushMove(move: Move) {
        state.undoStack.add(move)
        if (state.undoStack.size > 300) state.undoStack.removeAt(0)
        state.redoStack.clear()
    }

    private fun finishTurn() {
        val settings = settingsRepository.getSettings()
        if (state.isComplete()) {
            state.status = GameStatus.COMPLETED
            timerJob?.cancel()
            statisticsRepository.recordGameEnded(
                state.difficulty, true, state.elapsedSeconds, state.mistakes, state.hintsUsed
            )
            soundManager.playSuccess(settings.soundEnabled)
            gameRepository.clearGame()
            _events.value = Event(
                GameEvent.Completed(state.difficulty, state.elapsedSeconds, state.mistakes, state.hintsUsed)
            )
            publish()
            return
        }

        if (settings.mistakeLimit > 0 && state.mistakes >= settings.mistakeLimit) {
            state.status = GameStatus.GAME_OVER
            timerJob?.cancel()
            statisticsRepository.recordGameEnded(
                state.difficulty, false, state.elapsedSeconds, state.mistakes, state.hintsUsed
            )
            gameRepository.clearGame()
            _events.value = Event(GameEvent.GameOver)
            publish()
            return
        }

        saveAndPublish()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && state.status == GameStatus.PLAYING) {
                delay(1000)
                if (state.status == GameStatus.PLAYING) {
                    state.elapsedSeconds++
                    _elapsedSeconds.postValue(state.elapsedSeconds)
                }
            }
        }
    }

    private fun saveAndPublish() {
        gameRepository.saveGame(state)
        publish()
    }

    private fun publish() {
        _uiState.value = buildUiState()
        _elapsedSeconds.value = state.elapsedSeconds
    }

    private fun liveWrongCells(): Set<Int> = (0 until GameState.SIZE).filter {
        state.board[it] != 0 && state.board[it] != state.solution[it]
    }.toSet()

    private fun buildUiState(): GameUiState {
        val settings = settingsRepository.getSettings()
        val selected = state.selectedIndex
        val related: Set<Int> = if (selected in 0 until GameState.SIZE) {
            SudokuValidator.peersOf(selected).toSet() + selected
        } else emptySet()
        val sameNumber: Set<Int> = if (selected in 0 until GameState.SIZE && state.board[selected] != 0) {
            val v = state.board[selected]
            (0 until GameState.SIZE).filter { state.board[it] == v && it != selected }.toSet()
        } else emptySet()
        val conflictCells = if (settings.highlightConflicts) {
            SudokuValidator.conflictingIndices(state.board)
        } else emptySet()
        val liveErrors = if (settings.highlightConflicts) liveWrongCells() else emptySet()
        val errorCells = liveErrors + state.checkedErrorCells

        val remaining = IntArray(10)
        for (v in 1..9) {
            remaining[v] = 9 - state.board.count { it == v }
        }

        return GameUiState(
            board = state.board.copyOf(),
            given = state.given,
            notes = Array(GameState.SIZE) { state.notes[it].toSet() },
            selectedIndex = selected,
            hintCells = state.hintCells.toSet(),
            errorCells = errorCells,
            conflictCells = conflictCells,
            sameNumberCells = sameNumber,
            relatedCells = related,
            mistakes = state.mistakes,
            mistakeLimit = settings.mistakeLimit,
            hintsUsed = state.hintsUsed,
            difficulty = state.difficulty,
            elapsedSeconds = state.elapsedSeconds,
            status = state.status,
            notesMode = state.notesMode,
            canUndo = state.undoStack.isNotEmpty(),
            canRedo = state.redoStack.isNotEmpty(),
            remainingCounts = remaining
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundManager.release()
    }
}
