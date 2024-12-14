package com.stellaridea.swiftvision.ui.views.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarSample(
    onSearch: (String) -> Unit,
    onLogout: () -> Unit,
    userName: String,
    userEmail: String
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (isSearching) {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearch(it)
                    },
                    placeholder = { Text("Buscar proyectos...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(text = "Proyectos")
            }
        },
        actions = {
            if (isSearching) {
                IconButton(onClick = {
                    isSearching = false
                    searchQuery = ""
                    onSearch("") // Resetea la búsqueda
                }) {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = "Cerrar búsqueda")
                }
            } else {
                IconButton(onClick = { isSearching = true }) {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = "Buscar")
                }
                IconButton(onClick = { showProfileMenu = true }) {
                    Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = "Perfil")
                }
            }

            DropdownMenu(
                expanded = showProfileMenu,
                onDismissRequest = { showProfileMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(text = "Hola, $userName")
                            Text(text = userEmail, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    onClick = {}
                )
                Divider()
                DropdownMenuItem(
                    text = { Text("Cerrar sesión") },
                    onClick = {
                        showProfileMenu = false
                        onLogout()
                    }
                )
            }
        }
    )
}

