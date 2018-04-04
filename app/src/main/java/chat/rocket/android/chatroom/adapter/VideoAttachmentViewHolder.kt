package chat.rocket.android.chatroom.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.net.toUri
import chat.rocket.android.R
import chat.rocket.android.chatroom.di.DaggerChatRoomViewHolderComponent
import chat.rocket.android.chatroom.domain.MediaCache
import chat.rocket.android.chatroom.viewmodel.VideoAttachmentViewModel
import chat.rocket.android.chatroom.viewmodel.media.MediaMetadataViewModel
import chat.rocket.android.player.PlayerActivity
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.widget.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.message_attachment.view.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import javax.inject.Inject

class VideoAttachmentViewHolder(itemView: View,
                                listener: ActionsListener,
                                reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<VideoAttachmentViewModel>(itemView, listener, reactionListener) {
    private var job: Job? = null
    @Inject lateinit var cache: MediaCache

    init {
        DaggerChatRoomViewHolderComponent.builder()
                .context(itemView.context.applicationContext)
                .build()
                .inject(this)
        with(itemView) {
            image_attachment.setVisible(false)
            audio_video_attachment.setVisible(true)
            setupActionMenu(attachment_container)
            setupActionMenu(audio_video_attachment)
        }
    }

    override fun bindViews(data: VideoAttachmentViewModel) {
        val url = data.attachmentUrl
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
                audio_video_attachment.setOnClickListener { _ ->
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

            val cachedMediaData = cache.get(url)
            if (cachedMediaData == null) {
                job = launch(UI) {
                    val mediaMetadata = getMediaMetadata(ctx, url)
                    mediaMetadata?.let {
                        text_duration.setVisible(true)
                        text_duration.text = it.duration
                        if (it.frame is Bitmap) {
                            audio_video_preview.maxHeight = audio_video_preview.measuredWidth
                            audio_video_preview.setImageBitmap(it.frame)
                            image_preview_play.setVisible(true)
                            cache.put(url, mediaMetadata)
                        }
                    }
                }
            } else {
                with(cachedMediaData) {
                    audio_video_preview.setImageBitmap(frame)
                    text_duration.setVisible(true)
                    text_duration.text = duration
                }
            }
        }
    }

    private suspend fun getMediaMetadata(context: Context, url: String)
            : MediaMetadataViewModel? = withContext(CommonPool) {
        val mmr = MediaMetadataRetriever()
        try {
            if (url.startsWith("content") || url.startsWith("file")) {
                mmr.setDataSource(context, url.toUri())
            } else {
                mmr.setDataSource(url, emptyMap())
            }
            val durationMs = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = getFormattedDuration(Duration.of(durationMs.toLong(), ChronoUnit.MILLIS))
            val frame = mmr.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
            val thumb = ThumbnailUtils.extractThumbnail(frame, frame.width, frame.height,
                    MediaStore.Images.Thumbnails.FULL_SCREEN_KIND)
            val width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            return@withContext MediaMetadataViewModel(thumb, duration, height.toInt(), width.toInt())
        } catch (ex: RuntimeException) {
            Timber.e(ex)
        } finally {
            mmr.release()
        }
        return@withContext null
    }

    private fun getFormattedDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutes()
        val seconds = (duration.toMillis() / 1000L) % 60
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
            image_preview_play.setVisible(false)
            audio_video_preview.setImageDrawable(null)
            job?.cancel()
        }
    }
}