package pjatk.prm.pamietnikcyfrowy.data

import android.media.MediaPlayer

class AudioPlayerHelper {

    private var player: MediaPlayer? = null

    fun play(audioPath: String) {
        stop()

        player = MediaPlayer().apply {
            setDataSource(audioPath)
            prepare()
            start()
            setOnCompletionListener {
                stop()
            }
        }
    }

    fun stop() {
        player?.release()
        player = null
    }
}