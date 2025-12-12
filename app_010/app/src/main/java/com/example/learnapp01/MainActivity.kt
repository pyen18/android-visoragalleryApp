package com.example.learnapp01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.learnapp01.ui.theme.LearnApp01Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnApp01Theme {
//                Scaffold(
//                    modifier = Modifier.fillMaxSize()
//                ) { innerPadding ->
//                    HomeScreen(modifier = Modifier.padding(innerPadding))
//                }
//                SplashScreen()
                T1Navigation()
            }
        }
    }


  @Composable
  fun test(){
      Box(){
          Text("hello world", style = TextStyle(
              fontSize = 30.sp,
              fontWeight = FontWeight.Bold)
          )
      }

  }
    @Composable
    fun T1Navigation() {
        val navController = rememberNavController()
        NavHost(
            navController = navController, startDestination = "splash"
        ) {
            composable("splash") {
                SplashScreen(navController)
            }
            composable("players") {
                PlayersListScreen(navController)
            }
            composable("player_detail/{playerName}") { backStackEntry ->
                val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
                PlayerDetailScreen(playerName, navController)
            }
        }
    }


//    @Composable
//    fun HomeScreen(modifier: Modifier = Modifier) {
//        Column (modifier = modifier
//                .fillMaxSize()
//            .padding(horizontal = 16.dp))  {
//            Column  {
////            Greeting("Phuc Yen")
//            BannerCompose()
//            Spacer(modifier = Modifier.height(12.dp))
//            VectorResourceComponent()
//        }
//    }
//    val customH1: TextStyle
//        get() = TextStyle(
//            fontSize = 30.sp,
//            fontStyle = FontStyle.Italic,
//            fontWeight = FontWeight.Bold,
//            // Bỏ color và textAlign ở đây
//        )
@Composable
    fun PlayerItem(player: Player, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onClick() }) {
            Image(
                painter = painterResource(id = player.imageRes),
                contentDescription = player.name,
                modifier = Modifier
                    .size(80.dp)
                    .padding(vertical = 16.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(
                    text = player.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = player.realName, fontSize = 16.sp, color = Color.DarkGray
                )
            }
        }
    }
 @Composable
    fun PlayerDetailScreen(playerName: String, navController: NavHostController) {
        val player = PlayerData.players.find { it.name == playerName }
        player?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = it.name,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = it.imageRes),
                    contentDescription = it.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Real Name: ${it.realName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Birth Date: ${it.birthDate}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it.description, fontSize = 16.sp, color = Color.Black
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red, contentColor = Color.White
                    ),
                ) {
                    Text(text = "Back", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Data models
    data class Player(
        val name: String,
        val imageRes: Int,
        val realName: String,
        val birthDate: String,
        val description: String
    )
    
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(players) { player ->
                    PlayerItem(player = player) {
                        navController.navigate("player_detail/${player.name}")
                    }
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                }
            }

        }

    // Sample data
    object PlayerData {
        val players = listOf(
            Player(
                "Faker",
                R.drawable.faker,
                "Lee Sang-hyeok",
                "May 7, 1996",
                "Lee Sang-hyeok (Korean: 이상혁; born May 7, 1996), better known as Faker, is a South Korean professional League of Legends player for T1. He gained prominence after joining SK Telecom T1 (now T1) in 2013, where he has since played as the team's mid-laner. Throughout his career, he has secured a record of 10 League of Legends Champions Korea (LCK) titles, two Mid-Season Invitational (MSI) titles, and a record five World Championship titles. Faker is widely regarded as the greatest League of Legends player in history and has drawn comparison analogizing him to basketball player Michael Jordan for his esports success."
            ), Player(
                "Gumayusi",
                R.drawable.gumayasi,
                "Lee Min-hyeong",
                "February 6, 2002",
                "Lee Min-hyeong, known as Gumayusi, is T1's AD Carry. He is known for his exceptional mechanics and aggressive playstyle."
            ), Player(
                "Keria",
                R.drawable.keria,
                "Ryu Min-seok",
                "October 14, 2002",
                "Ryu Min-seok, known as Keria, is T1's Support player. He is recognized for his innovative strategies and champion pool."
            ), Player(
                "Oner",
                R.drawable.oner,
                "Moon Hyeon-joon",
                "December 24, 2002",
                "Moon Hyeon-joon, known as Oner, is T1's Jungler. He is praised for his aggressive jungle style and synergy with Faker."
            ), Player(
                "Zeus",
                R.drawable.zeus,
                "Choi Woo-je",
                "January 31, 2004",
                "Choi Woo-je, known as Zeus, is T1's Top laner. Despite being the youngest, he has proven to be a dominant force in the top lane."
            )
        )
    }


//    @Composable
//    fun Greeting(name: String, modifier: Modifier = Modifier) {
//        Text(
//            text = stringResource(id = R.string.test_text),
////            style = customH1,
//            modifier = modifier,
//            color = Color.Red,
//            fontSize = 30.sp,
//            fontStyle = FontStyle.Italic,
//            fontWeight = FontWeight.Bold,
//            textAlign = TextAlign.Justify,
//        )
//    }
//    @Composable
//    fun BannerCompose(){
//        Image(painterResource(id = R.drawable.banner), contentDescription = null, Modifier.fillMaxWidth())
//    }
//
//    @Composable
//    fun VectorResourceComponent(){
//        Image(imageVector = Icons.Filled.Person, contentDescription = "person")
//    }
//    @Composable
//    fun VectorImageComponent(){
//        Image(imageVector =  )
//    }

    // splash screen
    @Composable
    fun SplashScreen(navController: NavHostController) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {

//                CircleAvatar()
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "T1 logo",
                    modifier = Modifier.size(300.dp),
//                    contentScale = ContentScale.Fit
                )
                // avatar

                Spacer(modifier = Modifier.height(300.dp))

                // Button
                Button(
                    onClick = { navController.navigate("players") },
                    shape = RoundedCornerShape(
                        topEnd = 10.dp, bottomStart = 10.dp, topStart = 10.dp, bottomEnd = 10.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red, contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    ) {
                    Text(text = "T1 Win", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    fun CircleAvatar(){
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(100.dp)
                .shadow(4.dp, shape = RoundedCornerShape(50.dp))
                .background(Color.Gray, shape = RoundedCornerShape(50.dp))
                .aspectRatio(1f),
            contentScale = ContentScale.Crop
        )
    }

    //  Players List Screen
    @Composable
    fun PlayersListScreen(navController: NavHostController) {
        val players = PlayerData.players
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .background(Color.White)
                    .padding(top = 8.dp, start = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "T1 logo",
                    modifier = Modifier.size(100.dp)
//                    contentScale = ContentScale.Fit
                )
            }
//            Spacer(modifier = Modifier.height(6.dp))
            Image(
                painter = painterResource(id = R.drawable.banner),
                contentDescription = "T1 banner",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(players) { player ->
                    PlayerItem(player = player) {
                        navController.navigate("player_detail/${player.name}")
                    }
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                }
            }

        }
    }

    @Composable
    fun PlayerItem(player: Player, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onClick() }) {
            Image(
                painter = painterResource(id = player.imageRes),
                contentDescription = player.name,
                modifier = Modifier
                    .size(80.dp)
                    .padding(vertical = 16.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(
                    text = player.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = player.realName, fontSize = 16.sp, color = Color.DarkGray
                )
            }
        }
    }

    @Composable
    fun PlayerDetailScreen(playerName: String, navController: NavHostController) {
        val player = PlayerData.players.find { it.name == playerName }
        player?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = it.name,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = it.imageRes),
                    contentDescription = it.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Real Name: ${it.realName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Birth Date: ${it.birthDate}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it.description, fontSize = 16.sp, color = Color.Black
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red, contentColor = Color.White
                    ),
                ) {
                    Text(text = "Back", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
//        val navController = rememberNavController()
        LearnApp01Theme {
//            HomeScreen()
            T1Navigation()
        }
    }
}
