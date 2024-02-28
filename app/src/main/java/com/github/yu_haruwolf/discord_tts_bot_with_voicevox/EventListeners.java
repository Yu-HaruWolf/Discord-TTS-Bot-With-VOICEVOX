package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListeners extends ListenerAdapter{
    final AudioController audioController;
    public EventListeners() {
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
                case "shutdown":
                    System.exit(0);
                    break;
            }
        }
    }
}
