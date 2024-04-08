package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bot {
    JDA jda;
    Logger logger;
    public static final SQLSystem sqlSystem = new SQLSystem();

    public static void main(String[] args) {
        new Bot();
    }

    Bot() {
        logger = LoggerFactory.getLogger(Bot.class);
        String token = System.getenv("TOKEN_DISCORD_TTS_BOT_WITH_VOICEVOX");
        try {
            jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT).addEventListeners(new EventListeners()).build().awaitReady();
        } catch (InterruptedException e) {
            logger.error("error", e);
            System.exit(-1);
        }
        Thread shutdownHook = new Thread(() -> jda.shutdown());
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        logger.info("Logged In! " + jda.getSelfUser().getName());
        for (Guild guild : jda.getGuilds()) {
            initializeGuildCommands(guild);
        }
    }

    public static void initializeGuildCommands(Guild guild) {
        guild.updateCommands().addCommands(
                Commands.slash("connect", "Connect to voice channel.")
                        .setDescriptionLocalization(DiscordLocale.JAPANESE, "ボイスチャンネルに接続します。"),
                Commands.slash("disconnect", "Disconnect from voice channel.")
                        .setDescriptionLocalization(DiscordLocale.JAPANESE, "ボイスチャンネルから切断します。"),
                Commands.slash("volume", "Set the voice volume.").addOption(OptionType.INTEGER, "level", "You can set 0-100.", true)
                        .setDescriptionLocalization(DiscordLocale.JAPANESE, "声の音量を設定します。"),
                Commands.slash("set-speaker", "Select the speaker").addOption(OptionType.INTEGER, "speaker", "Speaker", true)
                        .setDescriptionLocalization(DiscordLocale.JAPANESE, "話者を選択します。"),
                Commands.slash("list-speaker", "Show the list of speaker")
                        .setDescriptionLocalization(DiscordLocale.JAPANESE, "話者の一覧を表示します。"),
                Commands.slash("reload-speaker", "Reload the list of speakers")
                        .setDescriptionLocalization(DiscordLocale.JAPANESE, "話者を再読み込みします。")
        ).queue();
    }
}
