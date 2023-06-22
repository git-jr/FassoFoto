package com.paradoxo.fassofoto

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paradoxo.fassofoto.camerax.CameraXActivity
import com.paradoxo.fassofoto.extensions.showToast
import com.paradoxo.fassofoto.ui.theme.FassoFotoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, CameraXActivity::class.java))

        setContent {
            FassoFotoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    ButtonSelectorScreen(
                        openNormalActivity = {
                            startActivity(Intent(this, CameraXActivity::class.java))
                        },
                        openComposeCameraActiviy = {
                            context.showToast("CameraX com Jetpack Compose")
                        },

                        )
                }
            }
        }
    }
}

@Composable
fun ButtonSelectorScreen(
    modifier: Modifier = Modifier,
    openComposeCameraActiviy: () -> Unit = {},
    openNormalActivity: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            openNormalActivity()
        }) {
            Text(text = "Normal Camera")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            openComposeCameraActiviy()
        }) {
            Text(text = "Compose Camera")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FassoFotoTheme {
        ButtonSelectorScreen()
    }
}


