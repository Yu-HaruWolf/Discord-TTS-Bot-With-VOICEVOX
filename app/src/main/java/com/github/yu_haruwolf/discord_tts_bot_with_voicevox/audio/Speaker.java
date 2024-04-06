package com.github.yu_haruwolf.discord_tts_bot_with_voicevox.audio;

import java.util.HashMap;
import java.util.Map;

public class Speaker {
    int id;
    String name;
    Map<String, Integer> styles;
    public Speaker(String name, Map<String, Integer> styles) {
        this.name = name;
        this.styles = styles;
        int defaultValue = 0;
        for(int i : styles.values()) {
            defaultValue = i;
            break;
        }
        this.id = styles.getOrDefault("ノーマル", defaultValue);
    }

    public void addStyle(int id, String name) {
        styles.put(name, id);
    }
}
