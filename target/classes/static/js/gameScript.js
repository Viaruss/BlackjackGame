// document.addEventListener('DOMContentLoaded', () => {
//     const nameInputContainer = document.getElementById('nameInputContainer');
//     const gameModeMenuContainer = document.getElementById('gameModeMenuContainer');
//     const onlineMenuContainer = document.getElementById('onlineMenuContainer');
//     const joinGameContainer = document.getElementById('joinGameContainer');
//     const onlineTableContainer = document.getElementById('onlineTableContainer');
//
//     const nameInputSubmit = document.getElementById('nameInputSubmit');
//     const nameInputField = document.getElementById('nameInputField');
//     const practiceModeButton = document.getElementById('practiceModeButton');
//     const learnModeButton = document.getElementById('learnModeButton');
//     const onlineModeButton = document.getElementById('onlineModeButton');
//     const newTableButton = document.getElementById('newTableButton');
//     const joinTableButton = document.getElementById('joinTableButton');
//     const refreshTableListButton = document.getElementById('refreshTableListButton');
//
//     // const onlinePlayer1FieldContainer = document.getElementById('onlinePlayer1FieldContainer');
//     // const onlinePlayer2FieldContainer = document.getElementById('onlinePlayer2FieldContainer');
//     // const onlinePlayer3FieldContainer = document.getElementById('onlinePlayer3FieldContainer');
//     // const onlineTableFieldContainer = document.getElementById('onlineTableFieldContainer');
//     // const localPlayerInformationContainer = document.getElementById('localPlayerInformationContainer');
//     // const localPlayerBetsPanelContainer = document.getElementById('localPlayerBetsPanelContainer');
//     // const betsControlAmountContainer = document.getElementById('betsControlAmountContainer');
//     // const betsControlsCoinContainer = document.getElementById('betsControlsCoinContainer');
//     // const localPlayerActionsPanelContainer = document.getElementById('localPlayerActionsPanelContainer');
//
//     let tableModel = null;
//     let player = null;
//
//     function showElement(element) {
//         element.style.visibility = 'visible';
//     }
//
//     function hideElement(element) {
//         element.style.visibility = 'hidden';
//     }
//
//     nameInputSubmit.addEventListener('click', async () => {
//         if (nameInputField.value !== '') {
//             await getPlayerRequest(nameInputField.value)
//                 .then(() => {
//                     hideElement(nameInputContainer);
//                     showElement(gameModeMenuContainer);
//                 });
//         } else {
//             alert('Please enter a name');
//         }
//     });
//
//     practiceModeButton.addEventListener('click', () => {
//         hideElement(gameModeMenuContainer);
//         // TODO: Add functionality for practice mode
//     });
//
//     learnModeButton.addEventListener('click', () => {
//         hideElement(gameModeMenuContainer);
//         // TODO: Add functionality for learn mode
//     });
//
//     onlineModeButton.addEventListener('click', () => {
//         hideElement(gameModeMenuContainer);
//         showElement(onlineMenuContainer);
//     });
//
//     newTableButton.addEventListener('click', () => {
//         createTableRequest()
//             .then(() => console.log('Table created'));
//     });
//
//     joinTableButton.addEventListener('click', async () => {
//         await getAllTables()
//             .then(() => {
//                 hideElement(onlineMenuContainer);
//                 showElement(joinGameContainer);
//             })
//     });
//
//     refreshTableListButton.addEventListener('click', async () => {
//         await getAllTables();
//     });
//
//     async function getPlayerRequest(playerName) {
//         try {
//             let response = await fetch(`/api/v1/player?name=${playerName}`, {
//                 method: 'GET',
//                 headers: {
//                     'Content-Type': 'application/json'
//                 }
//             });
//
//             if (response.ok) {
//                 player = await response.json();
//                 if (player) {
//                     tableModel = player;
//                     console.log('Player fetched:', tableModel);
//                     return;
//                 }
//             }
//
//             // If GET request fails or returns null, try POST request
//             response = await fetch(`/api/v1/player?name=${playerName}`, {
//                 method: 'POST',
//                 headers: {
//                     'Content-Type': 'application/json'
//                 }
//             });
//
//             if (response.ok) {
//                 player = await response.json();
//                 if (player) {
//                     tableModel = player;
//                     console.log('Player created:', tableModel);
//                 } else {
//                     console.error('Error: Player creation returned null');
//                 }
//             } else {
//                 console.error('Error: Failed to create player', response.statusText);
//             }
//         } catch (error) {
//             console.error('Error: Network or server issue', error);
//         }
//     }
//
//     async function createTableRequest() {
//         try {
//             const response = await fetch('/api/v1/table', {
//                 method: 'POST',
//                 headers: {
//                     'Content-Type': 'application/json'
//                 }
//             });
//
//             if (response.ok) {
//                 const table = await response.json();
//                 if (table) {
//                     tableModel = table;
//                     console.log('Table created:', tableModel);
//
//                     await joinTableRequest(tableModel.id)
//                         .then(() => console.log('Table joined'))
//                         .then(() => {
//                             hideElement(onlineMenuContainer);
//                             showElement(onlineTableContainer);
//                         });
//
//                 } else {
//                     console.error('Error: Table creation returned null');
//                 }
//             } else {
//                 console.error('Error: Failed to create table', response.statusText);
//             }
//         } catch (error) {
//             console.error('Error: Network or server issue', error);
//         }
//     }
//
//     async function joinTableRequest(tableId) {
//         try {
//             const response = await fetch(`/api/v1/table/join/${tableId}?playerId=${player.id}`, {
//                 method: 'PUT',
//                 headers: {
//                     'Content-Type': 'application/json'
//                 }
//             });
//
//             if (response.ok) {
//                 const table = await response.json();
//                 if (table) {
//                     tableModel = table;
//                     console.log('Joined table:', tableModel);
//                 } else {
//                     console.error('Error: Join table returned null');
//                 }
//             } else {
//                 console.error('Error: Failed to join table', response.statusText);
//             }
//         } catch (error) {
//             console.error('Error: Network or server issue', error);
//         }
//     }
//
//     async function getAllTables() {
//         try {
//             const response = await fetch('/api/v1/table', {
//                 method: 'GET',
//                 headers: {
//                     'Content-Type': 'application/json'
//                 }
//             });
//
//             if (response.ok) {
//                 const tables = await response.json();
//                 const joinGameTableList = document.getElementById('joinGameTableList');
//                 joinGameTableList.innerHTML = ''; // Clear existing list
//
//                 tables.forEach((table, index) => {
//                     const tableNumber = index + 1;
//                     const players = table.players.map(player => player.name);
//                     while (players.length < 3) {
//                         players.push('empty');
//                     }
//
//                     const listItem = document.createElement('li');
//                     listItem.textContent = `table ${tableNumber}, Players: ${players.join(', ')}`;
//
//                     const joinButton = document.createElement('button');
//                     joinButton.textContent = 'Join';
//                     joinButton.addEventListener('click', async () => {
//                         await joinTableRequest(table.id);
//                         hideElement(joinGameContainer);
//                         showElement(onlineTableContainer);
//                     });
//
//                     listItem.appendChild(joinButton);
//                     joinGameTableList.appendChild(listItem);
//                 });
//             } else {
//                 console.error('Error: Failed to fetch tables', response.statusText);
//             }
//         } catch (error) {
//             console.error('Error: Network or server issue', error);
//         }
//     }
// });