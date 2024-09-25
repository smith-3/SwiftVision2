package com.stellaridea.swiftvision.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stellaridea.swiftvision.R

@Composable
fun IconHome(){
    val icon: Painter = painterResource(id = R.drawable.logo)
    IconButton(
        onClick = { /*TODO*/ },
        modifier = Modifier.background(Color.Transparent)
    ) {
        Image(
            painter = icon,
            contentDescription = "",
        )
    }
}

@Composable
fun ButtonIconX(
    modifier: Modifier = Modifier.size(50.dp),
    imageVector: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    colorIcon: Color = MaterialTheme.colorScheme.onPrimary,
    isLoading: Boolean = false,
    action: () -> Unit
) {
    Box(modifier = modifier) {
        Button(
            onClick = { action() },
            shape = RoundedCornerShape(10.dp),
            modifier = modifier.align(Alignment.Center),
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                disabledContainerColor = Color.Gray,
                contentColor = colorIcon,
                disabledContentColor = colorIcon
            ),
            contentPadding = PaddingValues(10.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(24.dp), // Ajusta el tamaño del indicador de carga
                    color = colorIcon,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(60.dp),
                    imageVector = imageVector,
                    contentDescription = "Icon",
                    tint = colorIcon
                )
            }
        }
    }
}


@Composable
fun AppName(modifier: Modifier) {
    Box(modifier = modifier) {
        Text(
            text = "HOUSE OF COLOR",
            modifier = Modifier.align(Alignment.Center),
            color = Color.Black,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}
