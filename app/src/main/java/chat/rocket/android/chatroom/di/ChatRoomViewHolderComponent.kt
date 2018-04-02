package chat.rocket.android.chatroom.di

import android.content.Context
import chat.rocket.android.chatroom.adapter.VideoAttachmentViewHolder
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ChatRoomViewHolderModule::class])
interface ChatRoomViewHolderComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(applicationContext: Context): Builder

        fun build(): ChatRoomViewHolderComponent
    }

    fun inject(target: VideoAttachmentViewHolder)
}