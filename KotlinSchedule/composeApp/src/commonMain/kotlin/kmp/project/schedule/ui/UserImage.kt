package kmp.project.schedule.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import kotlinschedule.composeapp.generated.resources.Res
import kotlinschedule.composeapp.generated.resources.avatar
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun userImage(
    url: String,
    size: Dp
) {
    val myDrawableResource: DrawableResource = Res.drawable.avatar
    Surface(
        onClick = {},
        modifier = Modifier
            .size(size),
        shape = RoundedCornerShape(size / 2),
    ) {
        Image(
            painter = painterResource(myDrawableResource),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentDescription = "User Image",
            modifier = Modifier
                .size(size),
            contentScale = ContentScale.Crop
        )
    }
}