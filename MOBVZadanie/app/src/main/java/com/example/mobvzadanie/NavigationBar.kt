package com.example.mobvzadanie

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("home", "Domov", Icons.Default.Home, "Domov"),
    ACCOUNT("account", "Konto", Icons.Default.AccountCircle, "Konto")
}

@Composable
fun NavigationBar(modifier: Modifier = Modifier, userAccount: UserAccount, context: Context) {
    val navController = rememberNavController()
    val savedName by userAccount.getName().collectAsState(initial = "")
    val accountCreated = savedName.isNotBlank()
    val startDestination = if (accountCreated) Destination.HOME else Destination.ACCOUNT
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            navController.navigate(route = destination.route)
                            selectedDestination = index
                        },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        },
                        label = { Text(destination.label) },
                        enabled = destination == Destination.ACCOUNT ||
                                (destination == Destination.HOME && accountCreated)
                    )
                }
            }
        }
    ) { contentPadding ->
        AppNavHost(
            navController,
            startDestination,
            modifier = Modifier.padding(contentPadding),
            userAccount,
            context)
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier,
    userAccount: UserAccount,
    context: Context
) {
    NavHost(
        navController,
        startDestination = startDestination.route
    ) {
        Destination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destination.HOME -> HomeScreen()
                    Destination.ACCOUNT -> AccountScreen(modifier.padding(16.dp), userAccount, context)
                }
            }
        }
    }
}

@Composable
fun AccountScreen(modifier: Modifier, userAccount: UserAccount, context: Context, utils: Utils = Utils()) {
    val savedName by userAccount.getName().collectAsState(initial = "")
    val savedAvatar by userAccount.getAvatar().collectAsState(initial = "")
    val avatarBitmap = remember(savedAvatar) {
        if (savedAvatar.isNotBlank()) {
            try {
                android.graphics.BitmapFactory.decodeFile(savedAvatar)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    var nameInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                if (savedAvatar.isNotBlank()) {
                    val oldFile = java.io.File(savedAvatar)
                    if (oldFile.exists()){
                        oldFile.delete()
                    }
                }

                val copyFilePath = utils.saveImage(context, uri, "avatar.jpg")
                if (copyFilePath != null) {
                    userAccount.setAvatar(copyFilePath)
                }
            }
        }
    }
    var nameBeingEdited by remember { mutableStateOf(false) }
    val isEditable = savedName.isEmpty() || nameBeingEdited

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (savedName.isEmpty()) {
            Text(
                text = "Vytvorte si profil",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }

        if (avatarBitmap != null) {
            Image(
                bitmap = avatarBitmap.asImageBitmap(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = "Default Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Button(onClick = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text("Vybrať nový obrázok")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Meno:")
                TextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(if (savedName.isNotBlank()) savedName else "Meno") },
                    placeholder = { Text("Zadajte svoje meno") },
                    enabled = isEditable,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth() // Stretches to full width
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (savedName.isNotEmpty() && !nameBeingEdited) {
                    Button(onClick = {
                        nameInput = savedName
                        nameBeingEdited = true
                    }) {
                        Text("Upraviť")
                    }
                }

                if (isEditable) {
                    Button(
                        onClick = {
                            scope.launch {
                                userAccount.setName(nameInput)
                                nameBeingEdited = false
                            }
                        },
                        enabled = nameInput.isNotBlank()
                    ) {
                        Text("Uložiť")
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Text("Home")
}
