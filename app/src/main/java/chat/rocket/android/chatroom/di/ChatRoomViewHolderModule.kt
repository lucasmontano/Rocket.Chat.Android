package chat.rocket.android.chatroom.di

import android.app.ActivityManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.WindowManager
import chat.rocket.android.chatroom.domain.MediaCache
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ChatRoomViewHolderModule {

    @Singleton
    @Provides
    fun provideMediaPreviewCache(context: Context): MediaCache {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val availMemBytes = am.memoryClass * 1024 * 1024
        return MediaCache(availMemBytes / 8)
    }
}