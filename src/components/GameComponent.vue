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
        <button id="practiceModeButton" class="button" @click="selectPracticeMode">Practice</button>
        <button id="learnModeButton" class="button" @click="selectLearnMode">Learn</button>
        <button id="onlineModeButton" class="button" @click="selectOnlineMode">Online</button>
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

    <div id="onlineTableContainer" class="tableContainer">
      <div id="onlinePlayer1FieldContainer">
        <div id="onlinePlayer1Name">Empty</div>
        <div id="onlinePlayer1Balance">Balance:</div>
        <div id="onlinePlayer1Bet">Bet:</div>
      </div>
      <div id="onlineTableFieldContainer">
        <div id="croupierCardsField" class="tableCardsField">croupierCardsField</div>
        <div id="infoAndTimerContainer" class="infoField">
          <div id="timerStateMessage"></div>
          <div id="timerCountdownField">0</div>
        </div>
        <div id="player1CardsField" class="tableCardsField">player1CardsField</div>
        <div id="player2CardsField" class="tableCardsField">player2CardsField</div>
        <div id="player3CardsField" class="tableCardsField">player3CardsField</div>
      </div>
      <div id="onlinePlayer3FieldContainer">
        <div id="onlinePlayer3Name">Empty</div>
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

  mounted() {
    //TODO: Delete vm after debugging
    window.vm = this;
    console.log('Vue instance mounted and accessible via window.vm');

    window.addEventListener("beforeunload", this.handleBeforeUnload);
  },

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
      playerBetCount: 0,
      stompClient: null,
    };
  },

  methods: {
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

    selectPracticeMode() {
      document.getElementById('gameModeMenuContainer').style.visibility = 'hidden';
    },

    selectLearnMode() {
      document.getElementById('gameModeMenuContainer').style.visibility = 'hidden';
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
        console.log("trying to join a table")
        const response = await fetch(`/api/v1/table/join/${tableId}?playerId=${this.player.id}`, {method: 'PUT'});
        if (response.ok) {
          this.table = await response.json();
          this.connectToSocket(this.table.id)
          console.log('Joined table:', this.table);
          this.updateTable();
          document.getElementById('joinGameContainer').style.visibility = 'hidden';
          document.getElementById('onlineTableContainer').style.visibility = 'visible';
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
        debug: (str) => console.log(str),
        reconnectDelay: 5000,
        heartbeatIncoming: 2000,
        heartbeatOutgoing: 2000,
      });

      this.stompClient.onConnect = () => {
        console.log("Connected to WebSocket");

        this.stompClient.subscribe(`/topic/table/${tableId}`, (response) => {
          this.table = JSON.parse(response.body);
          this.updateTable();
        });
      };

      this.stompClient.activate();
    },

    async disconnectFromSocket() {
      await this.leaveTable();

      if (this.stompClient) {
        console.log("Disconnecting from WebSocket...");
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

    async leaveTable() {
      if (!this.table || !this.player) {
        console.warn("Cannot leave table: Table or player is not defined.");
        return;
      }

      const endpointUrl = `/api/v1/table/leave/${this.table.id}`;
      const queryParams = `?playerId=${this.player.id}`;

      try {
        const response = await fetch(endpointUrl + queryParams, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (response.ok) {
          const updatedTable = await response.json();
          console.log("Successfully left the table:", updatedTable);
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
        console.log("Placing bet " + this.playerBetCount)
        const response = await fetch(`/api/v1/table/bet/${this.table.id}?playerId=${this.player.id}&amount=${this.playerBetCount}`, {method: 'PUT'});
        if (response.ok) {
          this.table = await response.json();
          console.log(this.table);
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

    updateTimerField() {
      console.log("Updating timer field... table:", this.table);
      console.log("time: ", this.table.countdownTime);
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

    handleTimerEnd() {
      console.log("Timer ended!");
      document.getElementById("infoAndTimerContainer").style.visibility = "hidden";
    },
  },
};
</script>
<style scoped>
</style>

