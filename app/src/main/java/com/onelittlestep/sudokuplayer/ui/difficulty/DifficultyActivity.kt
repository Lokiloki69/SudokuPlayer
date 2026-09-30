package com.onelittlestep.sudokuplayer.ui.difficulty

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.onelittlestep.sudokuplayer.R
import com.onelittlestep.sudokuplayer.databinding.ActivityDifficultyBinding
import com.onelittlestep.sudokuplayer.databinding.ItemDifficultyCardBinding
import com.onelittlestep.sudokuplayer.model.Difficulty
import com.onelittlestep.sudokuplayer.ui.game.GameActivity

class DifficultyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDifficultyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDifficultyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        bindCard(
            binding.cardEasy, Difficulty.EASY, "E",
            R.string.difficulty_easy, R.string.difficulty_easy_desc,
            R.color.difficulty_easy_container, R.color.difficulty_easy_on_container
        )
        bindCard(
            binding.cardMedium, Difficulty.MEDIUM, "M",
            R.string.difficulty_medium, R.string.difficulty_medium_desc,
            R.color.difficulty_medium_container, R.color.difficulty_medium_on_container
        )
        bindCard(
            binding.cardHard, Difficulty.HARD, "H",
            R.string.difficulty_hard, R.string.difficulty_hard_desc,
            R.color.difficulty_hard_container, R.color.difficulty_hard_on_container
        )
        bindCard(
            binding.cardExpert, Difficulty.EXPERT, "X",
            R.string.difficulty_expert, R.string.difficulty_expert_desc,
            R.color.difficulty_expert_container, R.color.difficulty_expert_on_container
        )
    }

    private fun bindCard(
        card: ItemDifficultyCardBinding,
        difficulty: Difficulty,
        letter: String,
        nameRes: Int,
        descRes: Int,
        containerColorRes: Int,
        onContainerColorRes: Int
    ) {
        card.textDifficultyName.text = getString(nameRes)
        card.textDifficultyDesc.text = getString(descRes)
        card.textIconLetter.text = letter

        val containerColor = ContextCompat.getColor(this, containerColorRes)
        val onContainerColor = ContextCompat.getColor(this, onContainerColorRes)
        card.frameIcon.backgroundTintList = ColorStateList.valueOf(containerColor)
        card.textIconLetter.setTextColor(onContainerColor)

        card.root.setOnClickListener {
            startActivity(GameActivity.newGameIntent(this, difficulty))
            finish()
        }
        applyPressFeedback(card.root)
    }

    /** Small tactile scale-down/up on touch, purely visual - the click listener still fires normally. */
    @SuppressLint("ClickableViewAccessibility")
    private fun applyPressFeedback(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }
            false
        }
    }
}
