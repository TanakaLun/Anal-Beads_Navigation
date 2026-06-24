package com.example.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.example.ui.navigation.AnchorSide
import com.example.example.ui.navigation.BeadNavBar
import com.example.example.ui.navigation.BeadNavItem
import com.example.example.ui.theme.ComposeEmptyActivityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeEmptyActivityTheme {
                NavBarTestScreen()
            }
        }
    }
}

private val navItems = listOf(
    BeadNavItem(Icons.Default.Home, "Home"),
    BeadNavItem(Icons.Default.Search, "Search"),
    BeadNavItem(Icons.Default.Favorite, "Favorites"),
    BeadNavItem(Icons.Default.Person, "Profile"),
    BeadNavItem(Icons.Default.Settings, "Settings"),
)

@Composable
fun NavBarTestScreen() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var anchorSide by remember { mutableStateOf(AnchorSide.LEFT) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = navItems[selectedIndex].label,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Drag the bead chain below to navigate",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { anchorSide = AnchorSide.LEFT },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (anchorSide == AnchorSide.LEFT)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { Text("LEFT") }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { anchorSide = AnchorSide.RIGHT },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (anchorSide == AnchorSide.RIGHT)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { Text("RIGHT") }
                }
            }

            BeadNavBar(
                items = navItems,
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it },
                anchorSide = anchorSide,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}
