package chat.rocket.android.chatroom.adapter

import android.view.View
import chat.rocket.android.chatroom.viewmodel.VideoAttachmentViewModel
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.widget.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.message_attachment.view.*

class VideoAttachmentViewHolder(itemView: View,
                                listener: ActionsListener,
                                reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<VideoAttachmentViewModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            image_attachment.setVisible(false)
            audio_video_attachment.setVisible(true)
            setupActionMenu(attachment_container)
            setupActionMenu(audio_video_attachment)
        }
    }

    override fun bindViews(data: VideoAttachmentViewModel) {
        with(itemView) {
            if (data.isPreview) {
                image_preview_cancel.setVisible(true)
                image_preview_play.setVisible(false)
                val fileLength = data.fileSize
                val progress = data.progress
                val finished = progress >= fileLength
                preview_progress_container.setVisible(!finished)
                text_file_progress.text = readableSize(progress)
                if (text_file_length.text.isEmpty()) {
                    text_file_length.text = "/ ${readableSize(fileLength)}"
                }
                progress_bar.max = fileLength.toInt()
                progress_bar.progress = progress.toInt()
                audio_video_attachment.setOnClickListener { view ->
                    data.attachmentUrl.let { url ->

                    }
                }
            } else {
                preview_progress_container.setVisible(false)
                image_preview_cancel.setVisible(false)
                image_preview_play.setVisible(true)
                audio_video_attachment.setOnClickListener { view ->
                    data.attachmentUrl.let { url ->
                        PlayerActivity.play(view.context, url)
                    }
                }
            }
            file_name.text = data.attachmentTitle
        }
    }
}