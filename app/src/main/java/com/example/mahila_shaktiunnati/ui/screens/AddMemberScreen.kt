package com.example.mahila_shaktiunnati.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mahila_shaktiunnati.data.Member
import com.example.mahila_shaktiunnati.data.ShgDatabase
import com.example.mahila_shaktiunnati.ui.theme.PurplePrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Member") }
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("President", "Secretary", "Treasurer", "Member")
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val database = ShgDatabase.getDatabase(context)
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Member", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding() // Automatically slides UI up above the keyboard
                .verticalScroll(scrollState) // Allows swiping up if there's a long list or open keyboard
                .padding(20.dp)
        ) {
            Text("Name", fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Enter member name", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = PurplePrimary
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))

            Text("Phone Number", fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = { Text("Enter phone number", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = PurplePrimary
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("Role", fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = role,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = PurplePrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    roles.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                role = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            database.shgDao().insertMember(
                                Member(name = name, role = role, phone = phone)
                            )
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("Save Member", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            // Large bottom spacer to ensure the Save button can be swiped well above the bottom
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
