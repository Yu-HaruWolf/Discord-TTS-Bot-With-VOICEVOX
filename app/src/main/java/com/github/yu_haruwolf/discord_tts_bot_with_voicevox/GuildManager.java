package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;

import java.util.HashSet;
import java.util.Set;

import net.dv8tion.jda.api.entities.channel.Channel;

public class GuildManager {
    private Set<Channel> readAloudTargetChannel;

    public GuildManager() {
        readAloudTargetChannel = new HashSet<>();
    }

    public void addReadAloudTargetChannel(Channel channel) {
        readAloudTargetChannel.add(channel);
    }

    public void clearReadAloudTargetChannel() {
        readAloudTargetChannel.clear();
    }

    public boolean containsChannel(Channel channel) {
        return readAloudTargetChannel.contains(channel);
    }
}
