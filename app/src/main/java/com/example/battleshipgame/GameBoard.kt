package com.example.battleshipgame

import androidx.compose.runtime.Composable
import android.media.JetPlayer
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
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
fun gameBoard() {
    Row(modifier = Modifier
        .wrapContentSize(Alignment.Center))
    {
    Text("Lets create a game board")
    }


    val playerBoardSize = 10;
    val playerCellSize = 30.dp;

    val opponentBoardSize = 10;
    val opponentCellSize = 20.dp;

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .offset(y = 100.dp)
    ) {
        for (row in 0 until playerBoardSize) {
            Row {
                for (col in 0 until playerBoardSize) {
                    Box(
                        modifier = Modifier
                            .size(playerCellSize)
                            .border(1.dp, Color.Black)
                            .background(Color.LightGray)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                println("Clicked: Row $row, Col $col")
                            }
                    )
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .offset(y = -200.dp)
    ) {
        for (row in 0 until opponentBoardSize) {
            Row {
                for (col in 0 until opponentBoardSize) {
                    Box(
                        modifier = Modifier
                            .size(opponentCellSize)
                            .border(1.dp, Color.Black)
                            .background(Color.Red)
                    )
                }
            }
        }
    }
}
