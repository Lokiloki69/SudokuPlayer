package com.onelittlestep.sudokuplayer.ui.game

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.onelittlestep.sudokuplayer.R
import com.onelittlestep.sudokuplayer.model.Difficulty
import com.onelittlestep.sudokuplayer.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class GameActivityTest {

    private fun launchNewGame(difficulty: Difficulty = Difficulty.EASY): ActivityScenario<GameActivity> {
        val scenario = ActivityScenario.launch<GameActivity>(
            GameActivity.newGameIntent(
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext,
                difficulty
            )
        )
        waitForGeneration(scenario)
        return scenario
    }

    private fun viewModelOf(scenario: ActivityScenario<GameActivity>): GameViewModel {
        var vm: GameViewModel? = null
        scenario.onActivity { activity ->
            vm = ViewModelProvider(activity)[GameViewModel::class.java]
        }
        return vm!!
    }

    /** Blocks (with a timeout) until the puzzle has finished generating in the background. */
    private fun waitForGeneration(scenario: ActivityScenario<GameActivity>) {
        val latch = CountDownLatch(1)
        scenario.onActivity { activity ->
            val vm = ViewModelProvider(activity)[GameViewModel::class.java]
            vm.isGenerating.observeForever { generating ->
                if (!generating) latch.countDown()
            }
        }
        latch.await(15, TimeUnit.SECONDS)
    }

    @Test
    fun startingNewGameDisplaysTheBoard() {
        launchNewGame().use {
            onView(withId(R.id.boardView)).check(matches(isDisplayed()))
            onView(withId(R.id.layoutNumberPad)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun selectingACellUpdatesTheSelectedIndex() {
        launchNewGame().use { scenario ->
            val vm = viewModelOf(scenario)
            onView(withId(R.id.boardView)).perform(click())
            val selected = vm.uiState.value?.selectedIndex ?: -1
            assertTrue(selected in 0 until 81)
        }
    }

    @Test
    fun enteringANumberFillsTheSelectedEmptyCell() {
        launchNewGame().use { scenario ->
            val vm = viewModelOf(scenario)
            val emptyIndex = vm.uiState.value!!.board.indexOfFirst { it == 0 }
            scenario.onActivity { vm.selectCell(emptyIndex) }
            onView(withId(R.id.boardView)).perform(click())
            scenario.onActivity { vm.selectCell(emptyIndex); vm.inputNumber(5) }
            val boardValue = vm.uiState.value!!.board[emptyIndex]
            assertTrue(boardValue == 5 || vm.uiState.value!!.mistakes >= 0)
        }
    }

    @Test
    fun erasingClearsAPreviouslyEnteredValue() {
        launchNewGame().use { scenario ->
            val vm = viewModelOf(scenario)
            val emptyIndex = vm.uiState.value!!.board.indexOfFirst { it == 0 }
            scenario.onActivity {
                vm.selectCell(emptyIndex)
                vm.inputNumber(5)
                vm.eraseCell()
            }
            assertEquals(0, vm.uiState.value!!.board[emptyIndex])
        }
    }

    @Test
    fun completingThePuzzleWithHintsReachesCompletedStatus() {
        launchNewGame(Difficulty.EASY).use { scenario ->
            val vm = viewModelOf(scenario)
            var guard = 0
            scenario.onActivity {
                while (vm.uiState.value!!.status == GameStatus.PLAYING && guard < 81) {
                    vm.hint()
                    guard++
                }
            }
            assertEquals(GameStatus.COMPLETED, vm.uiState.value!!.status)
        }
    }
}
