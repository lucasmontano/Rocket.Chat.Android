package chat.rocket.android.chatroom.domain

import android.util.LruCache
import chat.rocket.android.chatroom.viewmodel.media.MediaMetadataViewModel

class MediaCache(maxSize: Int) : LruCache<String, MediaMetadataViewModel>(maxSize) {
    override fun sizeOf(key: String, value: MediaMetadataViewModel): Int {
        return value.frame?.byteCount ?: 0 + value.duration.length + 8
    }
}