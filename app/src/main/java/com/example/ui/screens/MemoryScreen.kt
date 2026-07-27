package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.database.entity.UserMemoryEntity
import com.example.ui.MainViewModel
import com.example.ui.components.FuturisticHudContainer
import com.example.ui.theme.ArcBorder
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcGold
import com.example.ui.theme.ArcSurface
import com.example.ui.theme.ArcSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val memories by viewModel.allMemories.collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var newFactInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("general") }

    val filteredMemories = memories.filter {
        it.factText.contains(searchQuery, ignoreCase = true) ||
                it.keyCategory.contains(searchQuery, ignoreCase = true)
    }

    FuturisticHudContainer(modifier = modifier.testTag("memory_screen")) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "LOCAL MEMORY BANK",
                            color = ArcCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ArcCyan)
                        }
                    },
                    actions = {
                        if (memories.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearAllMemories() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear All Memories", tint = ErrorRed)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ArcSurface.copy(alpha = 0.85f)
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add New Memory Input Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArcSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ArcBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "STORE USER MEMORY / FACT",
                            color = ArcGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newFactInput,
                            onValueChange = { newFactInput = it },
                            placeholder = { Text("e.g. My project is Human-Powered Agricultural Machine", color = TextSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("memory_fact_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = ArcBorder.copy(alpha = 0.5f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (newFactInput.isNotBlank()) {
                                    val cat = if (newFactInput.lowercase().contains("project")) "project" else "general"
                                    viewModel.addMemoryNote(cat, newFactInput.trim())
                                    newFactInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = ArcSurface),
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("save_memory_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SAVE TO MEMORY", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Search Filter Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search saved memory entries...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ArcCyan) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("memory_search_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = ArcBorder.copy(alpha = 0.5f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Memory Entries List
                if (filteredMemories.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "NO SAVED MEMORY ENTRIES",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Tell JARVIS \"Remember that [fact]\" to save info automatically.",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMemories, key = { it.id }) { memory ->
                            MemoryCardItem(
                                memory = memory,
                                onDelete = { viewModel.deleteMemoryNote(memory.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryCardItem(
    memory: UserMemoryEntity,
    onDelete: () -> Unit
) {
    val dateStr = remember(memory.createdAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(memory.createdAt))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = ArcSurface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ArcBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .testTag("memory_item_${memory.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CATEGORY: ${memory.keyCategory.uppercase()}",
                    color = ArcGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = memory.factText,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateStr,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Memory", tint = ErrorRed.copy(alpha = 0.8f))
            }
        }
    }
}
