package com.example.memorycardgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// Model section
data class MemoryCard(
    val id: Int,
    val symbol: String,
    val isUp: Boolean = false,
    val isMatched: Boolean = false
)

private val POSSIBLE_SYMBOLS = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
    "L", "M", "N") // total 14 pairs --> 28 matches, could use emojis but nah

enum class Difficulty(val label: String, val pairs: Int, val columns: Int){
    SIX("6 pairs", pairs = 6, columns = 3),
    EIGHT("8 pairs", pairs = 8, columns = 4),
    TEN("10 pairs", pairs = 10, columns = 5),
    TWELVE("12 pairs", pairs = 12, columns = 4),
    FOURTEEN("14 pairs", pairs = 14, columns = 4)
}

fun createDeck(pairs: Int): List<MemoryCard> {
    val chosen = POSSIBLE_SYMBOLS.shuffled().take(pairs)
    return (chosen + chosen)
        .shuffled()
        .mapIndexed { index, symbol -> MemoryCard(id = index, symbol =symbol) }
}

// Main activity section, aka what's its boot screen?
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                RootScreen()
            }
        }
    }
}
}


// UI section hehehe

@Composable
fun RootScreen() {
    var difficulty by remember { mutableStateOf<Difficulty?>(null) }
    val chosen = difficulty
    if (chosen == null) {
        DifficultyScreen(onSelect = { difficulty = it })
    } else {
        MemoryCardGameScreen(
            difficulty = chosen,
            onChangeDifficulty = { difficulty = null }
        )
    }
}

@Composable
fun DifficultyScreen(onSelect: (Difficulty) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("A Simple Memory Game", fontSize = 32.sp)
        Text(
            "Choose your difficulty!",
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        Difficulty.values().forEach { level ->
            Button(
                onClick = { onSelect(level) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(level.label)
            }
        }
    }
}


@Composable
fun MemoryCardGameScreen(difficulty: Difficulty, onChangeDifficulty: () -> Unit){
    var cards by remember { mutableStateOf(createDeck(difficulty.pairs)) }
    var selected by remember { mutableStateOf<List<Int>>(emptyList())}
    var moves by remember { mutableIntStateOf(0) }

    val hasWon = cards.all { it.isMatched }

    fun onCardClick(index: Int) {
        val card = cards[index]
        if (card.isUp || card.isMatched) return

        // A mismatched pair stays visible until the next click, then flips back
        var current = cards
        var pending = selected
        if (pending.size == 2) {
            current = current.mapIndexed { i, c ->
                if (i in pending) c.copy(isUp = false) else c
            }
            pending = emptyList()
        }

        // Flip the clicked card
        current = current.mapIndexed { i, c ->
            if (i == index) c.copy(isUp = true) else c
        }
        pending = pending + index

        // Check for a match once two cards are up
        if (pending.size == 2) {
            moves++
            val (a, b) = pending
            if (current[a].symbol == current[b].symbol) {
                current = current.mapIndexed { i, c ->
                    if (i == a || i == b) c.copy(isMatched = true) else c
                }
                pending = emptyList()
            }
        }

        cards = current
        selected = pending

    }

    fun restart() {
        cards = createDeck(difficulty.pairs)
        selected = emptyList()
        moves = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("A Simple Memory Game", fontSize = 28.sp)
        Text("Moves: $moves", fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))

        if (hasWon) {
            Text("You won in $moves moves! 🎉🎉🎉", fontSize = 20.sp)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(difficulty.columns),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            itemsIndexed(cards, key = { _, card -> card.id }) { index, card ->
                CardView(card = card, onClick = { onCardClick(index) })
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { restart() }) {
                Text("New game")
            }
            Button(onClick = onChangeDifficulty){
                Text("Main menu")
            }
        }

    }
}

@Composable
fun CardView(card: MemoryCard, onClick: () -> Unit) {
    val showFace = card.isUp || card.isMatched
    val background = when {
        card.isMatched -> Color(0x00FFFFFF) // clear
        card.isUp -> Color.White
        else -> Color(0xFF673AB7)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
    ) {
        if (showFace) {
            Text(card.symbol, fontSize = 32.sp)
        }
    }
}