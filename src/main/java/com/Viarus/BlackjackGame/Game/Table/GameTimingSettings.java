package com.Viarus.BlackjackGame.Game.Table;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "game.settings.delays")
public class GameTimingSettings {
    public int initialWaiting;
    public int betting;
    public int deciding;
    public int croupierDelay;
    public int summaryDelay;
    public int postGameWaiting;
}
