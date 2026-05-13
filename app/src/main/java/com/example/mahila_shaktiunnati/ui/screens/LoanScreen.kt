package com.example.mahila_shaktiunnati.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.example.mahila_shaktiunnati.data.Loan
import com.example.mahila_shaktiunnati.data.ShgDatabase
import com.example.mahila_shaktiunnati.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanScreen(navController: NavController, memberId: Int) {
    val context = LocalContext.current
    val db = ShgDatabase.getDatabase(context)
    val shgDao = db.shgDao()
    val loanDao = db.loanDao()
    val scope = rememberCoroutineScope()

    val members by shgDao.getAllMembers().collectAsState(initial = emptyList())
    var selectedMemberId by remember { mutableIntStateOf(memberId) }
    
    var principal by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("12") }
    var timePeriod by remember { mutableStateOf("12") }
    
    var interestAmount by remember { mutableDoubleStateOf(0.0) }
    var totalRepayment by remember { mutableDoubleStateOf(0.0) }
    var calculated by remember { mutableStateOf(false) }
    var isSavedSuccessfully by remember { mutableStateOf(false) }

    if (isSavedSuccessfully) {
        LoanSuccessContent(
            principal = principal.toDoubleOrNull() ?: 0.0,
            rate = interestRate.toDoubleOrNull() ?: 0.0,
            period = timePeriod.toIntOrNull() ?: 0,
            interest = interestAmount,
            total = totalRepayment,
            onOk = { navController.popBackStack() }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Loan Activation", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
                if (memberId == 0) {
                    item {
                        Text("Select Member", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    
                    items(members.filter { it.activeLoan == 0.0 }) { member ->
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
                    
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }

                item {
                    Text("Principal Amount (₹)", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = CardTextTitle)
                    OutlinedTextField(
                        value = principal,
                        onValueChange = { principal = it; calculated = false },
                        placeholder = { Text("Enter principal amount") },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                    )

                    Text("Interest Rate (%)", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = CardTextTitle)
                    OutlinedTextField(
                        value = interestRate,
                        onValueChange = { interestRate = it; calculated = false },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                    )

                    Text("Time Period (Months)", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = CardTextTitle)
                    OutlinedTextField(
                        value = timePeriod,
                        onValueChange = { timePeriod = it; calculated = false },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                    )

                    Button(
                        onClick = {
                            val p = principal.toDoubleOrNull() ?: 0.0
                            val r = interestRate.toDoubleOrNull() ?: 0.0
                            val t = timePeriod.toDoubleOrNull() ?: 0.0
                            interestAmount = (p * r * (t / 12)) / 100
                            totalRepayment = p + interestAmount
                            calculated = true
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Text("Calculate", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    if (calculated) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CalculationResultCard(
                                label = "Interest Amount",
                                value = "₹ ${String.format(Locale.getDefault(), "%,.2f", interestAmount)}",
                                valueColor = PurplePrimary,
                                modifier = Modifier.weight(1f)
                            )
                            CalculationResultCard(
                                label = "Total Repayment",
                                value = "₹ ${String.format(Locale.getDefault(), "%,.2f", totalRepayment)}",
                                valueColor = Color(0xFF2E7D32),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (selectedMemberId != 0) {
                                    scope.launch {
                                        val loan = Loan(
                                            memberId = selectedMemberId,
                                            principalAmount = principal.toDouble(),
                                            interestRate = interestRate.toDouble(),
                                            timePeriodMonths = timePeriod.toInt(),
                                            interestAmount = interestAmount,
                                            totalRepayment = totalRepayment,
                                            currentBalance = totalRepayment
                                        )
                                        loanDao.insertLoan(loan)
                                        shgDao.updateMemberActiveLoan(selectedMemberId, totalRepayment)
                                        isSavedSuccessfully = true
                                    }
                                } else {
                                    Toast.makeText(context, "Please select a member", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Activate Loan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanSuccessContent(
    principal: Double,
    rate: Double,
    period: Int,
    interest: Double,
    total: Double,
    onOk: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Loan Activated", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PurplePrimary,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier.size(80.dp).background(Color(0xFF4CAF50), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Loan Activated Successfully!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CardTextTitle)
            Text("The member can now repay from savings.", fontSize = 14.sp, color = CardTextSummary)
            Spacer(modifier = Modifier.height(32.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Loan Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CardTextTitle)
                Spacer(modifier = Modifier.height(16.dp))
                LoanSummaryRow("Principal Amount", "₹ ${String.format(Locale.getDefault(), "%,.2f", principal)}")
                LoanSummaryRow("Interest Rate", "${rate.toInt()}%")
                LoanSummaryRow("Time Period", "$period Months")
                LoanSummaryRow("Interest Amount", "₹ ${String.format(Locale.getDefault(), "%,.2f", interest)}")
                LoanSummaryRow("Total Repayment", "₹ ${String.format(Locale.getDefault(), "%,.2f", total)}")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onOk,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("OK", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun LoanSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = CardTextSummary, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, color = CardTextTitle, fontSize = 14.sp)
    }
}

@Composable
fun CalculationResultCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontSize = 11.sp, color = CardTextSummary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}
