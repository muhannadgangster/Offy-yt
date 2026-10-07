package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.VideoItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MyTube", appName)
  }

  @Test
  fun `verify VideoItem custom title priority`() {
    val item = VideoItem(
        id = 1L,
        uri = Uri.parse("content://media/external/video/media/1"),
        path = "/storage/emulated/0/DCIM/video.mp4",
        originalTitle = "VID_20261006.mp4",
        customTitle = "Amazing Travel Vlog",
        durationMs = 125000L
    )

    assertEquals("Amazing Travel Vlog", item.displayTitle)
    assertEquals("2:05", item.formattedDuration)
  }

  @Test
  fun `verify formatted duration for long videos`() {
    val item = VideoItem(
        id = 2L,
        uri = Uri.parse("content://media/external/video/media/2"),
        path = "/movies/film.mkv",
        originalTitle = "Movie",
        durationMs = 3665000L // 1 hour 1 minute 5 seconds
    )

    assertEquals("1:01:05", item.formattedDuration)
  }
}
