package com.example.mahila_shaktiunnati.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mahila_shaktiunnati.data.ShgDatabase
import com.example.mahila_shaktiunnati.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val db = ShgDatabase.getDatabase(context)
    val shgDao = db.shgDao()
    val totalSavings by shgDao.getTotalGroupSavings().collectAsState(initial = 0.0)
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mahila Shakti Unnati", 
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurplePrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = CardTextTitle
            )
            
            Text(
                text = "Empowering Women, Strengthening\nCommunities",
                fontSize = 14.sp,
                color = CardTextSummary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Total Group Savings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Group Savings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CardTextSummary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "₹ ${String.format(Locale.getDefault(), "%,.2f", totalSavings ?: 0.0)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Cards
            DashboardActionCard(
                title = "Member Directory",
                subtitle = "View and manage members",
                icon = Icons.Default.Person,
                iconContainerColor = IconMemberDir.copy(alpha = 0.1f),
                iconColor = IconMemberDir,
                onClick = { navController.navigate("members_list/0") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DashboardActionCard(
                title = "Savings Engine",
                subtitle = "Record and track savings",
                icon = Icons.Default.AccountBalanceWallet,
                iconContainerColor = IconSavings.copy(alpha = 0.1f),
                iconColor = IconSavings,
                onClick = { 
                    navController.navigate("savings/0") 
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DashboardActionCard(
                title = "Loan Tracker",
                subtitle = "Manage loans and repayments",
                icon = Icons.Default.TrendingUp,
                iconContainerColor = IconLoan.copy(alpha = 0.1f),
                iconColor = IconLoan,
                onClick = { navController.navigate("members_list/1") }
            )
            
            // Extra spacer at bottom to ensure last card is fully visible above navigation bar
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DashboardActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconContainerColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CardTextTitle
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = CardTextSummary
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFBDBDBD)
            )
        }
    }
}
