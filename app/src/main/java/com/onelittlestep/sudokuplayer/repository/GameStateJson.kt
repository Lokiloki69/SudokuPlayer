package com.onelittlestep.sudokuplayer.repository

import com.onelittlestep.sudokuplayer.model.Difficulty
import com.onelittlestep.sudokuplayer.model.GameState
import com.onelittlestep.sudokuplayer.model.GameStatus
import com.onelittlestep.sudokuplayer.model.Move
import org.json.JSONArray
import org.json.JSONObject

/** Manual JSON (de)serialization for [GameState] so it can survive process death via SharedPreferences. */
object GameStateJson {

    private fun IntArray.toJsonArray(): JSONArray {
        val arr = JSONArray()
        for (v in this) arr.put(v)
        return arr
    }

    private fun JSONArray.toIntArray(): IntArray = IntArray(length()) { getInt(it) }

    private fun BooleanArray.toJsonArray(): JSONArray {
        val arr = JSONArray()
        for (v in this) arr.put(v)
        return arr
    }

    private fun JSONArray.toBooleanArray(): BooleanArray = BooleanArray(length()) { getBoolean(it) }

    private fun Set<Int>.toJsonArray(): JSONArray {
        val arr = JSONArray()
        for (v in this) arr.put(v)
        return arr
    }

    private fun JSONArray.toIntSet(): MutableSet<Int> {
        val set = mutableSetOf<Int>()
        for (i in 0 until length()) set.add(getInt(i))
        return set
    }

    private fun moveToJson(move: Move): JSONObject = JSONObject().apply {
        put("cellIndex", move.cellIndex)
        put("prevValue", move.prevValue)
        put("newValue", move.newValue)
        put("prevNotes", move.prevNotes.toJsonArray())
        put("newNotes", move.newNotes.toJsonArray())
        put("wasHint", move.wasHint)
        put("isHint", move.isHint)
        put("mistakeDelta", move.mistakeDelta)
    }

    private fun moveFromJson(json: JSONObject): Move = Move(
        cellIndex = json.getInt("cellIndex"),
        prevValue = json.getInt("prevValue"),
        newValue = json.getInt("newValue"),
        prevNotes = json.getJSONArray("prevNotes").toIntSet(),
        newNotes = json.getJSONArray("newNotes").toIntSet(),
        wasHint = json.getBoolean("wasHint"),
        isHint = json.getBoolean("isHint"),
        mistakeDelta = json.getInt("mistakeDelta")
    )

    private fun movesToJson(moves: List<Move>): JSONArray {
        val arr = JSONArray()
        for (m in moves) arr.put(moveToJson(m))
        return arr
    }

    private fun movesFromJson(arr: JSONArray): MutableList<Move> {
        val list = mutableListOf<Move>()
        for (i in 0 until arr.length()) list.add(moveFromJson(arr.getJSONObject(i)))
        return list
    }

    fun toJson(state: GameState): JSONObject = JSONObject().apply {
        put("difficulty", state.difficulty.name)
        put("puzzle", state.puzzle.toJsonArray())
        put("solution", state.solution.toJsonArray())
        put("board", state.board.toJsonArray())
        put("given", state.given.toJsonArray())
        val notesArr = JSONArray()
        for (noteSet in state.notes) notesArr.put(noteSet.toJsonArray())
        put("notes", notesArr)
        put("hintCells", state.hintCells.toJsonArray())
        put("selectedIndex", state.selectedIndex)
        put("mistakes", state.mistakes)
        put("hintsUsed", state.hintsUsed)
        put("elapsedSeconds", state.elapsedSeconds)
        put("status", state.status.name)
        put("notesMode", state.notesMode)
        put("undoStack", movesToJson(state.undoStack))
        put("redoStack", movesToJson(state.redoStack))
        put("checkedErrorCells", state.checkedErrorCells.toJsonArray())
    }

    fun fromJson(json: JSONObject): GameState {
        val notesArr = json.getJSONArray("notes")
        val notes = Array(notesArr.length()) { i -> notesArr.getJSONArray(i).toIntSet() }
        return GameState(
            difficulty = Difficulty.fromName(json.getString("difficulty")),
            puzzle = json.getJSONArray("puzzle").toIntArray(),
            solution = json.getJSONArray("solution").toIntArray(),
            board = json.getJSONArray("board").toIntArray(),
            given = json.getJSONArray("given").toBooleanArray(),
            notes = notes,
            hintCells = json.getJSONArray("hintCells").toIntSet(),
            selectedIndex = json.getInt("selectedIndex"),
            mistakes = json.getInt("mistakes"),
            hintsUsed = json.getInt("hintsUsed"),
            elapsedSeconds = json.getLong("elapsedSeconds"),
            status = GameStatus.valueOf(json.getString("status")),
            notesMode = json.getBoolean("notesMode"),
            undoStack = movesFromJson(json.getJSONArray("undoStack")),
            redoStack = movesFromJson(json.getJSONArray("redoStack")),
            checkedErrorCells = json.getJSONArray("checkedErrorCells").toIntSet()
        )
    }
}
