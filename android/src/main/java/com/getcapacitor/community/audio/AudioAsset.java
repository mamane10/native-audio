package com.getcapacitor.community.audio;

import android.content.res.AssetFileDescriptor;
import com.getcapacitor.JSObject;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class AudioAsset {

  private final String TAG = "AudioAsset";

  private AudioDispatcher audioDispatcher;
  private String assetId;
  private NativeAudio owner;

  AudioAsset(
          NativeAudio owner,
          String assetId,
          AssetFileDescriptor assetFileDescriptor,
          float volume
  )
          throws Exception {
    this.owner = owner;
    this.assetId = assetId;

    AudioDispatcher audioDispatcher = new AudioDispatcher(
            assetFileDescriptor,
            volume
    );
    this.audioDispatcher = audioDispatcher;
    audioDispatcher.setOwner(this);
  }

  public void dispatchComplete() {
    this.owner.dispatchComplete(this.assetId);
  }

  public void play(Double time, boolean loop, Runnable runnable) throws Exception {
    audioDispatcher.play(time, loop, runnable);
  }

  public double getDuration() {
    return audioDispatcher.getDuration();
  }

  public double getCurrentPosition() {
    return audioDispatcher.getCurrentPosition();
  }

  public boolean pause() throws Exception {
    return audioDispatcher.pause();
  }

  public void resume() throws Exception {
    audioDispatcher.resume();
  }

  public void stop() throws Exception {
    audioDispatcher.stop();
  }

  public void unload() throws Exception {
    this.stop();
    audioDispatcher.unload();
    audioDispatcher = null;
  }

  public void setVolume(float volume) throws Exception {
    audioDispatcher.setVolume(volume);
  }

  public boolean isPlaying() throws Exception {
    return audioDispatcher.isPlaying();
  }
}
