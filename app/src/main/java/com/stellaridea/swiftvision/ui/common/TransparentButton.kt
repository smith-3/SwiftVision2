package com.stellaridea.swiftvision.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.stellaridea.swiftvision.R

@Composable
fun TransparentButton(
    modifier: Modifier,
    text: String,
    isImageVector: Boolean = true,
    imageVector: ImageVector = Icons.Default.Email,
    imagePainter: Painter = painterResource(id = R.drawable.icon),
    color: Color,
    colorText: Color,
    clicked:Boolean = false,
    activate: Boolean = true,
    action:(() -> Unit)? =null
) {

    Box(modifier = modifier.fillMaxWidth().padding(10.dp)) {
        Button(
            onClick = {
                if (action != null) {
                    action()
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                disabledContainerColor = Color.Gray,
                contentColor = Color.White,
                disabledContentColor = Color.White
            ),
            enabled = activate
        ) {
            Row(
                modifier = modifier,
            ) {
                if (isImageVector) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = "Bottom",
                        tint = colorText
                    )
                } else {
                    Icon(
                        painter = imagePainter,
                        contentDescription = "Icon",
                        tint = Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    modifier = modifier.align(Alignment.CenterVertically),
                    text = text,
                    color = colorText
                )
            }
        }
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            if (clicked){
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(30.dp)
                        .width(30.dp),
                    color = Color.White,
                    strokeWidth = 4.dp
                )
            }
            Spacer(modifier = Modifier.width(15.dp))
        }
    }
    Spacer(modifier = modifier.height(15.dp))
}
