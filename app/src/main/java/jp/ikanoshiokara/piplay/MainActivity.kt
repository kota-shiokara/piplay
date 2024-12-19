package jp.ikanoshiokara.piplay

import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import jp.ikanoshiokara.piplay.ui.theme.PiplayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PiplayTheme {
                val context = LocalContext.current
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ){
                        Greeting(
                            name = "Android"
                        )

                        Button(
                            onClick = {
                                context.findActivity().enterPictureInPictureMode(
                                    PictureInPictureParams.Builder().apply {
                                        setAspectRatio(Rational(16,9))
                                    }.build()
                                )
                            }
                        ) {
                            Text(text = "Enter PiP mode!")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PiplayTheme {
        Greeting("Android")
    }
}

// https://github.com/android/snippets/blob/6ea91d123db3e094b82c3c8b6245e75114dfed60/compose/snippets/src/main/java/com/example/compose/snippets/pictureinpicture/PictureInPictureSnippets.kt#L74-L82
// [START android_compose_pip_find_activity]
internal fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Picture in picture should be called in the context of an Activity")
}
// [END android_compose_pip_find_activity]