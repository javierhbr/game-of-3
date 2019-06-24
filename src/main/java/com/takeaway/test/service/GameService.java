package com.takeaway.test.service;

import com.hazelcast.core.HazelcastInstance;
import com.takeaway.test.domain.*;
import com.takeaway.test.exceptions.GameNotFoundExceptions;
import com.takeaway.test.exceptions.NoActiveGameException;
import com.takeaway.test.message.PlayMessage;
import com.takeaway.test.message.PlaySender;
import com.takeaway.test.message.SenderPlayA;
import com.takeaway.test.message.SenderPlayB;
import com.takeaway.test.rest.response.GameResponse;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GameService {

    private Logger logger = LoggerFactory.getLogger(GameService.class);

    private HazelcastInstance hazelcastInstance;

    private SenderPlayB senderPlayB;

    private SenderPlayA senderPlayA;

    @Value("${game.hazelcast.map.name}")
    private String hazelcastGameMapName;

    @Value("${game.random.range.min}")
    private int randomRangeMin;

    @Value("${game.random.range.max}")
    private int randomRangeMax;

    @Value("${game.automatic.max.play}")
    private int maxAutomaticPlays;

    public GameService(HazelcastInstance hazelcastInstance, SenderPlayB senderPlayB, SenderPlayA senderPlayA) {
        this.hazelcastInstance = hazelcastInstance;
        this.senderPlayB = senderPlayB;
        this.senderPlayA = senderPlayA;
    }

    public GameResponse createNewGame(Optional<Integer> inputNumber, Optional<GameType> gameType){

        Game game = Game.createNewGame(inputNumber.orElse(RandomUtils.nextInt(randomRangeMin, randomRangeMax))
                    , gameType.orElse(GameType.AUTOMATIC));
        GameResponse gameResponse = GameResponse.builder()
                .game(game)
                .gameStatus(createGameStatusURL(game.getGameId()))
                .turnsPlayer(creteActionsList(game.getGameId(), Player.PLAYERB))
                .build();
        addGameToGlobalList(game);
        if(GameType.AUTOMATIC.equals(game.getGameType())){
            senderPlayB.sendGameToPlayer(PlayMessage
                    .builder()
                    .gameId(game.getGameId())
                    .gameType(game.getGameType())
                    .playTurn(game.getPlayTurn())
                    .build());
        }
        return gameResponse;
    }


    public GameResponse getGameData(Optional<String> gameId) throws GameNotFoundExceptions{

        Game game = getGameFromGlobalList(gameId.orElseThrow(GameNotFoundExceptions :: new));

        List<String> turnsPlayerA = creteActionsList(game.getGameId(), Player.PLAYERA);
        turnsPlayerA.add("-----");
        List<String> turnsPlayerB = creteActionsList(game.getGameId(), Player.PLAYERB);

        return GameResponse.builder()
                .game(game)
                .gameStatus(createGameStatusURL(game.getGameId()))
                .turnsPlayer(Stream.concat(turnsPlayerA.stream(), turnsPlayerB.stream()).collect(Collectors.toList()))
                .build();
    }



    public void playIncomingMove(PlayMessage incomingMove) throws GameNotFoundExceptions, NoActiveGameException {
            Game game = getGameFromGlobalList(incomingMove.getGameId());
            game.playMove(incomingMove.getPlayTurn());
            addGameToGlobalList(game);

            if (game.isActiveGame() && game.getGameType().equals(GameType.AUTOMATIC)) {
                if(game.getPlaysOfTheGame().size() <= maxAutomaticPlays){
                    PlaySender playSender = getPlaySenderForAutomaticMove(incomingMove.getPlayTurn().getPlayer());
                    Player nextPlayerTurn = getNextPlayerTurn(incomingMove.getPlayTurn().getPlayer());

                    playSender.sendGameToPlayer(PlayMessage
                            .builder()
                            .gameId(game.getGameId())
                            .gameType(game.getGameType())
                            .playTurn(PlayTurn.builder()
                                    .inputNumber(game.getPlayTurn().getInputNumber())
                                    .player(nextPlayerTurn)
                                    .build())
                            .build());
                }else{
                    logger.info("Automatic game is cancel due max number of plays - gameId[{}] Number of plays [{}]"
                            , game.getGameId(), game.getPlaysOfTheGame().size());
                    game.cancelGame();
                    addGameToGlobalList(game);
                }
            }

        logger.info("processing move for game: {}", game);
    }

    private PlaySender getPlaySenderForAutomaticMove(Player player){
        PlaySender playerSelected = senderPlayA;
        if (player.equals(Player.PLAYERA)){
            playerSelected = senderPlayB;
        }
        return playerSelected;
    }

    private Player getNextPlayerTurn(Player player){
        Player nextPlay = Player.PLAYERA;
        if (player.equals(Player.PLAYERA)){
            nextPlay = Player.PLAYERB;
        }
        return nextPlay;
    }

    protected Game getGameFromGlobalList(final String gameId) throws GameNotFoundExceptions {
        Map<String, Game> allGames = hazelcastInstance.getMap(hazelcastGameMapName);
        return Optional.ofNullable(allGames.get(gameId)).orElseThrow(GameNotFoundExceptions::new);
    }

    protected void addGameToGlobalList(Game game) {
        Map<String, Game> allGames = hazelcastInstance.getMap(hazelcastGameMapName);
        allGames.put(game.getGameId(), game);
    }

    public String createURL() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String fullURL = request.getRequestURL().toString();
        return fullURL.substring(0, StringUtils.ordinalIndexOf(fullURL, "/", 4));
    }

    public String createGameStatusURL(String gameId) {
        return String.format("%s/game/status/%s", createURL(), gameId);
    }

    protected List<String> creteActionsList(String gameId, Player playerTurn){
        String url = createURL();
        return Action.stream()
                .map(a -> String.format("%s/play/turn/%s/%s?action=%s", url, gameId, playerTurn, a.toString()))
                .collect(Collectors.toList());
    }

}
