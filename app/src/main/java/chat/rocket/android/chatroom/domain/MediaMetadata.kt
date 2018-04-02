package chat.rocket.android.chatroom.domain

import android.graphics.Bitmap

data class MediaMetadata(
        val frame: Bitmap?,
        val duration: String
)