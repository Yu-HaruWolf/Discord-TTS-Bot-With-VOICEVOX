package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;


import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;


public class AudioController {
    final HashMap<Guild, GuildAudioManager> guildAudioManagers;
    final AudioPlayerManager audioPlayerManager;
    public AudioController() {
        guildAudioManagers = new HashMap<>();
        audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
    }

    private GuildAudioManager getGuildAudioManager(Guild guild) {
        GuildAudioManager guildAudioManager;
        if(guildAudioManagers.containsKey(guild)) {
            guildAudioManager = guildAudioManagers.get(guild);
        } else {
            guildAudioManagers.put(guild, new GuildAudioManager(audioPlayerManager));
            guildAudioManager = guildAudioManagers.get(guild);
        }
        return guildAudioManager;
    }

    public void connectToVoiceChannel(VoiceChannel voiceChannel) {
        Guild guild = voiceChannel.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        try {
            audioManager.openAudioConnection(voiceChannel);
        } catch (InsufficientPermissionException e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromVoiceChannel(Guild guild) {
        AudioManager audioManager = guild.getAudioManager();
        audioManager.closeAudioConnection();
    }
}
