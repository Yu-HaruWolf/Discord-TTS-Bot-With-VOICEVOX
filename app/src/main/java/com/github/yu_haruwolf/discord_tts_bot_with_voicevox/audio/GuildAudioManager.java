package com.github.yu_haruwolf.discord_tts_bot_with_voicevox.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildAudioManager {
    private final AudioPlayer player;
    private final TrackScheduler scheduler;

    public GuildAudioManager(AudioPlayerManager manager, int volume) {
        player = manager.createPlayer();
        player.setVolume(volume);
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }
    
}
