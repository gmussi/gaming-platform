# Testing locally

```shell script
wget https://raw.githubusercontent.com/gmussi/gaming-platform/master/docker-compose.yml
docker-compose up
```

# Backend services

The backend services consists of many services that communicate with each other using Apache Kafka as broker, with multiple topics.

The following services and their responsibilities are listed below:
- Admin Service:
  - User registration
  - User authentication
  - Get tickets for gameplay service
- Gameplay Service
  - Allows players to connect and start matches
  - Sends all player-related events to the player
  - Implements multiple gameplay mechanics:
    - TicTacToe
    - Match Four (TODO)
    - Chess (TODO)
    - Checkers (TODO)
  - Informs other services of connections and disconnections
- Matchmaking Service
  - Find matches for players on specific games
  - Informs other services when a match starts
  - Currently, only supports 1x1 types of games
- Statistics Service 
  - Keeps track of how many players are connected (TODO)
  - Keeps track of how many matches are happening (TODO)
  - Caches the information using infinispan for immediate retrieval (TODO)
  - Exposes interface via Server-Sent-Events (TODO)
- WebAdmin Service
  - Exposes HTML interface to monitor the activities of the server
  - Allows to create players and bots and see them working
  
## Admin service

The admin service provides tools to create account, login and obtain tickets and tokens, that are used to authenticate in other services.

### Check if username is available

**Input**

```shell script
curl --location --request GET 'http://{ADMIN_URL}/auth/available/{username}'
```

**Output** (TEXT/PLAIN)
 
`true` if username is available or `false` if username has been taken

### Register a new user

**Input**

```shell script
curl --location --request POST 'http://{ADMIN_URL}/auth/register' \
 --header 'Content-Type: application/x-www-form-urlencoded' \
 --data-urlencode 'username={username}' \
 --data-urlencode 'password={password}'
```

**Output** (TEXT/PLAIN)

`jwt token` String containing the authorization token to use in requests

### Sign in

**Input**

```shell script
curl --location --request POST 'http://{ADMIN_URL}/auth/login' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username={username}' \
--data-urlencode 'password={password}'
```

**Output** (TEXT/PLAIN)

`jwt token` String containing the authorization token to use in requests

### Getting a ticket
Tickets are single-use tokens used to connect via websocket (since websockets do not accept headers).

**Input**
```shell script
curl --location --request GET 'http://{ADMIN_URL}/ticket/' \
--header 'Authorization: Bearer {jwt token}'
```

**Output** (TEXT/PLAIN)

`ticketId` Ticket to be used to connect to gameplay service 

## Gameplay service

The gameplay service works via JSON communication over WebSockets.

A connection must be opened to `ws://{GAMEPLAY_URL}/player/{username}/{ticketId}`.

### Commands
The following inputs are currently supported:

**Find Match**
```json
{
  "action": "FIND_MATCH",
  "gameType": "TICTACTOE"
}
```

**Cancel match (TODO)**
```json
{
  "action": "CANCEL_MATCH",
  "matchId": "{matchId}"
}
```

**Play move**
```json
{
  "action": "PLAY",
  "matchId": "{matchId}",
  "move": "<object> // depends on game type, check further down"
}
```

### Events

All events sent by the server follows the same format:
```json
{
  "eventType": "<string>",
  "match": "<object> // game state, see Match Object below"
}
```

**Event types**
- START_MATCH: Sent when a match with this player starts
- END_MATCH: Sent when a match with this player is over
- PLAY: Sent each time any player in a match plays

**Match Object**
```json
{
  "matchId": "<string>",
  "players": "<string array> // usernames",
  "winner": "<string> // username", 
  "endType": "<string> DRAW|DISCONNECTION|WINNER",
  "gameType": "<string> TICTACTOE|MATCH_FOUR|CHESS,CHECKERS"
}
```

**End Type**
- DRAW: When no player wins
- WINNER: When there is a clear victory
- DISCONNECTION: The game ended because a player disconnected
- CANCELED: The game ended because a player canceled the match (TODO)



## Statistics service

TODO

# Game Types

## Tic Tac Toe

In TicTacToe, there is a 3x3 board. The positions are addressed as follows:

|   |   |   |
|---|---|---|
| 0 | 1 | 2 |
| 3 | 4 | 5 |
| 6 | 7 | 8 |

When starting the game, all 9 values are set to `0`, the starting player is always `1` and the opponent is always `2`.

Example of a game with a victorious player 2:

|   |   |   |
|---|---|---|
| 2 | 0 | 1 | 
| 0 | 2 | 0 |
| 1 | 1 | 2 |

### Game state

The following properties represent a tictactoe game and are sent together with every match event:

```json
{
  "startingPlayer": "<string>",
  "currentTurnPlayer": "<string>",
  "pos": "<number array>"
}
```
 
### Move object

```json
{
  "pos": "<number> // position on the board (0 - 8)"
}
```

Each input is validated as soon as it arrives against the following conditions:

- Is the player who sent the command the current turn owner?
- Is the position `0`?

If these conditions are not met, the request is ignored.
