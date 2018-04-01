package chat.rocket.android.chatroom.adapter

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.View
import androidx.net.toUri
import chat.rocket.android.chatroom.viewmodel.VideoAttachmentViewModel
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.widget.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.message_attachment.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class VideoAttachmentViewHolder(itemView: View,
                                listener: ActionsListener,
                                reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<VideoAttachmentViewModel>(itemView, listener, reactionListener) {
    private var job: Job? = null

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
            val ctx = context
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

            if (job?.isActive != true || audio_video_preview.drawable == null) {
                job = launch(UI) {
                    val url = data.attachmentUrl
                    val previewBitmap = async {
                        val mediaMetadataRetriever = MediaMetadataRetriever()
                        try {
                            if (url.startsWith("content") || url.startsWith("file")) {
                                val uri = url.toUri()
                                mediaMetadataRetriever.setDataSource(ctx, uri)
                            } else {
                                mediaMetadataRetriever.setDataSource(url, hashMapOf())
                            }
                            mediaMetadataRetriever.getFrameAtTime(1,
                                    MediaMetadataRetriever.OPTION_CLOSEST)
                        } catch (ex: RuntimeException) {
                            Timber.e(ex)
                        } finally {
                            mediaMetadataRetriever.release()
                        }
                    }.await()
                    if (previewBitmap is Bitmap) {
                        audio_video_preview.setImageBitmap(previewBitmap)
                    }
                }
            }
        }
    }

    fun resetState() {
        with(itemView) {
            audio_video_preview.setImageDrawable(null)
            job?.cancel()
        }
    }
}