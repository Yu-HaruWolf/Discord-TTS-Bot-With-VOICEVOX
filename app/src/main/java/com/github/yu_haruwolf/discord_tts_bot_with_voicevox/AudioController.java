package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;


import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;


public class AudioController {
    final HashMap<Guild, GuildAudioManager> guildAudioManagers;
    final AudioPlayerManager audioPlayerManager;
    final Logger logger;
    public AudioController() {
        this.logger = LoggerFactory.getLogger(AudioController.class);
        guildAudioManagers = new HashMap<>();
        audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
    }

    private GuildAudioManager getGuildAudioManager(Guild guild) {
        GuildAudioManager guildAudioManager;
        if (!guildAudioManagers.containsKey(guild)) {
            guildAudioManagers.put(guild, new GuildAudioManager(audioPlayerManager));
        }
        guildAudioManager = guildAudioManagers.get(guild);
        return guildAudioManager;
    }

    private GuildAudioManager getGuildAudioPlayer(Guild guild) {
        GuildAudioManager guildAudioManager = getGuildAudioManager(guild);
        guild.getAudioManager().setSendingHandler(guildAudioManager.getSendHandler());
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

    public void playSound(Guild guild) throws IOException, InterruptedException {
        GuildAudioManager guildAudioManager = getGuildAudioPlayer(guild);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://127.0.0.1:50021/audio_query?text=Hello&speaker=3"))
                .version(HttpClient.Version.HTTP_1_1)
                .method("POST", HttpRequest.BodyPublishers.ofString(""))
                .header("accept", "application/json")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        request = HttpRequest
                .newBuilder(URI.create("http://127.0.0.1:50021/synthesis?speaker=3&enable_interrogative_upspeak=true"))
                .version(HttpClient.Version.HTTP_1_1)
                .method("POST", HttpRequest.BodyPublishers.ofString(response.body()))
                .header("accept", "audio/wav")
                .build();
        HttpResponse<Path> responseAudio = client.send(request, HttpResponse.BodyHandlers.ofFile(Path.of("audio.wav")));

        audioPlayerManager.loadItemOrdered(guildAudioManager, responseAudio.body().toString(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                logger.debug("Track Loaded!");
                guildAudioManager.getScheduler().queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                logger.debug("Playlist Loaded!");
            }

            @Override
            public void noMatches() {
                logger.warn("No file loaded.");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                logger.error("Load Failed");
                logger.error(Arrays.toString(exception.getStackTrace()));
            }
        });
    }
}
