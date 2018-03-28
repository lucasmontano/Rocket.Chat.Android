package chat.rocket.android.chatroom.adapter

import android.view.View
import chat.rocket.android.chatroom.viewmodel.AudioAttachmentViewModel
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.widget.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.message_attachment.view.*
import java.text.DecimalFormat

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
            if (data.isPreview) {
                // we have a preview
                val fileLength = data.fileSize
                val progress = data.progress
                val finished = progress >= fileLength
                preview_container.setVisible(!finished)
                text_file_progress.text = readableSize(progress)
                text_file_length.text = "/ ${readableSize(fileLength)}"
                progress_bar.max = fileLength.toInt()
                progress_bar.progress = progress.toInt()
            } else {
                preview_container.setVisible(false)
            }
            file_name.text = data.attachmentTitle
            audio_video_attachment.setOnClickListener { view ->
                data.attachmentUrl.let { url ->
                    PlayerActivity.play(view.context, url)
                }
            }
        }
    }

    private fun readableSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }
}