package com.getcapacitor.community.audio;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AudioDispatcher implements MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

  private final String TAG = "AudioDispatcher";

  private MediaPlayer mediaPlayer;
  private AudioAsset owner;

  CountDownLatch seekLatch;
  private Runnable onSeekCallback;

  public AudioDispatcher(AssetFileDescriptor assetFileDescriptor, float volume)
          throws Exception {

    mediaPlayer = new MediaPlayer();
    mediaPlayer.setOnCompletionListener(this);
    mediaPlayer.setDataSource(
            assetFileDescriptor.getFileDescriptor(),
            assetFileDescriptor.getStartOffset(),
            assetFileDescriptor.getLength()
    );
    mediaPlayer.setOnSeekCompleteListener(this);
    mediaPlayer.setAudioAttributes(
            new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
    );
    mediaPlayer.setVolume(volume, volume);
    mediaPlayer.prepare();
  }

  public void setOwner(AudioAsset asset) {
    owner = asset;
  }

  public double getDuration() {
    return mediaPlayer.getDuration() / 1000.0;
  }

  public double getCurrentPosition() {
    return mediaPlayer.getCurrentPosition() / 1000.0;
  }

  public void play(Double time, boolean loop, Runnable runnable) throws Exception {
    invokePlay(time, loop, runnable);
  }

  public boolean pause() throws Exception {
    if (mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
      return true;
    }

    return false;
  }

  public void resume() throws Exception {
    mediaPlayer.start();
  }

  public void stop() throws Exception {
    if (mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
      mediaPlayer.seekTo(0);
    }
  }

  public void setVolume(float volume) throws Exception {
    mediaPlayer.setVolume(volume, volume);
  }

  public void unload() throws Exception {
    this.stop();
    mediaPlayer.release();
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    if (!mediaPlayer.isLooping()) {
//        this.stop();

//        if (this.owner != null) {
      this.owner.dispatchComplete();
//        }
    }
  }

  /**
   * This function makes the seek synchronous
   * @param time
   * @throws InterruptedException
   */
  private void seek(Double time) throws Exception {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      mediaPlayer.seekTo((int)(time * 1000), MediaPlayer.SEEK_NEXT_SYNC);
    } else {
      mediaPlayer.seekTo((int)(time * 1000));
    }
    if (this.onSeekCallback != null) {
      this.onSeekCallback.run();
    }
  }

  private void invokePlay(Double time, Boolean loop, Runnable runnable) throws Exception {
    boolean playing = mediaPlayer.isPlaying();

    if (playing) {
      mediaPlayer.pause();
    }
    mediaPlayer.setLooping(loop);
    this.onSeekCallback = () -> {
      mediaPlayer.start();
      runnable.run();
    };
    seek(time);
  }

  @Override
  public void onSeekComplete(MediaPlayer mp) {
//    seekLatch.countDown();
  }

  public boolean isPlaying() throws Exception {
    return mediaPlayer.isPlaying();
  }
}
