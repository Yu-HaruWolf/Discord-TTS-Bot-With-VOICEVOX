/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;

import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Bot {
    JDA jda;
    Logger logger;
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
    }
}
