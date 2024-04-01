package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EventListeners extends ListenerAdapter {
    final AudioController audioController;
    final Logger logger;
    final SQLSystem sqlSystem;
    Map<Integer, String> speakersMap;

    public EventListeners() {
        this.logger = LoggerFactory.getLogger(EventListeners.class);
        audioController = new AudioController();
        this.sqlSystem = new SQLSystem();
        try {
            this.speakersMap = getSpeakers();
        } catch (IOException | InterruptedException e) {
            this.speakersMap = new HashMap<>();
            this.speakersMap.put(-1, "Error");
            logger.error("Failed to get the list of speakers");
        }
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
                if (level >= 0 && level <= 100) {
                    audioController.setVolume(event.getGuild(), level);
                    event.reply("Server volume is set to " + level + ".").queue();
                } else {
                    event.reply("Level must be the range between 0 and 100").setEphemeral(true).queue();
                }
                break;
            case "list-speaker":
                StringBuilder result = new StringBuilder();
                for (int id : speakersMap.keySet()) {
                    result.append(id).append(" - ").append(speakersMap.get(id));
                    result.append("\n");
                }
                event.reply(result.toString()).setEphemeral(true).queue();
                break;
            case "set-speaker":
                int speakerId = event.getOption("speaker").getAsInt();
                if(!speakersMap.containsKey(speakerId)) {
                    event.reply("The speaker id is not exist!").setEphemeral(true).queue();
                }
                try {
                    sqlSystem.updateSpeakerId(event.getUser().getId(), speakerId);
                    event.reply("Update the speaker to " + speakersMap.get(speakerId)).queue();
                } catch (SQLException e) {
                    event.reply("Failed to update the speaker").setEphemeral(true).queue();
                }
                break;
            case "reload-speaker":
                try {
                    speakersMap = getSpeakers();
                    event.reply("Success!").setEphemeral(true).queue();
                } catch (IOException | InterruptedException e) {
                    event.reply("Failed to get the list of speakers").setEphemeral(true).queue();
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

    public Map<Integer, String> getSpeakers() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:50021/speakers"))
                .version(HttpClient.Version.HTTP_1_1)
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response.body());
        Map<Integer, String> speakers = new HashMap<>();
        for (int i = 0; i < node.size(); i++) {
            JsonNode child = node.get(i);
            String characterName = child.get("name").asText();
            JsonNode styles = child.get("styles");
            for (int j = 0; j < styles.size(); j++) {
                JsonNode style = styles.get(j);
                String styleName = style.get("name").asText();
                int id = style.get("id").asInt();
                speakers.put(id, characterName + " - " + styleName);
            }
        }
        return speakers;
    }
}
