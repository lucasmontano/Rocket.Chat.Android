package chat.rocket.android.chatroom.adapter

import android.view.View
import chat.rocket.android.R
import chat.rocket.android.chatroom.viewmodel.AudioAttachmentViewModel
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.widget.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.message_attachment.view.*

class AudioAttachmentViewHolder(itemView: View,
                                listener: ActionsListener,
                                reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<AudioAttachmentViewModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            image_attachment.setVisible(false)
            audio_video_attachment.setVisible(true)
            setupActionMenu(attachment_container)
            setupActionMenu(audio_video_attachment)
        }
    }

    override fun bindViews(data: AudioAttachmentViewModel) {
        with(itemView) {
            val ctx = context
            if (data.isPreview) {
                // we have a preview
                val fileLength = data.fileSize
                val progress = data.progress
                val finished = progress >= fileLength
                preview_progress_container.setVisible(!finished)
                text_file_progress.text = readableSize(progress)
                text_file_length.text = ctx.getString(R.string.max_file_size, readableSize(fileLength))
                progress_bar.max = fileLength.toInt()
                progress_bar.progress = progress.toInt()
            } else {
                preview_progress_container.setVisible(false)
            }
            file_name.text = data.attachmentTitle
            audio_video_attachment.setOnClickListener { view ->
                data.attachmentUrl.let { url ->
                    PlayerActivity.play(view.context, url)
                }
            }
        }
    }
}