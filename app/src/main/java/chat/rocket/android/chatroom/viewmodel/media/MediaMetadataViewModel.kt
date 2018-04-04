package chat.rocket.android.chatroom.viewmodel.media

import android.graphics.Bitmap

data class MediaMetadataViewModel(
        val frame: Bitmap?,
        val duration: String,
        val height: Int,
        val width: Int
)