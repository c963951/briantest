package com.example.bot.spring.echo.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AudioConfig {

    @SerializedName("audioEncoding")
    @Expose
    private String audioEncoding;
    @SerializedName("pitch")
    @Expose
    private String pitch;
    @SerializedName("speakingRate")
    @Expose
    private String speakingRate;

    public AudioConfig(String audioEncoding, String pitch, String speakingRate) {
        this.audioEncoding = audioEncoding;
        this.pitch = pitch;
        this.speakingRate = speakingRate;
    }

    public String getAudioEncoding() {
        return audioEncoding;
    }

    public void setAudioEncoding(String audioEncoding) {
        this.audioEncoding = audioEncoding;
    }

    public String getPitch() {
        return pitch;
    }

    public void setPitch(String pitch) {
        this.pitch = pitch;
    }

    public String getSpeakingRate() {
        return speakingRate;
    }

    public void setSpeakingRate(String speakingRate) {
        this.speakingRate = speakingRate;
    }
}