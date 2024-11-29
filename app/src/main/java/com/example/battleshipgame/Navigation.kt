package com.example.battleshipgame

import android.media.JetPlayer
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "MainScreen") {
        composable("MainScreen") { MainScreen(navController) } // Main screen route
        composable("LobbyScreen/{username}") { backStackEntry ->
            // Extract username from navigation arguments
            val loggedInUsername = backStackEntry.arguments?.getString("username").orEmpty()
            if (loggedInUsername.isBlank()) {
                // Show error and navigate back if username is invalid
                Toast.makeText(LocalContext.current, "Invalid username", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } else {
                // Navigate to Lobby screen with valid username
                LobbyScreen(navController, loggedInUsername)
            }
        }
            composable("ChallangeScreen?loggedInUsername" +
                    "={loggedInUsername}&opponentName={opponentName}",
                arguments = listOf(
                    navArgument("loggedInUsername") { type = NavType.StringType },
                    navArgument("opponentName") { type = NavType.StringType }
                )
            ){ backStackEntry ->
                val loggedInUsername = backStackEntry.arguments?.getString("loggedInUsername")
                val opponentName = backStackEntry.arguments?.getString("opponentName")
                ChallangeScreen(navController, loggedInUsername, opponentName)
        };
    }
}

@Composable
fun MainScreen(navController: NavController) {
    // State to track user input for username
    var username by remember { mutableStateOf(TextFieldValue("")) }
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var isOnline = false;
    var online = "online";
    var offline = "offline";

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally){
        Text(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .width(500.dp)
                .offset(y = -0.dp)
                .wrapContentSize(Alignment.Center),
            text = "Welcome to Battleship",
            fontSize = 28.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            )

        OutlinedTextField(
            modifier = Modifier
                .padding(bottom = 0.dp)
                .width(350.dp),
            value = username,
            onValueChange = { username = it },
            label = { Text("Enter Username") },
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            singleLine = true
        )
    }
    // Function to check if a player exists or add a new player to Firestore

    fun checkAndAddPlayer(
        username: String,
        onPlayerExists: () -> Unit, // Callback if the player already exists
        onPlayerAdded: () -> Unit, // Callback if a new player is added
        onError: (String) -> Unit  // Callback for handling errors
    ) {
        val playersCollection = firestore.collection("players")

        // Query Firestore to check if the player already exists
        playersCollection
            .whereEqualTo("name", username)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    // Update status to "online" if player exists
                    playersCollection
                        .document(snapshot.documents[0].id)
                        .update("status", "online")
                        .addOnSuccessListener { onPlayerExists() }
                        .addOnFailureListener { onError("Error updating player status") }
                } else {
                    // Add a new player document to Firestore
                    playersCollection
                        .add(
                            mapOf(
                                "name" to username,
                                "status" to "online",
                                "playerId" to UUID.randomUUID().toString()
                            )
                        )
                        .addOnSuccessListener { onPlayerAdded() }
                        .addOnFailureListener { exception ->
                            onError("Error adding player: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                onError("Error checking player: ${exception.message}")
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Button to join the lobby
        Button(
            onClick = {
                if (username.text.isNotBlank()) {
                    // Check or add the player when the button is clicked
                    checkAndAddPlayer(
                        username = username.text.trim(),
                        onPlayerExists = {
                            Toast.makeText(context, "Welcome back, ${username.text.trim()}", Toast.LENGTH_SHORT).show()
                            navController.navigate("LobbyScreen/${username.text.trim()}") // Navigate to Lobby screen
                        },
                        onPlayerAdded = {
                            Toast.makeText(context, "Player added successfully!", Toast.LENGTH_SHORT).show()
                            navController.navigate("LobbyScreen/${username.text.trim()}") // Navigate to Lobby screen
                        },
                        onError = { errorMessage ->
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    // Show error message if username is blank
                    Toast.makeText(context, "Please enter a valid Username", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .padding(bottom = 10.dp)
                .offset(y = 100.dp)
                .width(350.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text("Join Lobby", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LobbyScreen(navController: NavController, loggedInUsername: String) {
    // State to hold the list of online players
    val players = remember { mutableStateListOf<String>() }
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the lobby title
        Text(
            text = "Lobby",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Greet the logged-in user
        Text(
            text = "Welcome, $loggedInUsername!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Display the online players heading
        Text(
            text = "Online Players",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

    // Fetch the list of "online" players when the screen is loaded
    LaunchedEffect(Unit) {
        firestore.collection("players")
            .whereEqualTo("status", "online")
            .get()
            .addOnSuccessListener { snapshot ->
                // Populate the players list with usernames of online players
                snapshot?.documents?.mapNotNull {
                    val name = it.getString("name")
                    if (name != null && name != loggedInUsername) name else null
                }?.let {
                    players.clear()
                    players.addAll(it)
                }
            }
            .addOnFailureListener { error ->
                // Show error message if fetching players fails
                Toast.makeText(context, "Error fetching players: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

        @Composable
        fun PlayerRow(player: String, onChallengeClick: () -> Unit) {
            // A single row to display a player's name and a "Challenge" button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Black, shape = MaterialTheme.shapes.small)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display the player's name
                Text(text = player, fontSize = 16.sp, modifier = Modifier.weight(1f))
                // Button to challenge the player
                Button(
                    onClick = onChallengeClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White

                        )
                ){
                    Text("Challenge")
                }
            }
        }

        // List online players in a scrollable column
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(players) { player ->
                // Display each player as a row with a "Challenge" button
                PlayerRow(player = player) {
                    navController.navigate("ChallangeScreen?loggedInUsername=$loggedInUsername&opponentName=$player")
                }
            }
        }
    }
}

@Composable
fun ChallangeScreen(
    navController: NavController,
    loggedInUsername: String?,
    opponentName: String?) {
    val loggedInUser = loggedInUsername ?: "Player 1" // Provide default value if null
    val opponent = opponentName ?: "Player 2" // Provide default value if null

    var message by remember { mutableStateOf("") }
    var opponentResponse by remember { mutableStateOf <String?> (null) }
    var isChallenged by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Box(
            modifier = Modifier
                .padding(10.dp)
                .width(350.dp)
                .height(250.dp)
                .border(
                    width = 10.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(15.dp)
                )
        ) {
            Text("Challenge Screen: $loggedInUser vs $opponent",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        if(isChallenged) {
            Text("$loggedInUser wants to challenge.")
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxSize()
            ) {
                Button(onClick = {
                    opponentResponse = "accept"
                    isChallenged = false
                }) {
                    Text("Accept")
                }
                Button(onClick = {
                    opponentResponse = "decline"
                    isChallenged = false
                }) {
                    Text("Decline")
                }
            }
        }else{
            Button(onClick = {
                isChallenged = true
            }) {
                Text("Challenge")
            }
        }
    }
    // ... (rest of your GameBoardScreen content) ...
}