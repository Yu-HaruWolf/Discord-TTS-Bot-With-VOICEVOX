package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;

import javax.annotation.Nonnull;

import com.github.yu_haruwolf.discord_tts_bot_with_voicevox.audio.AudioController;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EventListeners extends ListenerAdapter{
    final AudioController audioController;
    final Logger logger;
    public EventListeners() {
        this.logger = LoggerFactory.getLogger(EventListeners.class);
        audioController = new AudioController();
    }
    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot() && !event.isWebhookMessage()) {
            switch (event.getMessage().getContentDisplay().toLowerCase()) {
                case "hello!":
                    event.getChannel().sendMessage("Hello!").queue();
                    break;
                case "connect":
                    audioController
                            .connectToVoiceChannel(event.getMember().getVoiceState().getChannel().asVoiceChannel());
                    break;
                case "disconnect":
                    audioController.disconnectFromVoiceChannel(event.getGuild());
                    break;
                case "play":
                    try {
                        audioController.textToSpeech(event.getGuild(), "Play");
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case "shutdown":
                    System.exit(0);
                    break;
                default:
                    if (event.getGuild().getAudioManager().isConnected()) {
                        try {
                            audioController.textToSpeech(event.getGuild(), event.getMessage().getContentDisplay());
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getInteraction().getName()) {
            case "connect":
                audioController.connectToVoiceChannel(event.getMember().getVoiceState().getChannel().asVoiceChannel());
                break;
            case "disconnect":
                audioController.disconnectFromVoiceChannel(event.getGuild());
                break;
            case "volume":
                int level = event.getInteraction().getOption("level").getAsInt();
                if(level >= 0 && level <= 100) {
                    audioController.setVolume(event.getGuild(), level);
                } else {
                    event.reply("Level must be the range between 0 and 100").setEphemeral(true).queue();
                }
                break;
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        event.getGuild().updateCommands().addCommands(
                Commands.slash("connect", "Connect to voice channel."),
                Commands.slash("disconnect", "Disconnect from voice channel."),
                Commands.slash("volume", "Set the voice volume.").addOption(OptionType.INTEGER, "level", "You can set 0-100.", true)
        ).queue();
    }
}
