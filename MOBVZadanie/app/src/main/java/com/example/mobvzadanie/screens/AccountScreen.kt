package com.example.mobvzadanie.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobvzadanie.R
import com.example.mobvzadanie.UserAccount
import com.example.mobvzadanie.Utils
import kotlinx.coroutines.launch

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