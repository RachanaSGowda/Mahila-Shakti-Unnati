package com.example.mahila_shaktiunnati.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mahila_shaktiunnati.data.Member
import com.example.mahila_shaktiunnati.data.ShgDatabase
import com.example.mahila_shaktiunnati.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersListScreen(navController: NavController, startTab: Int = 0) {
    val context = LocalContext.current
    val db = ShgDatabase.getDatabase(context)
    val shgDao = db.shgDao()
    val scope = rememberCoroutineScope()

    val members by shgDao.getAllMembers().collectAsState(initial = emptyList())
    var memberToDelete by remember { mutableStateOf<Member?>(null) }
    var selectedTab by remember { mutableIntStateOf(startTab) }
    var searchQuery by remember { mutableStateOf("") }

    if (memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("Delete Member") },
            text = { Text("Are you sure you want to delete ${memberToDelete?.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            memberToDelete?.let { shgDao.deleteMember(it) }
                            memberToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(PurplePrimary)) {
                TopAppBar(
                    title = { Text("Members Management", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("add_member") }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PurplePrimary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search members...", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedBorderColor = Color.White,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = PurplePrimary,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color.White
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Directory", color = Color.White) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Loan Activation", color = Color.White) }
                    )
                }
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            val filteredMembers = members.filter { 
                it.name.contains(searchQuery, ignoreCase = true) && 
                (selectedTab == 0 || it.activeLoan == 0.0)
            }
            
            Text(
                text = if (selectedTab == 0) "Found: ${filteredMembers.size} members" else "Eligible: ${filteredMembers.size}",
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = CardTextSummary
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredMembers) { member ->
                    MemberListItem(
                        member = member,
                        showLoanAction = selectedTab == 1,
                        onClick = { 
                            if (selectedTab == 0) {
                                navController.navigate("member_details/${member.id}")
                            } else {
                                navController.navigate("loan/${member.id}")
                            }
                        },
                        onDelete = { memberToDelete = member }
                    )
                }
            }
        }
    }
}

@Composable
fun MemberListItem(member: Member, showLoanAction: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val initials = member.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase()
            Box(
                modifier = Modifier.size(50.dp).background(PurplePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            ) {
                Text(text = member.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CardTextTitle)
                Text(text = member.role, fontSize = 12.sp, color = CardTextSummary)
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "S: ₹${String.format(Locale.getDefault(), "%.0f", member.totalSavings)}",
                        fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium
                    )
                    if (member.activeLoan > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "L: ₹${String.format(Locale.getDefault(), "%.0f", member.activeLoan)}",
                            fontSize = 12.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (showLoanAction) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = PurplePrimary)
            } else {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFBDBDBD))
            }
        }
    }
}
