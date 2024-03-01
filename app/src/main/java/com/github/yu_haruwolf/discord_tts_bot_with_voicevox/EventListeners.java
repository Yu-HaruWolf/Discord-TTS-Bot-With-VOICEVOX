package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
        if(!event.getAuthor().isBot() && !event.isWebhookMessage()) {
            switch (event.getMessage().getContentDisplay().toLowerCase()) {
                case "hello!":
                    event.getChannel().sendMessage("Hello!").queue();
                    break;
                case "connect":
                    audioController.connectToVoiceChannel(event.getMember().getVoiceState().getChannel().asVoiceChannel());
                    break;
                case "disconnect":
                    audioController.disconnectFromVoiceChannel(event.getGuild());
                    break;
                case "play":
                    try {
                        audioController.playSound(event.getGuild());
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case "shutdown":
                    System.exit(0);
                    break;
            }
        }
    }
}
