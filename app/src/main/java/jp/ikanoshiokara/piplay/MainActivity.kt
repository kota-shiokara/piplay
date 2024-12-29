package jp.ikanoshiokara.piplay

import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.graphics.toRect
import androidx.core.util.Consumer
import jp.ikanoshiokara.piplay.ui.theme.PiplayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PiplayTheme {
                val context = LocalContext.current
                val isInPipMode = rememberIsInPipMode()
                val pipParams = PictureInPictureParams.Builder().apply {
                    setAspectRatio(Rational(9,16))
                }

                val pipModifier = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    DisposableEffect(context) {
                        val onUserLeaveBehavior: () -> Unit = {
                            context.findActivity()
                                .enterPictureInPictureMode(pipParams.build())
                        }
                        context.findActivity().addOnUserLeaveHintListener(
                            onUserLeaveBehavior
                        )
                        onDispose {
                            context.findActivity().removeOnUserLeaveHintListener(
                                onUserLeaveBehavior
                            )
                        }
                    }
                    Modifier
                } else {
                    Modifier.onGloballyPositioned { layoutCoordinates ->
                        val builder = pipParams
                        val sourceRect = layoutCoordinates.boundsInWindow().toAndroidRectF().toRect()
                        builder.setSourceRectHint(sourceRect)
                        builder.setAutoEnterEnabled(true)
                        context.findActivity().setPictureInPictureParams(builder.build())
                    }
                }

                Scaffold(modifier = pipModifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Greeting(
                            name = if (isInPipMode) "PiP Mode in Android" else "Android"
                        )


                        Button(
                            onClick = {
                                context.findActivity().enterPictureInPictureMode(
                                    pipParams.build()
                                )
                            }
                        ) {
                            Text(text = if (isInPipMode) "Exit PiP mode!" else "Enter PiP mode!")
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

internal fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Picture in picture should be called in the context of an Activity")
}

@Composable
fun rememberIsInPipMode(): Boolean {
    val activity = LocalContext.current.findActivity()
    var pipMode by remember { mutableStateOf(activity.isInPictureInPictureMode) }
    DisposableEffect(activity) {
        val observer = Consumer<PictureInPictureModeChangedInfo> { info ->
            pipMode = info.isInPictureInPictureMode
        }
        activity.addOnPictureInPictureModeChangedListener(
            observer
        )
        onDispose { activity.removeOnPictureInPictureModeChangedListener(observer) }
    }
    return pipMode
}