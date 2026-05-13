package com.example.mahila_shaktiunnati.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mahila_shaktiunnati.data.*
import com.example.mahila_shaktiunnati.ui.theme.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailsScreen(navController: NavController, memberId: Int) {
    val context = LocalContext.current
    val db = ShgDatabase.getDatabase(context)
    val shgDao = db.shgDao()
    val loanDao = db.loanDao()
    val repaymentDao = db.repaymentDao()
    val scope = rememberCoroutineScope()
    
    val member by shgDao.getMemberById(memberId).collectAsState(initial = null)
    var activeLoan by remember { mutableStateOf<Loan?>(null) }
    var repayments by remember { mutableStateOf<List<LoanRepayment>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRepayDialog by remember { mutableStateOf(false) }
    var showInterestDeductDialog by remember { mutableStateOf(false) }

    LaunchedEffect(memberId) {
        activeLoan = loanDao.getActiveLoanForMember(memberId)
        activeLoan?.let {
            repaymentDao.getRepaymentsForLoan(it.id).collect { list ->
                repayments = list
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Member") },
            text = { Text("Are you sure you want to delete ${member?.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            member?.let { 
                                shgDao.deleteMember(it)
                                navController.popBackStack()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showInterestDeductDialog) {
        AlertDialog(
            onDismissRequest = { showInterestDeductDialog = false },
            title = { Text("Deduct Interest") },
            text = { 
                Text("Confirm deduction of ₹ ${String.format(Locale.getDefault(), "%.2f", activeLoan?.interestAmount ?: 0.0)} from ${member?.name}'s savings?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            activeLoan?.let { loan ->
                                if ((member?.totalSavings ?: 0.0) >= loan.interestAmount) {
                                    shgDao.deductMemberSavings(memberId, loan.interestAmount)
                                    shgDao.insertSavings(Savings(
                                        memberId = memberId,
                                        amount = -loan.interestAmount,
                                        date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                                        note = "Interest Deduction"
                                    ))
                                    repaymentDao.insertRepayment(LoanRepayment(
                                        loanId = loan.id,
                                        amount = loan.interestAmount,
                                        note = "Interest from Savings"
                                    ))
                                    Toast.makeText(context, "Interest deducted", Toast.LENGTH_SHORT).show()
                                    showInterestDeductDialog = false
                                } else {
                                    Toast.makeText(context, "Insufficient savings!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Deduct")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInterestDeductDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRepayDialog) {
        AlertDialog(
            onDismissRequest = { showRepayDialog = false },
            title = { Text("Confirm Repayment") },
            text = { Text("Mark the full amount as repaid?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            activeLoan?.let { loan ->
                                repaymentDao.insertRepayment(LoanRepayment(
                                    loanId = loan.id, 
                                    amount = loan.totalRepayment, 
                                    note = "Full Repayment"
                                ))
                                loanDao.payOffLoan(loan.id)
                                shgDao.updateMemberActiveLoan(memberId, 0.0)
                                activeLoan = null
                                showRepayDialog = false
                            }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRepayDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Member Details", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
    ) { padding ->
        member?.let { m ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Profile Section
                item {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        val initials = m.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase()
                        Box(modifier = Modifier.size(80.dp).background(PurplePrimary, CircleShape), contentAlignment = Alignment.Center) {
                            Text(text = initials, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = m.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CardTextTitle)
                        Text(text = m.role, fontSize = 14.sp, color = CardTextSummary)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Balance Cards
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DetailStatCard(
                            title = "Savings Balance",
                            value = "₹ ${String.format(Locale.getDefault(), "%,.2f", m.totalSavings)}",
                            valueColor = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                        DetailStatCard(
                            title = "Active Loan",
                            value = "₹ ${String.format(Locale.getDefault(), "%,.2f", m.activeLoan)}",
                            valueColor = if (m.activeLoan > 0) Color(0xFFD32F2F) else Color(0xFF757575),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Loan Section
                item {
                    if (m.activeLoan > 0 && activeLoan != null) {
                        Text(text = "Active Loan Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CardTextTitle)
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                LoanDetailRow("Principal", "₹ ${String.format(Locale.getDefault(), "%,.2f", activeLoan!!.principalAmount)}")
                                LoanDetailRow("Interest (12%)", "₹ ${String.format(Locale.getDefault(), "%,.2f", activeLoan!!.interestAmount)}")
                                LoanDetailRow("Total Due", "₹ ${String.format(Locale.getDefault(), "%,.2f", activeLoan!!.totalRepayment)}", isBold = true)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { showInterestDeductDialog = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IconSavings),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Deduct Int.", fontSize = 13.sp)
                            }
                            Button(
                                onClick = { showRepayDialog = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Full Repay", fontSize = 13.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = { navController.navigate("loan/${m.id}") },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Activate New Loan", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Repayment History Section
                item {
                    Text(text = "Repayment History (Date & Time)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CardTextTitle)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (repayments.isEmpty()) {
                        Text("No records found.", fontSize = 14.sp, color = CardTextSummary)
                    }
                }

                items(repayments) { repayment ->
                    RepaymentItem(repayment)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Delete Account Section at the bottom
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Member Account", fontSize = 13.sp)
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PurplePrimary)
        }
    }
}

@Composable
fun RepaymentItem(repayment: LoanRepayment) {
    val date = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date(repayment.timestamp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = date, fontSize = 12.sp, color = CardTextSummary)
                Text(text = repayment.note, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Text(text = "₹ ${String.format(Locale.getDefault(), "%,.2f", repayment.amount)}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        }
    }
}

@Composable
fun DetailStatCard(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 12.sp, color = CardTextSummary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
fun LoanDetailRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = CardTextSummary)
        Text(
            text = value, 
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium, 
            color = CardTextTitle
        )
    }
}
