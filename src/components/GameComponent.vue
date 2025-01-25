<template>
  <div>
    <div id="nameInputContainer" class="popUpContainer">
      <div id="nameInputTitleContainer" class="titleContainer">
        <h1 id="nameInputTitle" class="title">Input Player Name</h1>
      </div>
      <div id="nameInputFieldContainer" class="contentContainer">
        <input type="text" id="nameInputField" placeholder="Player Name" v-model="playerName"/>
        <button id="nameInputSubmit" class="button" @click="handleNameSubmit">Submit</button>
      </div>
    </div>

    <div id="nameNotFoundContainer" class="popUpContainer">
      <div id="nameNotFoundTitleContainer" class="titleContainer">
        <h1 id="nameNotFoundTitle" class="title">Player not found!</h1>
      </div>
      <div id="nameNotFoundFieldContainer" class="contentContainer">
        <button id="nameNotFoundReturn" class="button" @click="returnToNameInput">Try Again</button>
        <button id="nameNotFoundCreate" class="button" @click="handleCreatePlayer">Create Player</button>
      </div>
    </div>

    <div id="gameModeMenuContainer" class="popUpContainer">
      <div id="gameModeMenuTitleContainer" class="titleContainer">
        <h1 id="gameModeMenuTitle" class="title">Choose Game Mode</h1>
      </div>
      <div id="gameModeMenuButtonsContainer" class="contentContainer">
        <button id="practiceModeButton" class="button"
                @mouseenter="hoverMode('Learn more about the game rules and strategies, play along with the computer and practice your skills!')"
                @mouseleave="resetModeText('Learn')"
                @click="selectLearnMode"
        >
          Learn
        </button>
        <button id="onlineModeButton" class="button"
                @mouseenter="hoverMode('Play with other players online!')"
                @mouseleave="resetModeText('Online')"
                @click="selectOnlineMode"
        >
          Online
        </button>
      </div>
    </div>

    <div id="joinGameContainer" class="popUpContainer">
      <div id="joinGameTitleContainer" class="titleContainer">
        <h1 id="joinGameTitle" class="title">Tables:</h1>
      </div>
      <div id="joinGameTableListContainer" class="contentContainer">
        <ul id="joinGameTableList">
          <li id="joinGameTableListPlaceholder" v-if="tables.length === 0">
            No tables available. Please create a new table.
          </li>
          <li id="joinGameTableListElement" class="joinGameTableListElement" @click="joinTable(table.id)"
              v-for="(table, index) in tables" :key="table.id">
            Table {{ index + 1 }}: (Players: {{
              table.players ? table.players.filter(p => p)
                  .map(p => p.name).join(', ') || 'No players yet' : 'No players yet'
            }})
          </li>
        </ul>
        <div id="joinGameTableActions">
          <button id="refreshTableListButton" class="button" @click="fetchTables">Refresh</button>
          <button id="newTableButton" class="button" @click="createTable">New Table</button>
        </div>
      </div>
    </div>

    <!--                            ONLINE TABLE                          -->
    <div id="onlineTableContainer" class="tableContainer">
      <div id="onlinePlayer1FieldContainer">
        <div id="onlinePlayer1Name">Empty</div>
        <div id="onlinePlayer1Balance">Balance:</div>
        <div id="onlinePlayer1Bet">Bet:</div>
      </div>
      <div id="onlineTableFieldContainer">
        <div id="croupierCardsField" class="tableCardsField"></div>
        <div id="infoAndTimerContainer" class="infoField">
          <div id="timerStateMessage"></div>
          <div id="timerCountdownField">0</div>
        </div>
        <div id="player1CardsField" class="tableCardsField"></div>
        <div id="player2CardsField" class="tableCardsField"></div>
        <div id="player3CardsField" class="tableCardsField"></div>
      </div>
      <div id="onlinePlayer3FieldContainer">
        <div id="onlinePlayer3Name">Empty</div>
        <div id="onlinePlayer3Balance">Balance:</div>
        <div id="onlinePlayer3Bet">Bet:</div>
      </div>
      <div id="onlinePlayer2FieldContainer">
        <div id="onlinePlayer2InfoFieldContainer">
          <div id="onlinePlayer2InformationContainer">
            <div id="onlinePlayer2Balance"></div>
            <div id="onlinePlayer2Name"></div>
            <div id="onlinePlayer2Bet"></div>
          </div>
        </div>
        <div id="onlinePlayer2ControlsFieldContainer">
          <div id="onlinePlayer2BetsPanelContainer" v-if="this.player && this.player.currentAction === 'BETTING'">
            <div id="betsControlAmountContainer">
              Amount: {{ this.playerBetCount }}
            </div>
            <div id="betsControlsCoinContainer">
              <button id="betsControlsCoin5" class="coinButton coinButton1" @click="this.playerBetCount += 5">5</button>
              <button id="betsControlsCoin10" class="coinButton coinButton2" @click="this.playerBetCount += 10">10
              </button>
              <button id="betsControlsCoin25" class="coinButton coinButton3" @click="this.playerBetCount += 25">25
              </button>
              <button id="betsControlsCoin50" class="coinButton coinButton4" @click="this.playerBetCount += 50">50
              </button>
              <button id="betsControlsCoin100" class="coinButton coinButton5" @click="this.playerBetCount += 100">100
              </button>
              <button id="betsControlPlaceButton" class="button" @click="placeBet">Place Bet</button>
              <button id="betsControlPlaceButton" class="button" @click="this.playerBetCount = 0">Reset</button>
            </div>
          </div>
          <div id="onlinePlayer2ActionsPanelContainer" v-if="this.player && this.player.currentAction === 'DECIDING'">
            <button
                v-for="decision in this.player.availableDecisions"
                :key="decision"
                class="button"
                @click="makeMove(decision)">
              {{ decision }}
            </button>
          </div>
          <div
              id="resultField"
              v-if="this.table && this.table.gameState === 'ROUND_SUMMARY'"
          >
            {{
              this.player.lastRoundResult === 'WON' ? 'You WON!' :
                  this.player.lastRoundResult === 'LOST' ? 'You lost...' :
                      this.player.lastRoundResult === 'DRAW' ? 'It\'s a draw!' :
                          this.player.lastRoundResult === 'BLACKJACK' ? 'BLACKJACK!' :
                              ''
            }}
          </div>
        </div>
      </div>
    </div>

    <!--                            PRACTICE TABLE                          -->
    <div id="practiceTableContainer" class="tableContainer">
      <div id="gameInfoContainer">
        <div id="houseEdgeContainer">
          <div id="houseEdgeH">House</div>
          <div id="houseEdgeIndicator">
            <div
                id="indicatorTop"
                :style="{ height: indicatorTopHeight + '%' }"
            ></div>
            <div id="middleLine"></div>
            <div
                id="indicatorBottom"
                :style="{ height: indicatorBottomHeight + '%' }"
            ></div>
          </div>
          <div id="houseEdgeP">Player</div>
        </div>
        <div id="otherStatsContainer">
          <span class="valueContainer">
            <span class="statDescription">Running Value: </span><span class="statValue">{{ runningValue }}</span>
          </span>
          <span class="valueContainer">
            <span class="statDescription">True Value: </span><span class="statValue">{{ trueValue }}</span>
          </span>
          <span class="valueContainer">
            <span class="statDescription">House Edge: </span><span class="statValue">{{ houseEdge }}%</span>
          </span>
          <span class="sectionBreak"></span>

          <span class="valueContainer">
            <span class="statDescription">Cards in play: </span><span class="statValue">{{ cardsInPlay }}</span>
          </span>
          <span class="valueContainer">
            <span class="statDescription">Cards played: </span><span class="statValue">{{ cardsPlayed }}</span>
          </span>
          <span class="valueContainer">
            <span class="statDescription">Cards left: </span><span class="statValue">{{ cardsLeft }}</span>
          </span>
          <span class="sectionBreak"></span>

          <span class="valueContainer">
            <span class="statDescription">Croupier card value: </span><span class="statValue">{{ croupierValue }}</span>
          </span>
          <span class="valueContainer">
            <span class="statDescription">Your card value: </span><span class="statValue">{{ playerValue }}</span>
          </span>
          <span class="valueContainer">
          <span class="statDescription">Bot card value: </span><span class="statValue">{{ botValue }}</span>
          </span>
          <span class="sectionBreak"></span>

          <span class="valueContainer">
          <span class="statDescription">Your profit: </span><span class="statValue">{{ playerProfit }}</span>
          </span>
          <span class="valueContainer">
          <span class="statDescription">Bot profit: </span><span class="statValue">{{ botProfit }}</span>
          </span>
        </div>
      </div>
      <div id="practiceTableFieldContainer">
        <div id="practiceCroupierCardsField" class="tableCardsField"></div>
        <div id="practiceInfoAndTimerContainer" class="infoField">
          <div id="practiceTimerStateMessage"></div>
          <div id="practiceTimerCountdownField"></div>
        </div>
        <div id="localPlayerCardsField" class="tableCardsField"></div>
        <div id="botCardsField" class="tableCardsField"></div>
      </div>
      <div id="botFieldContainer" v-if="this.practiceTable && this.practiceTable.botPlayer">
        <div id="botInfoContainer">
          <div id="botName">{{ this.practiceTable.botPlayer.name || "Unknown" }}</div>
          <div id="botBalance">Balance: {{ this.practiceTable.botPlayer.balance || 0 }}</div>
          <div id="botBet">Bet: {{ this.practiceTable.botPlayer.bet || 0 }}</div>
        </div>
        <div id="botDecisionsContainer">
          <span id="botDecisionsTitle">Decisions:</span>
          <div
              v-if="this.practiceTable.botPlayer.lastDecisions && this.practiceTable.botPlayer.lastDecisions.length > 0">
            <ul id="botDecisionsList">
              <li v-for="(decision, index) in this.practiceTable.botPlayer.lastDecisions" :key="index">
                {{ decision }}
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div id="localPlayerFieldContainer">
        <div id="localPlayerInfoFieldContainer">
          <div id="localPlayerInformationContainer" v-if="this.player">
            <div id="localPlayerBalance">Balance: {{ this.player.learningBalance || 0 }}</div>
            <div id="localPlayerName">{{ this.player.name || 'Unknown' }}</div>
            <div id="localPlayerBet">Bet: {{ this.player.bet || 0 }}</div>
          </div>
        </div>
        <div id="localPlayerControlsFieldContainer">
          <div id="localPlayerBetsPanelContainer" v-if="this.player && this.player.currentAction === 'BETTING'">
            <div id="betsControlAmountContainer">
              Amount: {{ this.playerBetCount }}
            </div>
            <div id="betsControlsCoinContainer">
              <button id="betsControlsCoin5" class="coinButton coinButton1" @click="this.playerBetCount += 5">5</button>
              <button id="betsControlsCoin10" class="coinButton coinButton2" @click="this.playerBetCount += 10">10
              </button>
              <button id="betsControlsCoin25" class="coinButton coinButton3" @click="this.playerBetCount += 25">25
              </button>
              <button id="betsControlsCoin50" class="coinButton coinButton4" @click="this.playerBetCount += 50">50
              </button>
              <button id="betsControlsCoin100" class="coinButton coinButton5" @click="this.playerBetCount += 100">100
              </button>
              <button id="betsControlPlaceButton" class="button" @click="placePracticeBet">Place Bet</button>
              <button id="betsControlPlaceButton" class="button" @click="this.playerBetCount = 0">Reset</button>
            </div>
          </div>
          <div id="localPlayerActionsPanelContainer" v-if="this.player && this.player.currentAction === 'DECIDING'">
            <button
                v-for="decision in this.player.availableDecisions"
                :key="decision"
                class="button"
                @click="practiceMakeMove(decision)">
              {{ decision }}
            </button>
          </div>
          <div
              id="practiceResultField"
              v-if="this.practiceTable && this.practiceTable.gameState === 'ROUND_SUMMARY'"
          >
            {{
              this.player.lastRoundResult === 'WON' ? 'You WON!' :
                  this.player.lastRoundResult === 'LOST' ? 'You lost...' :
                      this.player.lastRoundResult === 'DRAW' ? 'It\'s a draw!' :
                          this.player.lastRoundResult === 'BLACKJACK' ? 'BLACKJACK!' :
                              'Unknown result'
            }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import SockJS from 'sockjs-client';
import {Client} from '@stomp/stompjs';

export default {

  beforeUnmount() {
    window.removeEventListener("beforeunload", this.handleBeforeUnload);
    this.disconnectFromSocket()
  },

  data() {
    return {
      playerName: '',
      player: null,
      tables: [],
      table: null,
      practiceTable: null,
      playerBetCount: 0,
      stompClient: null,
    };
  },

  computed: {
    indicatorTopHeight() {
      return this.practiceTable
          ? 50 + ((this.practiceTable.houseEdge / 3) * 50)
          : 50;
    },
    indicatorBottomHeight() {
      return this.practiceTable
          ? 50 - ((this.practiceTable.houseEdge / 3) * 50)
          : 50;
    },
    houseEdge() {
      return this.practiceTable ? this.practiceTable.houseEdge.toFixed(2) : "0.00";
    },
    runningValue() {
      return this.practiceTable ? this.practiceTable.runningValue : 0;
    },
    trueValue() {
      return this.practiceTable ? this.practiceTable.trueValue.toFixed(2) : "0.00";
    },
    cardsInPlay() {
      return this.practiceTable ? this.practiceTable.totalCards : 0;
    },
    cardsPlayed() {
      return this.practiceTable ? this.practiceTable.cardsInPlay.cardsDealt : 0;
    },
    cardsLeft() {
      return this.practiceTable ? this.practiceTable.cardsInPlay.cardsLeft : 0;
    },
    croupierValue() {
      return this.practiceTable ? (this.practiceTable.croupierValue ? this.practiceTable.croupierValue : 0) : 0;
    },
    playerValue() {
      return this.practiceTable ? (this.practiceTable.playerValue ? this.practiceTable.playerValue : 0) : 0;
    },
    botValue() {
      return this.practiceTable ? (this.practiceTable.botValue ? this.practiceTable.botValue : 0) : 0;
    },
    playerProfit() {
      return this.practiceTable ? (this.practiceTable.player.learningBalance - this.practiceTable.learningBalance) : 0;
    },
    botProfit() {
      return this.practiceTable ? (this.practiceTable.botPlayer.balance - this.practiceTable.learningBalance) : 0;
    },
  },

  methods: {
    hoverMode(description) {
      const modeButton = event.target;
      modeButton.innerText = description;
    },
    resetModeText(originalText) {
      const modeButton = event.target;
      modeButton.innerText = originalText;
    },

    async handleBeforeUnload(event) {
      await this.disconnectFromSocket();

      event.preventDefault();
      event.returnValue = "";
    },

    handleNameSubmit() {
      if (this.playerName) {
        this.getPlayer();
      } else {
        alert('Please enter a name');
      }
    },

    async getPlayer() {
      try {
        const response = await fetch(`/api/v1/player?name=${this.playerName}`);
        if (response.ok) {
          document.getElementById('nameInputContainer').style.visibility = 'hidden';
          document.getElementById('gameModeMenuContainer').style.visibility = 'visible';
          this.player = await response.json();
        } else {
          document.getElementById('nameNotFoundContainer').style.visibility = 'visible';
          document.getElementById('nameInputContainer').style.visibility = 'hidden';
        }
      } catch (error) {
        console.error('Error fetching player:', error);
      }
    },

    returnToNameInput() {
      document.getElementById('nameNotFoundContainer').style.visibility = 'hidden';
      document.getElementById('nameInputContainer').style.visibility = 'visible';
    },

    async handleCreatePlayer() {
      try {
        const postResponse = await fetch(`/api/v1/player?name=${this.playerName}`, {method: 'POST'});
        if (postResponse.ok) {
          this.player = await postResponse.json();
          document.getElementById('nameNotFoundContainer').style.visibility = 'hidden';
          document.getElementById('gameModeMenuContainer').style.visibility = 'visible';
        }
      } catch (error) {
        console.error('Error creating player:', error);
      }
    },

    async selectLearnMode() {
      await this.joinPracticeTable();
    },

    async selectOnlineMode() {
      await this.fetchTables();
      document.getElementById('gameModeMenuContainer').style.visibility = 'hidden';
      document.getElementById('joinGameContainer').style.visibility = 'visible';
    },

    async createTable() {
      try {
        const response = await fetch('/api/v1/table', {method: 'POST'});
        if (response.ok) {
          const table = await response.json();
          await this.joinTable(table.id);
          document.getElementById('joinGameContainer').style.visibility = 'hidden';
          document.getElementById('onlineTableContainer').style.visibility = 'visible';
        }
      } catch (error) {
        console.error('Error creating table:', error);
      }
    },

    async joinTable(tableId) {
      try {
        const response = await fetch(`/api/v1/table/join/${tableId}?playerId=${this.player.id}`, {method: 'PUT'});
        if (response.ok) {
          this.table = await response.json();
          this.connectToSocket(this.table.id)
          document.getElementById('joinGameContainer').style.visibility = 'hidden';
          document.getElementById('onlineTableContainer').style.visibility = 'visible';
          this.updateTable();
        } else {
          const info = await response.text();
          alert(info)
          console.error('Error joining table: ', info);
        }
      } catch (error) {
        console.error('Error joining table: ', error.toString());
      }
    },

    async joinPracticeTable() {
      try {
        const response = await fetch(`/api/v1/practiceTable/join?playerId=${this.player.id}`, {method: 'PUT'});
        if (response.ok) {
          this.practiceTable = await response.json();
          this.connectToPracticeSocket(this.practiceTable.id)
          this.updatePracticeTable();
          document.getElementById('gameModeMenuContainer').style.visibility = 'hidden';
          document.getElementById('practiceTableContainer').style.visibility = 'visible';
        } else {
          const info = await response.text();
          alert(info)
          console.error('Error joining table: ', info);
        }
      } catch (error) {
        console.error('Error joining table: ', error.toString());
      }
    },

    connectToSocket(tableId) {
      const socket = new SockJS("http://localhost:8080/api/v1/table/connections");

      this.stompClient = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 2000,
        heartbeatOutgoing: 2000,
      });

      this.stompClient.onConnect = () => {
        this.stompClient.subscribe(`/topic/table/${tableId}`, (response) => {
          this.table = JSON.parse(response.body);
          this.updateTable();
        });
      };

      this.stompClient.activate();
    },

    connectToPracticeSocket(tableId) {
      const socket = new SockJS("http://localhost:8080/api/v1/table/connections");

      this.stompClient = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 2000,
        heartbeatOutgoing: 2000,
      });

      this.stompClient.onConnect = () => {
        this.stompClient.subscribe(`/topic/practiceTable/${tableId}`, (response) => {
          this.practiceTable = JSON.parse(response.body);
          this.updatePracticeTable();
        });
      };

      this.stompClient.activate();
    },

    async disconnectFromSocket() {
      await this.leaveTable();

      if (this.stompClient) {
        this.stompClient.deactivate();
        this.stompClient = null;
      }
    },

    renderPlayerCards(player, containerId) {
      const container = document.getElementById(containerId);
      if (container) container.innerHTML = "";
      if (player && player.hand && player.hand.cards) {
        player.hand.cards.forEach((card, index) => {
          const cardImg = this.createCardElement(card, index);
          container.appendChild(cardImg);
        });
      }
    },

    createCardElement(card, index) {
      const cardImg = document.createElement("img");
      cardImg.className = "Card";
      if (card.isHidden) {
        cardImg.src = require(`@/assets/images/cards/Rev.png`);
        cardImg.alt = `$Hidden`;
      } else {
        cardImg.src = require(`@/assets/images/cards/${card.rank}${card.suit}.svg`);
        cardImg.alt = `${card.rank}${card.suit}`;
      }
      cardImg.style.setProperty("--index", index);
      return cardImg;
    },

    renderAllCards() {
      const croupierContainer = document.getElementById("croupierCardsField");
      if (croupierContainer) {
        croupierContainer.innerHTML = "";
        if (this.table && this.table.croupier && this.table.croupier.hand.cards) {
          this.table.croupier.hand.cards.forEach((card, index) => {
            const cardImg = this.createCardElement(card, index);
            croupierContainer.appendChild(cardImg);
          });
        }
      }

      const otherPlayers = (this.table.players || []).filter(p => p && p.id !== this.player.id);

      this.renderPlayerCards(otherPlayers[0], "player1CardsField");
      this.renderPlayerCards(this.player, "player2CardsField");
      this.renderPlayerCards(otherPlayers[1], "player3CardsField");
    },

    renderAllPracticeCards() {
      this.renderPlayerCards(this.player, "localPlayerCardsField");
      this.renderPlayerCards(this.practiceTable.botPlayer, "botCardsField");
      this.renderPlayerCards(this.practiceTable.croupier, "practiceCroupierCardsField");
    },

    updateTable() {
      if (!this.table || !this.player) {
        console.error('Table or player is not defined.');
        return;
      }
      this.player = this.table.players.find(p => p.id === this.player.id) || this.player;
      const otherPlayers = (this.table.players || []).filter(p => p && p.id !== this.player.id);

      document.getElementById("onlinePlayer2Name").innerText = this.player.name || 'Unknown';
      document.getElementById("onlinePlayer2Balance").innerText = `Balance: ${this.player.balance || 0}`;
      document.getElementById("onlinePlayer2Bet").innerText = `Bet: ${this.player.bet || 0}`;

      const seatMapping = [
        {name: "onlinePlayer1Name", balance: "onlinePlayer1Balance", bet: "onlinePlayer1Bet"},
        {name: "onlinePlayer3Name", balance: "onlinePlayer3Balance", bet: "onlinePlayer3Bet"},
      ];

      seatMapping.forEach((seat, index) => {
        const player = otherPlayers[index] || {};

        document.getElementById(seat.name).innerText = player.name || 'Empty';
        document.getElementById(seat.balance).innerText = `Balance: ${player.balance || 0}`;
        document.getElementById(seat.bet).innerText = `Bet: ${player.bet || 0}`;
      });
      this.updateTimerField();
      this.renderAllCards();
    },

    updatePracticeTable() {
      if (!this.practiceTable || !this.player) {
        console.error('Table or player is not defined.');
        return;
      }
      this.player = this.practiceTable.player || this.player;

      this.updatePracticeTimerField();
      this.renderAllPracticeCards();
    },

    async leaveTable() {
      if (!(this.table || this.practiceTable) || !this.player) {
        console.warn("Cannot leave table: Table or player is not defined.");
        return;
      }

      let endpointUrl;

      if (this.table) {
        endpointUrl = `/api/v1/table/leave/${this.table.id}`;
      } else {
        endpointUrl = `/api/v1/practiceTable/leave/${this.practiceTable.id}`;

      }
      const queryParams = `?playerId=${this.player.id}`;

      try {
        const response = await fetch(endpointUrl + queryParams, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (response.ok) {
          await response.json();
        } else if (response.status === 400) {
          const errorMessage = await response.text();
          console.error("Failed to leave table:", errorMessage);
          alert(`Failed to leave table: ${errorMessage}`);
        } else {
          console.error("Unexpected error when leaving table:", response.statusText);
        }
      } catch (error) {
        console.error("Error while leaving table:", error);
      }
    },

    async fetchTables() {
      try {
        const response = await fetch('/api/v1/table');
        if (response.ok) {
          this.tables = await response.json();
        }
      } catch (error) {
        console.error('Error fetching tables:', error);
      }
    },

    async placeBet() {
      try {
        const response = await fetch(`/api/v1/table/bet/${this.table.id}?playerId=${this.player.id}&amount=${this.playerBetCount}`, {method: 'PUT'});
        if (response.ok) {
          this.table = await response.json();
          this.playerBetCount = 0;
        } else {
          const info = await response.text();
          alert(info);
          console.error(info);
        }
      } catch (error) {
        console.error('Error fetching tables:', error);
      }
    },

    async placePracticeBet() {
      try {
        const response = await fetch(`/api/v1/practiceTable/bet/${this.practiceTable.id}?playerId=${this.player.id}&amount=${this.playerBetCount}`, {method: 'PUT'});
        if (response.ok) {
          this.practiceTable = await response.json();
          this.playerBetCount = 0;
        } else {
          const info = await response.text();
          alert(info);
          console.error(info);
        }
      } catch (error) {
        console.error('Error fetching tables: ', error);
      }
    },

    async makeMove(decision) {
      if (!this.table || !this.player) {
        console.error("Table or player is not defined.");
        return;
      }

      const endpointUrl = `/api/v1/player/${this.table.id}/makeMove`;
      const queryParams = `?playerId=${this.player.id}&playerDecision=${decision}`;

      try {
        const response = await fetch(endpointUrl + queryParams, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) {
          const errorMessage = await response.text();
          console.error(`Failed to make move '${decision}':`, errorMessage);
          alert(`Failed to make move: ${errorMessage}`);
        }
      } catch (error) {
        console.error(`Error while making move '${decision}':`, error);
      }
    },

    async practiceMakeMove(decision) {
      if (!this.practiceTable || !this.player) {
        console.error("Table or player is not defined.");
        return;
      }

      const endpointUrl = `/api/v1/player/practice/${this.practiceTable.id}/makeMove`;
      const queryParams = `?playerDecision=${decision}`;

      try {
        const response = await fetch(endpointUrl + queryParams, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) {
          const errorMessage = await response.text();
          console.error(`Failed to make move '${decision}':`, errorMessage);
          alert(`Failed to make move: ${errorMessage}`);
        }
      } catch (error) {
        console.error(`Error while making move '${decision}':`, error);
      }
    },

    updateTimerField() {
      const countdownTime = this.table?.countdownTime || 0;
      const message = this.table.stateMessage;

      if (countdownTime === 0) {
        console.error("Countdown time is 0 or invalid. Timer won't start.");
        document.getElementById("infoAndTimerContainer").style.visibility = "hidden";
        return;
      }

      this.$nextTick(() => {
        const timerField = document.getElementById("timerCountdownField");
        const infoField = document.getElementById("timerStateMessage");

        if (!timerField) {
          console.error("Timer field not found!");
          return;
        }

        if (this.timerInterval) {
          clearInterval(this.timerInterval);
        }
        document.getElementById("infoAndTimerContainer").style.visibility = "visible";
        timerField.innerText = `${countdownTime}s`;
        infoField.innerText = message;

        let remainingTime = countdownTime;
        this.timerInterval = setInterval(() => {
          remainingTime--;
          const countdownElement = document.getElementById("timerCountdownField");

          if (countdownElement) {
            countdownElement.innerText = `${remainingTime}s`;
          }

          if (remainingTime <= 0) {
            clearInterval(this.timerInterval);
            this.timerInterval = null;
            this.handleTimerEnd();
          }
        }, 1000);
      });
    },

    updatePracticeTimerField() {
      const countdownTime = this.practiceTable?.countdownTime || 0;
      const message = this.practiceTable.stateMessage;

      if (countdownTime === 0) {
        console.error("Countdown time is 0 or invalid. Timer won't start.");
        document.getElementById("practiceInfoAndTimerContainer").style.visibility = "hidden";
        return;
      }

      this.$nextTick(() => {
        const timerField = document.getElementById("practiceTimerCountdownField");
        const infoField = document.getElementById("practiceTimerStateMessage");

        if (!timerField) {
          console.error("Timer field not found!");
          return;
        }

        if (this.timerInterval) {
          clearInterval(this.timerInterval);
        }
        document.getElementById("practiceInfoAndTimerContainer").style.visibility = "visible";
        timerField.innerText = `${countdownTime}s`;
        infoField.innerText = message;

        let remainingTime = countdownTime;
        this.timerInterval = setInterval(() => {
          remainingTime--;
          const countdownElement = document.getElementById("practiceTimerCountdownField");

          if (countdownElement) {
            countdownElement.innerText = `${remainingTime}s`;
          }

          if (remainingTime <= 0) {
            clearInterval(this.timerInterval);
            this.timerInterval = null;
            this.handleTimerEnd();
          }
        }, 1000);
      });
    },

    handleTimerEnd() {
      document.getElementById("practiceInfoAndTimerContainer").style.visibility = "hidden";
    },
  },
};
</script>
<style scoped>
</style>

