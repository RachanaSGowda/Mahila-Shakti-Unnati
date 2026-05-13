package com.example.mahila_shaktiunnati.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mahila_shaktiunnati.data.Savings
import com.example.mahila_shaktiunnati.data.ShgDatabase
import com.example.mahila_shaktiunnati.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(navController: NavController, memberId: Int) {
    val context = LocalContext.current
    val db = ShgDatabase.getDatabase(context)
    val shgDao = db.shgDao()
    val scope = rememberCoroutineScope()

    val members by shgDao.getAllMembers().collectAsState(initial = emptyList())
    var selectedMemberId by remember { mutableIntStateOf(memberId) }
    var amount by remember { mutableStateOf("") }
    
    val savingsHistory by shgDao.getSavingsForMember(selectedMemberId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings Engine", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurplePrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text("Select Member", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            // Member selection items integrated into the main list
            items(members) { member ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .background(
                            if (member.id == selectedMemberId) PurplePrimary.copy(alpha = 0.05f) else Color.White,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (member.id == selectedMemberId) PurplePrimary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .selectable(
                            selected = (member.id == selectedMemberId),
                            onClick = { selectedMemberId = member.id },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (member.id == selectedMemberId),
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = PurplePrimary)
                    )
                    Text(text = "${member.name} (${member.role})", modifier = Modifier.padding(start = 8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Savings Amount (₹)", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = { Text("Enter amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull()
                        if (amt != null && amt > 0 && selectedMemberId != 0) {
                            scope.launch {
                                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                                shgDao.insertSavings(Savings(
                                    memberId = selectedMemberId, 
                                    amount = amt, 
                                    date = dateStr, 
                                    note = "Manual Savings Deposit"
                                ))
                                shgDao.updateMemberSavings(selectedMemberId, amt)
                                Toast.makeText(context, "Savings added successfully", Toast.LENGTH_SHORT).show()
                                amount = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Deposit Savings", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("Recent Activity", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(savingsHistory.sortedByDescending { it.timestamp }) { saving ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(saving.date, fontSize = 11.sp, color = CardTextSummary)
                            Text(saving.note, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            text = if (saving.amount > 0) "+₹${saving.amount}" else "-₹${-saving.amount}",
                            fontWeight = FontWeight.Bold,
                            color = if (saving.amount > 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }
}
