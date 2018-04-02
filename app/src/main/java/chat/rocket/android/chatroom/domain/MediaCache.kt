package chat.rocket.android.chatroom.domain

import android.util.LruCache

class MediaCache(maxSize: Int) : LruCache<String, MediaMetadata>(maxSize) {
    override fun sizeOf(key: String, value: MediaMetadata): Int {
        return value.frame?.byteCount ?: 0 + value.duration.length
    }
}