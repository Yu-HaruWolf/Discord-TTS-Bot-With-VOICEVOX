package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
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
            e.printStackTrace();
            System.exit(-1);
        }
        Thread shutdownHook = new Thread() {
            public void run() {
                jda.shutdown();
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        logger.info("Logged In! " + jda.getSelfUser().getName());
        for (Guild guild : jda.getGuilds()) {
            guild.updateCommands().addCommands(
                    Commands.slash("connect", "Connect to voice channel."),
                    Commands.slash("disconnect", "Disconnect from voice channel."),
                    Commands.slash("volume", "Set the voice volume.").addOption(OptionType.INTEGER, "level", "You can set 0-100.", true),
                    Commands.slash("set-speaker", "Select the speaker").addOption(OptionType.INTEGER, "speaker", "Speaker", true),
                    Commands.slash("list-speaker", "Show the list of speaker"),
                    Commands.slash("reload-speaker", "Reload the list of speakers")
            ).queue();
        }
    }
}
