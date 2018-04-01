package chat.rocket.android.chatroom.adapter

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.View
import androidx.net.toUri
import chat.rocket.android.R
import chat.rocket.android.chatroom.viewmodel.VideoAttachmentViewModel
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.widget.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.message_attachment.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

class VideoAttachmentViewHolder(itemView: View,
                                listener: ActionsListener,
                                reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<VideoAttachmentViewModel>(itemView, listener, reactionListener) {
    private var job: Job? = null
    private val cache = mutableMapOf<String, Bitmap>()

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
                    text_file_length.text = ctx.getString(R.string.max_file_size, readableSize(fileLength))
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
                    val mediaMetadataRetriever = async {
                        val mediaMetadataRetriever = MediaMetadataRetriever()
                        try {
                            if (url.startsWith("content") || url.startsWith("file")) {
                                mediaMetadataRetriever.setDataSource(ctx, url.toUri())
                            } else {
                                mediaMetadataRetriever.setDataSource(url, emptyMap())
                            }
                            mediaMetadataRetriever
                        } catch (ex: RuntimeException) {
                            Timber.e(ex)
                            mediaMetadataRetriever.release()
                            null
                        }
                    }.await()

                    if (mediaMetadataRetriever != null) {
                        val duration = mediaMetadataRetriever.extractMetadata(
                                MediaMetadataRetriever.METADATA_KEY_DURATION)

                        text_duration.setVisible(true)
                        text_duration.text = getDuration(Duration.of(duration.toLong(), ChronoUnit.MILLIS))

                        val previewBitmap = mediaMetadataRetriever.getFrameAtTime(1,
                                MediaMetadataRetriever.OPTION_CLOSEST)
                        if (previewBitmap is Bitmap) {
                            cache[url] = previewBitmap
                            audio_video_preview.setImageBitmap(previewBitmap)
                        }
                    }
                }
            }
        }
    }

    private fun getDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutes()
        val seconds = duration.toMillis() / 1000L
        var formatted = ""
        if (hours > 0) {
            formatted += "${hours.toString().padStart(2, '0')}:"
        }
        formatted += "${minutes.toString().padStart(2, '0')}:"
        formatted += seconds.toString().padStart(2, '0')
        return formatted
    }

    fun resetState() {
        with(itemView) {
            text_duration.setVisible(false)
            audio_video_preview.setImageDrawable(null)
            job?.cancel()
        }
    }
}