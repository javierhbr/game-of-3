# Game of Three

### Context
To solve this problem, I choose a simple implementation using ActiveMQ (embedded) to communicate the client with the server.
With that one pair of player can play any time asynchrony and also support other games at the same time. 
All the games and moves are saved in memory using hazelcast as a Memory grid.


![Alt text](readme-file/Diagram02.png
 "Solution diagram")

I choose to use two different queues for the process a payer turn to make an explicit difference of the flows, even if the logic it's the same. because it's running in the same app.

Other option could use only one queue and the listener or the logic determine which player it's sending the play. There are more similar options for that.

The REST endpoint in the app it's to allow a simple interface to play the game. 

REST API sends a JMS message with the turn of playerX through the queueX and a Listener will process the play.
If the game it's automatic, the loop of sends plays and processes them will work until creating a maximum of plays(to avoid infinite loop)

There are other better options  
##### Others options:
- Using a  simple REST endpoints an save the status on a DB any type 
 
  
#### Disclaimer:
- I use GET endpoints to easily operate the game with a browser or curl command


### Build and run 

#### Configurations
changing the property file `application.properties` you can changes som options like:

Game options:
- `game.hazelcast.map.name`  = Name of the hazelcast map to save each game.
- `game.random.range.min`    = Minimum range number when the input number doesn't exist at the game creation time
- `game.random.range.max`    = Maximum range number when the input number doesn't exist at the game creation time  
- `game.automatic.max.play`  = Maximum of plays of **AUTOMATIC** game.

ActiveMQ options:
- `game.messages.connection-factory.name`  = Connection factory name
- `game.messages.queue.player-a.name`  = Name of the queue to process Player A turn
- `game.messages.queue.player-b.name`  = Name of the queue to process Player B turn


Current values:
```
game.messages.connection-factory.name=connectionFactory
game.messages.queue.player-a.name=playerA
game.messages.queue.player-b.name=playerB

game.hazelcast.map.name=allGames
game.random.range.min=0
game.random.range.max=100
game.automatic.max.play=100
```


From the project root directory :

##### Build:
`
 ./gradlew clean build
`
##### Run:
 ###### With gradle:
 `
./gradlew bootRun
 `
 ###### With java:
`
java -jar build/libs/takeaway-challenge-0.0.1.jar
`

##### Rest swagger docs:
[http://localhost:9090/challenge/swagger-ui.html](http://localhost:9090/challenge/swagger-ui.html)



#### How Play:
1. Client Browser or curl using REST API `/game/start` to start and create a new game and will become a player A.
```
Automatic Game and random input number:
- http://localhost:9090/challenge/game/start
- http://localhost:9090/challenge/game/start?gameType=AUTOMATIC

Manual Game and specific input number:
- http://localhost:9090/challenge/game/start?gameType=MANUAL&inputNumber=53

Manual Game and random input number:
- http://localhost:9090/challenge/game/start?gameType=MANUAL
```

2. A PlayerB start choosing the next move, and then PlayerA using the link with the moves.

    2. Play a move:
        `MINUS	= -1`
        `ZERO	= 0` 
        `PLUS	= 1`

- `http://localhost:9090/challenge/play/turn/{gameId}/{player}?action=MINUS`
- `http://localhost:9090/challenge/play/turn/{gameId}/{player}?action=ZERO`
- `http://localhost:9090/challenge/play/turn/{gameId}/{player}?action=PLUS`


3- To get final status of the game, use `game/status/{gameId}` endpoint.

- http://localhost:9090/challenge/game/status/{gameId}
```json
{
  "game": {
    "gameId": "XGFzqzHdge",
    "gameType": "MANUAL",
    "originalInput": 56,
    "winnerPlayer": "PLAYERA",
    "playTurn": {
      "inputNumber": 56,
      "player": "PLAYERB"
    },
    "activeGame": true,
    "playsOfTheGame": [
      previous plays
    ]
  },
  "gameStatus": "http://localhost:9090/challenge/game/status/XGFzqzHdge",
  "turnsPlayer": [
    "http://localhost:9090/challenge/play/turn/XGFzqzHdge/PLAYERA?action=MINUS",
    "http://localhost:9090/challenge/play/turn/XGFzqzHdge/PLAYERA?action=ZERO",
    "http://localhost:9090/challenge/play/turn/XGFzqzHdge/PLAYERA?action=PLUS",
    "-----",
    "http://localhost:9090/challenge/play/turn/XGFzqzHdge/PLAYERB?action=MINUS",
    "http://localhost:9090/challenge/play/turn/XGFzqzHdge/PLAYERB?action=ZERO",
    "http://localhost:9090/challenge/play/turn/XGFzqzHdge/PLAYERB?action=PLUS"
  ]
}
```
- If the fields `"activeGame": false` and  `"winnerPlayer": {ANYPLAYER}` the game got a winner.
- If the fields `"activeGame": false` and  `"winnerPlayer": "DEFAULT"` the game was cancel due to a max of moves.



##### Example of the demo game:

```json
{
  "game": {
    "gameId": "tbLhorsumc",
    "gameType": "MANUAL",
    "originalInput": 56,
    "winnerPlayer": "PLAYERA",
    "playTurn": {
      "inputNumber": 1,
      "player": "PLAYERB"
    },
    "activeGame": false,
    "playsOfTheGame": [
      {
        "inputNumber": 56,
        "player": "PLAYERA"
      },
      {
        "inputNumber": 56,
        "player": "PLAYERB",
        "action": "PLUS"
      },
      {
        "inputNumber": 19,
        "player": "PLAYERA",
        "action": "MINUS"
      },
      {
        "inputNumber": 6,
        "player": "PLAYERB",
        "action": "ZERO"
      },
      {
        "inputNumber": 2,
        "player": "PLAYERA",
        "action": "PLUS"
      },
      {
        "inputNumber": 1,
        "player": "PLAYERA",
        "action": "PLUS"
      }
    ]
  },
  "gameStatus": "http://localhost:9090/challenge/game/status/tbLhorsumc",
  "turnsPlayer": [
    "http://localhost:9090/challenge/play/turn/tbLhorsumc/PLAYERA?action=MINUS",
    "http://localhost:9090/challenge/play/turn/tbLhorsumc/PLAYERA?action=ZERO",
    "http://localhost:9090/challenge/play/turn/tbLhorsumc/PLAYERA?action=PLUS",
    "-----",
    "http://localhost:9090/challenge/play/turn/tbLhorsumc/PLAYERB?action=MINUS",
    "http://localhost:9090/challenge/play/turn/tbLhorsumc/PLAYERB?action=ZERO",
    "http://localhost:9090/challenge/play/turn/tbLhorsumc/PLAYERB?action=PLUS"
  ]
}
```

### Known issues
- One unit test its randomly failing, I haven't time to fix it.
- There is no validation in the if the player plays more than once consecutively.   



