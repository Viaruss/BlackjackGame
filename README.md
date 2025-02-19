# Blackjack Game

**Work In Progress**

A web based blackjack game featuring a multiplayer mode that supports up to 3 players per table as well as educational mode with systems that provide the player with real-time game state info and a virtual player implementing the Hi-Lo strategy to play along and compare results.

Main features:
* Client - Server architecture with multi-layered backend and single-page frontend.
* Java 17 + SpringBoot backend.
* Vue, Js, Html, Less frontend.
* MongoDB Data management.

Gameplay:
* Communication based on Rest Api with Http protocol for basic tasks and WebSocket + STOMP for game state synchronization and real time feedback for multiple players.
* Game states managed automatically using Spring Task Scheduling.
* Online mode for up to 3 players.
* Practice mode with UI to provide game state information and strategy elements.
* Virtual player for educational purposes with gameplay logic based on Hi-Lo card counting strategy.

Deployment:
* Uses Docker as a Host environment (compose.yaml contains only testing-purpose credentials).

Game screens:
* Name input
![InputName](https://github.com/user-attachments/assets/0a94da24-b473-444c-b4ab-8ddbc346ad58)

* Online Mode Betting State
![OnlineTableBetPlaced2](https://github.com/user-attachments/assets/94fb60e5-c559-482e-8026-fda6c6a977aa)


* Practice Mode Deciding State:
![PracticeTableStatistics](https://github.com/user-attachments/assets/f6fb72f0-ae92-4535-b773-6a5bd6bcbdc1)

TODO:
* Automated tests
* Proper documentation
* Global refactoring and code cleanup
* Bugfixes

Updates TODO:
* Pairs splitting logic
* SQL DB migration
* Player accounts system
* Custom strategy deviations for practice mode
* Online mode rework - for up to 6 players
* Update README
* Many more to come...
