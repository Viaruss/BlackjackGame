package com.Viarus.BlackjackGame.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "game.settings.delays")
public class GameTimingConfig {
    public int initialWaiting;
    public int betting;
    public int turnDelay;
    public int practiceTurnDelay;
    public int croupierDelay;
    public int botDelay;
    public int summaryDelay;
    public int postGameWaiting;
}
