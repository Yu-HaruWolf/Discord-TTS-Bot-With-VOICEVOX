package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yu_haruwolf.discord_tts_bot_with_voicevox.audio.AudioController;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
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
        this.sqlSystem = Bot.sqlSystem;
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
            if (event.getGuild().getAudioManager().isConnected()) {
                try {
                    try {
                        int speaker = sqlSystem.getSpeakerId(event.getAuthor().getId());
                        audioController.textToSpeech(event.getGuild(), event.getMessage().getContentDisplay(), speaker);
                    } catch (SQLException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                        audioController.textToSpeech(event.getGuild(), event.getMessage().getContentDisplay(), 3);
                    }
                } catch (IOException | InterruptedException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        DiscordLocale userLocale = event.getUserLocale();
        switch (event.getInteraction().getName()) {
            case "connect":
                try {
                    VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel().asVoiceChannel();
                    audioController.connectToVoiceChannel(voiceChannel);
                    switch (userLocale) {
                        case JAPANESE ->
                            event.reply("接続しました。").queue();
                        default ->
                            event.reply("Connected.").queue();
                    }
                    audioController.textToSpeech(event.getGuild(), "接続しました。", 3);
                } catch (NullPointerException e) {
                    switch (userLocale) {
                        case JAPANESE ->
                                event.reply("先にボイスチャンネルに接続してください。").queue();
                        default ->
                                event.reply("Please connect to the voice channel first.").queue();
                    }
                } catch (IllegalArgumentException e) {
                    switch(userLocale) {
                        case JAPANESE ->
                            event.reply("不明なエラーが発生しました。管理者にサーバーログを確認するようにお願いしてください。").queue();
                        default ->
                            event.reply("An unknown error has occurred. Please ask the server administrator to check the server log.").queue();
                    }
                } catch (UnsupportedOperationException e) {
                    switch(userLocale) {
                        case JAPANESE ->
                            event.reply("内部エラーが発生しました。").queue();
                        default ->
                            event.reply("An internal error has occurred.").queue();
                    }
                } catch (InsufficientPermissionException e) {
                    switch(userLocale) {
                        case JAPANESE ->
                            event.reply("権限が不足しています。BOTに権限を追加してください。").queue();
                        default ->
                            event.reply("Insufficient permission. Please add the permission to me.").queue();
                    }
                } catch (IOException | InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                break;
            case "disconnect":
                audioController.disconnectFromVoiceChannel(event.getGuild());
                switch (userLocale) {
                    case JAPANESE ->
                        event.reply("切断しました。").queue();
                    default ->
                        event.reply("Disconnected.").queue();
                }
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
                if (!speakersMap.containsKey(speakerId)) {
                    switch (userLocale) {
                        case JAPANESE ->
                            event.reply("そのIDの話者は存在しません！").setEphemeral(true).queue();
                        default ->
                                event.reply("The speaker id is not exist!").setEphemeral(true).queue();
                    }
                }
                try {
                    sqlSystem.updateSpeakerId(event.getUser().getId(), speakerId);
                    String speakerName = speakersMap.get(speakerId);
                    switch (userLocale) {
                        case JAPANESE ->
                            event.reply("話者を" + speakerName + "に変更しました。").queue();
                        default ->
                                event.reply("Update the speaker to " + speakerName).queue();
                    }
                } catch (SQLException e) {
                    logger.error("Failed to update the speaker", e);
                    switch (userLocale) {
                        case JAPANESE ->
                            event.reply("話者を変更できませんでした。").setEphemeral(true).queue();
                        default ->
                                event.reply("Failed to update the speaker").setEphemeral(true).queue();
                    }
                }
                break;
            case "reload-speaker":
                try {
                    speakersMap = getSpeakers();
                    logger.info("Reloaded the list of speakers");
                    event.reply("Success!").setEphemeral(true).queue();
                } catch (IOException | InterruptedException e) {
                    event.reply("Failed to get the list of speakers").setEphemeral(true).queue();
                    logger.error("Failed to get the list of speakers", e);
                }
                break;
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Bot.initializeGuildCommands(event.getGuild());
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
