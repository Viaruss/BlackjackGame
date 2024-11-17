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

    <div id="onlineMenuContainer" class="popUpContainer">
      <button id="newTableButton" class="button" @click="createTable">New Table</button>
      <button id="joinTableButton" class="button" @click="handleJoinTable">Join Table</button>
    </div>

    <div id="joinGameContainer" class="popUpContainer">
      <div id="joinGameTitleContainer" class="titleContainer">
        <h1 id="joinGameTitle" class="title">Tables:</h1>
      </div>
      <div id="joinGameTableListContainer" class="contentContainer">
        <ul id="joinGameTableList">
          <li id="joinGameTableListElement" class="joinGameTableListElement" @click="joinTable(table.id)" v-for="(table, index) in tables" :key="table.id">
            Table {{ index+1 }}: (Players: {{ table.players.map(p => p.name).join(', ') }})
          </li>
        </ul>
        <button id="refreshTableListButton" class="button" @click="fetchTables">Refresh</button>
      </div>
    </div>

    <div id="onlineTableContainer" class="tableContainer">-->
          <div id="onlinePlayer1FieldContainer">
              <div id="onlinePlayer1Name">Player 1 Name</div>
              <div id="onlinePlayer1Balance">Balance: 20</div>
              <div id="onlinePlayer1Bet">Bet: 100</div>
          </div>
          <div id="onlineTableFieldContainer">
              <div id="croupierCardsField" class="tableCardsField">croupierCardsField</div>
              <div id="player1CardsField" class="tableCardsField">player1CardsField</div>
              <div id="player2CardsField" class="tableCardsField">player2CardsField</div>
              <div id="player3CardsField" class="tableCardsField">player3CardsField</div>
          </div>
          <div id="onlinePlayer3FieldContainer">
              <div id="onlinePlayer3Name">Player 3 Name</div>
              <div id="onlinePlayer3Balance">Balance: 20</div>
              <div id="onlinePlayer3Bet">Bet: 100</div>
          </div>
          <div id="onlinePlayer2FieldContainer">
              <div id="onlinePlayer2InfoFieldContainer">
                  <div id="onlinePlayer2InformationContainer">
                      <div id="onlinePlayer2Balance">Balance: 2000</div>
                      <div id="onlinePlayer2Name">Player Name</div>
                      <div id="onlinePlayer2Bet">Bet: 100</div>
                  </div>
              </div>
              <div id="onlinePlayer2ControlsFieldContainer">
                  <div id="onlinePlayer2BetsPanelContainer">
                      <!--            player bets controls go here-->
                      <div id="betsControlAmountContainer">
                          Amount: 150
                      </div>
                      <div id="betsControlsCoinContainer">
                          <button id="betsControlsCoin5" class="coinButton coinButton1">5</button>
                          <button id="betsControlsCoin10" class="coinButton coinButton2">10</button>
                          <button id="betsControlsCoin25" class="coinButton coinButton3">25</button>
                          <button id="betsControlsCoin50" class="coinButton coinButton4">50</button>
                          <button id="betsControlsCoin100" class="coinButton coinButton5">100</button>
                          <button id="betsControlPlaceButton" class="button">Place Bet</button>
                      </div>
                  </div>
                  <div id="onlinePlayer2ActionsPanelContainer">
                      actionsPanel
                      <!--            player actions go here-->
                  </div>
              </div>
          </div>
      </div>
  </div>
</template>

<script>
export default {
  //TODO: Delete after debugging
  mounted() {
    window.vm = this; // Assigns the Vue instance to `vm`
    console.log('Vue instance mounted and accessible via window.vm');
  },
  data() {
    return {
      playerName: '',
      tables: [],
      player: null,
    };
  },
  methods: {
    handleNameSubmit() {
      if (this.playerName) {
        this.getPlayer(this.playerName).then(() => {
          document.getElementById('nameInputContainer').style.visibility = 'hidden';
          document.getElementById('gameModeMenuContainer').style.visibility = 'visible';
        });
      } else {
        alert('Please enter a name');
      }
    },
    async getPlayer(name) {
      try {
        const response = await fetch(`/api/v1/player?name=${name}`);
        if (response.ok) {
          this.player = await response.json();
        } else {
          const postResponse = await fetch(`/api/v1/player?name=${name}`, {method: 'POST'});
          if (postResponse.ok) this.player = await postResponse.json();
        }
      } catch (error) {
        console.error('Error fetching/creating player:', error);
      }
    },
    selectPracticeMode() {
      document.getElementById('gameModeMenuContainer').style.visibility = 'hidden';
    },
    selectLearnMode() {
      document.getElementById('gameModeMenuContainer').style.visibility = 'hidden';
    },
    selectOnlineMode() {
      document.getElementById('gameModeMenuContainer').style.visibility = 'hidden';
      document.getElementById('onlineMenuContainer').style.visibility = 'visible';
    },
    async createTable() {
      try {
        const response = await fetch('/api/v1/table', {method: 'POST'});
        if (response.ok) {
          const table = await response.json();
          await this.joinTable(table.id);
          document.getElementById('onlineMenuContainer').style.visibility = 'hidden';
          document.getElementById('onlineTableContainer').style.visibility = 'visible';
        }
      } catch (error) {
        console.error('Error creating table:', error);
      }
    },
    async handleJoinTable() {
      await this.fetchTables();
      document.getElementById('onlineMenuContainer').style.visibility = 'hidden';
      document.getElementById('joinGameContainer').style.visibility = 'visible';
    },
    async joinTable(tableId) {
      try {
        console.log("trying to join a table")
        const response = await fetch(`/api/v1/table/join/${tableId}?playerId=${this.player.id}`, {method: 'PUT'});
        if (response.ok) {
          const table = await response.json();
          console.log('Joined table:', table);
          document.getElementById('joinGameContainer').style.visibility = 'hidden';
          document.getElementById('onlineTableContainer').style.visibility = 'visible';
        }
      } catch (error) {
        console.error('Error joining table:', error);
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
  },
};
</script>

<style scoped>
/* Scoped styles can be added here */
</style>
