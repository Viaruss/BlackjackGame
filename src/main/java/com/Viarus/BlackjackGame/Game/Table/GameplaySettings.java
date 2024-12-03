package com.Viarus.BlackjackGame.Game.Table;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "game.settings")
public class GameplaySettings {
    private int maxPlayers;
    private int decksCount;
    private int croupierLimit;
    private int blackJackMultiplier;
}
