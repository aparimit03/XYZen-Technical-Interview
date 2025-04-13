package com.example.xyzen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.xyzen.firebase.FirebaseServiceClass
import com.example.xyzen.screens.AuthenticationScreen
import com.example.xyzen.ui.theme.XYZenFrontendTheme

class AuthenticationActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Check if user is already logged in
		if (FirebaseServiceClass().isUserLoggedIn()) {
			startActivity(Intent(this, MainActivity::class.java)).also {
				finish()
			}
			return
		}

		setContent {
			XYZenFrontendTheme {
				AuthenticationScreen()
			}
		}
	}
}