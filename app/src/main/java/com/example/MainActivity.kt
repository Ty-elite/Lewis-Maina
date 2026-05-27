package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.components.OfflineBanner
import com.example.ui.screens.*
import com.example.ui.theme.KenyaGreen
import com.example.ui.theme.KenyaRentTheme
import com.example.viewmodel.KenyaRentViewModel

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      var darkThemeState by remember { mutableStateOf(false) }

      KenyaRentTheme(darkTheme = darkThemeState) {
        val viewModel: KenyaRentViewModel = viewModel()
        val currentTab by viewModel.currentTab.collectAsState()
        val selectedPropId by viewModel.selectedPropertyId.collectAsState()
        val currentUser by viewModel.currentUser.collectAsState()
        val isOffline by viewModel.isOffline.collectAsState()

        val activeChatPropId by viewModel.activeChatPropertyId.collectAsState()

        // Responsive Adaptive Check (Mobile Portrait vs Landscape/Tablet Widths)
        val config = LocalConfiguration.current
        val isWideScreen = config.screenWidthDp >= 600

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            Column {
              CenterAlignedTopAppBar(
                title = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Apartment,
                      contentDescription = "App logo",
                      tint = KenyaGreen,
                      modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "KenyaRent",
                      fontWeight = FontWeight.Black,
                      fontSize = 20.sp,
                      color = KenyaGreen
                    )
                  }
                },
                actions = {
                  // Dark Mode toggle Action
                  IconButton(onClick = { darkThemeState = !darkThemeState }) {
                    Icon(
                      imageVector = if (darkThemeState) Icons.Default.LightMode else Icons.Default.DarkMode,
                      contentDescription = "Toggle Dark Mode",
                      tint = KenyaGreen
                    )
                  }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                  containerColor = MaterialTheme.colorScheme.surface
                )
              )

              // Offline simulator alert banner
              OfflineBanner(
                isOffline = isOffline,
                onToggleOffline = { viewModel.toggleOfflineMode() }
              )
            }
          },
          bottomBar = {
            // Display standard Bottom Nav Bar only on mobile widths to avoid awkward stretching
            if (!isWideScreen) {
              NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
              ) {
                // Icons mapping
                NavigationBarItem(
                  selected = currentTab == "Home",
                  onClick = { viewModel.selectTab("Home") },
                  icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                  label = { Text("Home", fontSize = 10.sp) },
                  colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = KenyaGreen)
                )

                NavigationBarItem(
                  selected = currentTab == "Search",
                  onClick = { viewModel.selectTab("Search") },
                  icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                  label = { Text("Browse", fontSize = 10.sp) },
                  colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = KenyaGreen)
                )

                NavigationBarItem(
                  selected = currentTab == "Saved",
                  onClick = { viewModel.selectTab("Saved") },
                  icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved") },
                  label = { Text("Saved", fontSize = 10.sp) },
                  colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = KenyaGreen)
                )

                NavigationBarItem(
                  selected = currentTab == "Messages",
                  onClick = { viewModel.selectTab("Messages") },
                  icon = { Icon(Icons.Default.Mail, contentDescription = "Messages") },
                  label = { Text("Inbox", fontSize = 10.sp) },
                  colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = KenyaGreen)
                )

                NavigationBarItem(
                  selected = currentTab == "Profile",
                  onClick = { viewModel.selectTab("Profile") },
                  icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                  label = { Text("Profile", fontSize = 10.sp) },
                  colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = KenyaGreen)
                )
              }
            }
          }
        ) { innerPadding ->
          // Outer Layout wrapping either Horizontal Sidebar Rails or Port view
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            // Left navigation rail for tablet/desktop displays (Adaptive Canvas guidelines)
            if (isWideScreen) {
              NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxHeight()
              ) {
                NavigationRailItem(
                  selected = currentTab == "Home",
                  onClick = { viewModel.selectTab("Home") },
                  icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                  label = { Text("Home") },
                  colors = NavigationRailItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = KenyaGreen)
                )
                NavigationRailItem(
                  selected = currentTab == "Search",
                  onClick = { viewModel.selectTab("Search") },
                  icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                  label = { Text("Browse") },
                  colors = NavigationRailItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = KenyaGreen)
                )
                NavigationRailItem(
                  selected = currentTab == "Saved",
                  onClick = { viewModel.selectTab("Saved") },
                  icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved") },
                  label = { Text("Saved") },
                  colors = NavigationRailItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = KenyaGreen)
                )
                NavigationRailItem(
                  selected = currentTab == "Messages",
                  onClick = { viewModel.selectTab("Messages") },
                  icon = { Icon(Icons.Default.Mail, contentDescription = "Messages") },
                  label = { Text("Inbox") },
                  colors = NavigationRailItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = KenyaGreen)
                )
                NavigationRailItem(
                  selected = currentTab == "Profile",
                  onClick = { viewModel.selectTab("Profile") },
                  icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                  label = { Text("Profile") },
                  colors = NavigationRailItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = KenyaGreen)
                )
              }
            }

            // Main Display Content Grid Frame
            Box(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
            ) {
              when (currentTab) {
                "Home" -> HomeScreen(viewModel = viewModel)
                "Search" -> SearchScreen(viewModel = viewModel)

                // Saved elements requires login
                "Saved" -> {
                  if (currentUser == null) {
                    LoginStateBlock(
                      viewModel = viewModel,
                      title = "Unlock Your Saved Properties",
                      subtitle = "Store search alerts, bookmark apartment listings, and receive push notifications on price changes instantly."
                    )
                  } else {
                    SeekerDashboardScreen(viewModel = viewModel)
                  }
                }

                // Messages require login
                "Messages" -> {
                  if (currentUser == null) {
                    LoginStateBlock(
                      viewModel = viewModel,
                      title = "Real-time Messaging Inbox",
                      subtitle = "Engage in direct chat conversations with landlords, arrange viewing dates, and negotiate rents securely."
                    )
                  } else {
                    // Decide where they are navigated
                    if (currentUser?.role == UserRole.LANDLORD) {
                      LandlordDashboardScreen(viewModel = viewModel)
                    } else if (currentUser?.role == UserRole.ADMIN) {
                      AdminDashboardScreen(viewModel = viewModel)
                    } else {
                      SeekerDashboardScreen(viewModel = viewModel)
                    }
                  }
                }

                // Profile Tab manages authentication & appropriate panel view mapping
                "Profile" -> {
                  if (currentUser == null) {
                    var isRegisterState by remember { mutableStateOf(false) }
                    if (isRegisterState) {
                      RegisterScreen(
                        viewModel = viewModel,
                        onNavigateToLogin = { isRegisterState = false }
                      )
                    } else {
                      LoginScreen(
                        viewModel = viewModel,
                        onNavigateToRegister = { isRegisterState = true }
                      )
                    }
                  } else {
                    // Logs out profile or directs dashboard controls
                    ProfileManagerScreen(viewModel = viewModel)
                  }
                }

                "Chat" -> {
                  if (activeChatPropId != null) {
                    ChatScreen(viewModel = viewModel)
                  } else {
                    viewModel.selectTab("Messages")
                  }
                }

                else -> HomeScreen(viewModel = viewModel)
              }

              // Property Listing Detail over-sheet Slide Up
              this@Row.AnimatedVisibility(
                visible = selectedPropId != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
              ) {
                if (selectedPropId != null) {
                  ListingDetailScreen(viewModel = viewModel)
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Beautiful fallback panel explaining why seeker needs authentication, with active redirection triggers.
 */
@Composable
fun LoginStateBlock(
    viewModel: KenyaRentViewModel,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      imageVector = Icons.Default.Lock,
      contentDescription = "Contact Authorization",
      tint = KenyaGreen,
      modifier = Modifier.size(54.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
      onClick = { viewModel.selectTab("Profile") },
      colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen)
    ) {
      Text("Log In or Register Account")
    }
  }
}

/**
 * Clean Profile Management view when user is actively logged in.
 */
@Composable
fun ProfileManagerScreen(
    viewModel: KenyaRentViewModel,
    modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()

  Column(
    modifier = modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(24.dp),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(24.dp))
    Box(
      modifier = Modifier
          .size(80.dp)
          .background(KenyaGreen, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = currentUser?.fullName?.take(1)?.uppercase() ?: "K",
        style = MaterialTheme.typography.headlineLarge,
        color = Color.White,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = currentUser?.fullName ?: "Partner Name",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )

    Text(
      text = "Role: " + (currentUser?.role?.name ?: "Seeker Client"),
      fontWeight = FontWeight.SemiBold,
      color = KenyaGreen
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Profile specs card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = CardDefaults.outlinedCardBorder()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        ProfileMetaRow(icon = Icons.Default.Email, label = "Email", value = currentUser?.email ?: "")
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        ProfileMetaRow(icon = Icons.Default.Phone, label = "Mobile Line", value = currentUser?.phone ?: "")
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Direct actions to see dashboard map indicators depending on role
    if (currentUser?.role == UserRole.LANDLORD) {
      Button(
        onClick = { viewModel.selectTab("Messages") },
        colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen),
        modifier = Modifier.fillMaxWidth()
      ) {
         Icon(Icons.Default.Dashboard, contentDescription = "Landlord Dashboard")
         Spacer(modifier = Modifier.width(8.dp))
         Text("Open Landlord Property Dashboard")
      }
      Spacer(modifier = Modifier.height(12.dp))
    } else if (currentUser?.role == UserRole.ADMIN) {
      Button(
        onClick = { viewModel.selectTab("Messages") },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E211F)),
        modifier = Modifier.fillMaxWidth()
      ) {
         Icon(Icons.Default.AdminPanelSettings, contentDescription = "Go to Admin Board")
         Spacer(modifier = Modifier.width(8.dp))
         Text("Go to Admin Board")
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    OutlinedButton(
      onClick = { viewModel.logout() },
      colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
      modifier = Modifier.fillMaxWidth(),
      border = ButtonDefaults.outlinedButtonBorder
    ) {
      Icon(Icons.Default.Logout, contentDescription = "Log out")
      Spacer(modifier = Modifier.width(8.dp))
      Text("Secure Logout")
    }
  }
}

@Composable
fun ProfileMetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(imageVector = icon, contentDescription = label, tint = KenyaGreen, modifier = Modifier.size(20.dp))
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(label, fontSize = 10.sp, color = Color.Gray)
      Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
  }
}
