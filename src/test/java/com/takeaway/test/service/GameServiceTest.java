package com.takeaway.test.service;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import com.takeaway.test.TakeawayChallengeApplication;
import com.takeaway.test.domain.*;
import com.takeaway.test.exceptions.GameNotFoundExceptions;
import com.takeaway.test.exceptions.NoActiveGameException;
import com.takeaway.test.message.PlayMessage;
import com.takeaway.test.message.SenderPlayA;
import com.takeaway.test.message.SenderPlayB;
import com.takeaway.test.rest.response.GameResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:application.properties")
@ContextConfiguration(classes={TakeawayChallengeApplication.class})
public class GameServiceTest {

    @SpyBean
    private GameService gameService;

    @MockBean
    private SenderPlayA senderPlayA;

    @MockBean
    private SenderPlayB senderPlayB;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Optional<Integer> inputNumber;
    private Optional<GameType> gameType;
    private Integer orElseInputNumber;
    private String mockUrl;

    @Before
    public void setUp(){
        this.inputNumber = Optional.of(53);
        this.gameType = Optional.of(GameType.MANUAL);
        this.orElseInputNumber = 0;
        this.mockUrl = "http://localhost:8080/gameServiceTest";

        doNothing().when(senderPlayB).sendGameToPlayer(any());
        doNothing().when(senderPlayA).sendGameToPlayer(any());
        doReturn(this.mockUrl).when(gameService).createURL();

    }
    @Bean
    public HazelcastInstance getHazelcastInstance(){
        return Hazelcast.newHazelcastInstance();
    }

    @Bean
    public Config hazelCastConfig(){
        Config config = new Config();
        config.setInstanceName("hazelcast-instance")
                .addMapConfig(
                        new MapConfig()
                                .setName("gameOfThree")
                                .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                                .setEvictionPolicy(EvictionPolicy.LRU)
                                .setTimeToLiveSeconds(-1));
        return config;
    }

    @Test
    public void whenCreateNewGameManualTypeWithNullInputNumber_thenReturnGameObject() {
        doNothing().when(gameService).addGameToGlobalList(any());
        GameResponse gameResponse = gameService.createNewGame(Optional.empty(), this.gameType);

        assertNewGameManual(gameResponse, GameType.MANUAL);
        assertThat(gameResponse.getGame().getPlayTurn().getInputNumber(), is(greaterThanOrEqualTo(0)));
        assertThat(gameResponse.getGame().getOriginalInput(), is(greaterThanOrEqualTo(0)));
        verify(senderPlayB, times(0)).sendGameToPlayer(any());
    }
    @Test
    public void whenCreateNewGameWithNullInputNumberAndGameType_thenReturnAutomaticGameGameObjectAndSentToPlayerB() {
        doNothing().when(gameService).addGameToGlobalList(any());
        GameResponse gameResponse = gameService.createNewGame(Optional.empty(), Optional.empty());

        assertThat(gameResponse, is(notNullValue()));
        assertThat(gameResponse.getGame(), is(notNullValue()));
        assertThat(gameResponse.getGame().getGameId(), is(notNullValue()));
        assertThat(gameResponse.getGame().isActiveGame(), is(true));
        assertThat(gameResponse.getGame().getOriginalInput(), is(greaterThanOrEqualTo(0)));
        assertThat(gameResponse.getGame().getGameType(), is(GameType.AUTOMATIC));
        assertThat(gameResponse.getGame().getPlaysOfTheGame().size(), is(1));
        assertThat(gameResponse.getGame().getWinnerPlayer(), is(nullValue()));
        assertThat(gameResponse.getGame().getPlayTurn().getInputNumber(), is(greaterThanOrEqualTo(0)));
        assertThat(gameResponse.getGame().getPlayTurn().getPlayer(), is(Player.PLAYERB));
        assertThat(gameResponse.getGame().getPlayTurn().getAction(), is(nullValue()));
        assertThat(gameResponse.getTurnsPlayer().size(), is(3));
        assertThat(gameResponse.getTurnsPlayer().get(0), is(containsString(this.mockUrl)));
        verify(senderPlayB, times(1)).sendGameToPlayer(any());
    }

    @Test
    public void whenCreateNewGameManualTypeWithParameter_thenReturnGameObject() {
        doNothing().when(gameService).addGameToGlobalList(any());
        GameResponse gameResponse = gameService.createNewGame(this.inputNumber, this.gameType);

        assertNewGameManual(gameResponse, GameType.MANUAL);
        assertThat(gameResponse.getGame().getOriginalInput(), is(this.inputNumber.get()));
        assertThat(gameResponse.getGame().getPlayTurn().getInputNumber(), is(this.inputNumber.get()));
        verify(senderPlayB, times(0)).sendGameToPlayer(any());
    }

    @Test
    public void whenPlayFirstMoveOfPlayerBAutomatic_thenThrowsGameNotFoundExceptions() throws GameNotFoundExceptions, NoActiveGameException {
        expectedException.expect(GameNotFoundExceptions.class);
        Game game = Game.createNewGame(this.inputNumber.get(), GameType.AUTOMATIC);

        PlayTurn turnPlayerB = PlayTurn.builder()
                .action(Action.PLUS)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .build();

        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerB)
                .build());

        verify(gameService, times(1)).getGameFromGlobalList(anyString());
    }

    @Test
    public void whenPlayFirstMoveOfPlayerBManual_thenThrowsGameNotFoundExceptions() throws GameNotFoundExceptions, NoActiveGameException {
        expectedException.expect(GameNotFoundExceptions.class);
        Game game = Game.createNewGame(this.inputNumber.get(), this.gameType.get());

        PlayTurn turnPlayerB = PlayTurn.builder()
                .action(Action.PLUS)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .build();

        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerB)
                .build());

        verify(gameService, times(1)).getGameFromGlobalList(anyString());
    }

    @Test
    public void whenPlayerBPlayMoveOnAutomatic_thenSendTurnToPlayerA() throws GameNotFoundExceptions, NoActiveGameException {

        GameResponse gameResponse = gameService.createNewGame(Optional.ofNullable(5), Optional.ofNullable(GameType.AUTOMATIC));
        Game game = gameResponse.getGame();
        PlayTurn turnPlayerB = PlayTurn.builder()
                .action(Action.ZERO)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .build();

        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerB)
                .build());

        Game gameAfterMove = gameService.getGameFromGlobalList(game.getGameId());

        assertThat(gameAfterMove.getWinnerPlayer(), is(nullValue()));
        assertThat(gameAfterMove.isActiveGame(), is(true));
        assertThat(gameAfterMove.getPlaysOfTheGame().size(), is(2));
        verify(senderPlayA, times(1)).sendGameToPlayer(any(PlayMessage.class));
        verify(gameService, times(2)).getGameFromGlobalList(anyString());
        verify(gameService, times(2)).addGameToGlobalList(any(Game.class));

    }


    @Test
    public void whenPlayFirstMoveOfPlayerBManual_thenPlayerBWin() throws GameNotFoundExceptions, NoActiveGameException {

        GameResponse gameResponse = gameService.createNewGame(Optional.ofNullable(3), this.gameType);
        Game game = gameResponse.getGame();

        PlayTurn turnPlayerB = PlayTurn.builder()
                .action(Action.ZERO)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .build();

        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerB)
                .build());

        Game gameAfterMove = gameService.getGameFromGlobalList(game.getGameId());

        assertThat(gameAfterMove.getWinnerPlayer(), is(Player.PLAYERB));
        assertThat(gameAfterMove.isActiveGame(), is(false));
        assertThat(gameAfterMove.getPlaysOfTheGame().size(), is(3));
        verify(senderPlayB, times(0)).sendGameToPlayer(any(PlayMessage.class));
        verify(gameService, times(2)).getGameFromGlobalList(anyString());
        verify(gameService, times(2)).addGameToGlobalList(any(Game.class));

    }


    @Test
    public void whenPlaySecondMoveOfPlayerBManual_thenPlayerAWin() throws GameNotFoundExceptions, NoActiveGameException {

        GameResponse gameResponse = gameService.createNewGame(Optional.ofNullable(5), this.gameType);
        Game game = gameResponse.getGame();

        PlayTurn turnPlayerB = PlayTurn.builder()
                .action(Action.PLUS)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .build();

        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerB)
                .build());

        PlayTurn turnPlayerA = PlayTurn.builder()
                .action(Action.PLUS)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERA)
                .build();

        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerA)
                .build());

        Game gameAfterMove = gameService.getGameFromGlobalList(game.getGameId());

        assertThat(gameAfterMove.getWinnerPlayer(), is(Player.PLAYERA));
        assertThat(gameAfterMove.isActiveGame(), is(false));
        assertThat(gameAfterMove.getPlaysOfTheGame().size(), is(4));
        verify(gameService, times(3)).getGameFromGlobalList(anyString());
        verify(gameService, times(3)).addGameToGlobalList(any(Game.class));

    }


    @Test
    public void whenPlayMoveOfPlayerBManual_thenPerformancePlayWithOutWinner() throws GameNotFoundExceptions, NoActiveGameException {

        GameResponse gameResponse = gameService.createNewGame(Optional.ofNullable(3), this.gameType);
        Game game = gameResponse.getGame();

        verify(gameService, times(1)).addGameToGlobalList(any(Game.class));

        PlayTurn turnPlayerB = PlayTurn.builder()
                .action(Action.PLUS)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .build();

        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerB)
                .build());

        Game gameAfterMove = gameService.getGameFromGlobalList(game.getGameId());

        assertThat(gameAfterMove.getWinnerPlayer(), is(nullValue()));
        assertThat(gameAfterMove.isActiveGame(), is(true));
        assertThat(gameAfterMove.getPlaysOfTheGame().size(), is(2));
        verify(senderPlayB, times(0)).sendGameToPlayer(any(PlayMessage.class));
        verify(gameService, times(2)).getGameFromGlobalList(anyString());
        verify(gameService, times(2)).addGameToGlobalList(any(Game.class));
    }

    @Test
    public void whenPlayMoveOfPlayerAManual_thenPerformancePlayWithOutWinner() throws GameNotFoundExceptions, NoActiveGameException {
        GameResponse gameResponse = gameService.createNewGame(Optional.ofNullable(3), this.gameType);
        Game game = gameResponse.getGame();

        verify(gameService, times(1)).addGameToGlobalList(any(Game.class));

        PlayTurn turnPlayerB = PlayTurn.builder()
                .action(Action.PLUS)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .build();

        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerB)
                .build());

        PlayTurn turnPlayerA = PlayTurn.builder()
                .action(Action.PLUS)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERA)
                .build();

        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerA)
                .build());

        Game gameAfterMove = gameService.getGameFromGlobalList(game.getGameId());

        assertThat(gameAfterMove.getWinnerPlayer(), is(nullValue()));
        assertThat(gameAfterMove.isActiveGame(), is(true));
        assertThat(gameAfterMove.getPlaysOfTheGame().size(), is(3));
        verify(senderPlayB, times(0)).sendGameToPlayer(any(PlayMessage.class));
        verify(gameService, times(3)).getGameFromGlobalList(anyString());
        verify(gameService, times(3)).addGameToGlobalList(any(Game.class));
    }



    @Test
    public void whenPlayAutomaticUntilMaxMoves_thenCancelGame() throws GameNotFoundExceptions, NoActiveGameException {
        GameResponse gameResponse = gameService.createNewGame(Optional.ofNullable(3), Optional.of(GameType.AUTOMATIC));
        Game game = gameResponse.getGame();

        PlayTurn turnPlayerB = PlayTurn.builder()
                .action(Action.PLUS)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .build();
        PlayTurn turnPlayerA = PlayTurn.builder()
                .action(Action.PLUS)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERA)
                .build();
        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerB)
                .build());

        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerA)
                .build());
        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerB)
                .build());

        Game gameAfterMove = gameService.getGameFromGlobalList(game.getGameId());

        assertThat(gameAfterMove.getWinnerPlayer(), is(Player.DEFAULT));
        assertThat(gameAfterMove.isActiveGame(), is(false));
        assertThat(gameAfterMove.getPlayTurn().getInputNumber(), is(-1));
        verify(gameService, times(4)).getGameFromGlobalList(anyString());
        verify(gameService, times(5)).addGameToGlobalList(any(Game.class));
    }


    @Test
    public void whenPlayInactiveGame_thenThrowsNoActiveGameException () throws GameNotFoundExceptions, NoActiveGameException {
        expectedException.expect(NoActiveGameException.class);
        GameResponse gameResponse = gameService.createNewGame(Optional.ofNullable(3), Optional.of(GameType.AUTOMATIC));
        Game game = gameResponse.getGame();

        PlayTurn turnPlayerB = PlayTurn.builder()
                .action(Action.PLUS)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .build();
        PlayTurn turnPlayerA = PlayTurn.builder()
                .action(Action.PLUS)
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERA)
                .build();
        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerB)
                .build());

        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerA)
                .build());
        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerB)
                .build());
        gameService.playIncomingMove(PlayMessage
                .builder()
                .gameId(game.getGameId())
                .gameType(game.getGameType())
                .playTurn(turnPlayerA)
                .build());

        verify(gameService, times(5)).getGameFromGlobalList(anyString());
        verify(gameService, times(6)).addGameToGlobalList(any(Game.class));
    }

    @Test
    public void whenGetGameDataWithValidGameID_thenReturnGameData() throws GameNotFoundExceptions {
        GameResponse gameResponse = gameService.createNewGame(Optional.empty(), this.gameType);
        GameResponse gameDataResponse= gameService.getGameData(Optional.ofNullable(gameResponse.getGame().getGameId()));

        assertThat(gameResponse, is(notNullValue()));
        assertThat(gameDataResponse, is(notNullValue()));
        assertNewGameManual(gameResponse, this.gameType.get());
        assertThat(gameDataResponse, is(notNullValue()));
        assertThat(gameDataResponse.getGame(), is(notNullValue()));
        assertThat(gameDataResponse.getGame().getOriginalInput(), is(notNullValue()));
        assertThat(gameDataResponse.getGame().getGameId(), is(notNullValue()));
        assertThat(gameDataResponse.getGame().isActiveGame(), is(true));
        assertThat(gameDataResponse.getGame().getGameType(), is(this.gameType.get()));
        assertThat(gameDataResponse.getGame().getPlaysOfTheGame().isEmpty(), is(false));
        assertThat(gameDataResponse.getGame().getWinnerPlayer(), is(nullValue()));
        assertThat(gameDataResponse.getGame().getPlayTurn().getPlayer(), is(Player.PLAYERB));
        assertThat(gameDataResponse.getGame().getPlayTurn().getAction(), is(nullValue()));
        assertThat(gameDataResponse.getTurnsPlayer().size(), is(7));
        assertThat(gameDataResponse.getTurnsPlayer().get(0), is(containsString(this.mockUrl)));
        assertThat(gameResponse.getGame().getGameId(), is(gameDataResponse.getGame().getGameId()));

    }

    @Test
    public void whenGetGameDataInvalidGameID_thenReturnGameData() throws GameNotFoundExceptions {
        expectedException.expect(GameNotFoundExceptions.class);
        doNothing().when(gameService).addGameToGlobalList(any());
        gameService.getGameData(Optional.empty());
        verify(senderPlayB, times(0)).sendGameToPlayer(any());
    }

    @Test
    public void whenGetGameDataWithOutGameID_thenReturnGameData() throws GameNotFoundExceptions {
        expectedException.expect(GameNotFoundExceptions.class);
        doNothing().when(gameService).addGameToGlobalList(any());
        gameService.getGameData(Optional.empty());
        verify(senderPlayB, times(0)).sendGameToPlayer(any());
    }

    private void assertNewGameManual(GameResponse gameResponse, GameType gameType) {
        assertThat(gameResponse, is(notNullValue()));
        assertThat(gameResponse.getGame(), is(notNullValue()));
        assertThat(gameResponse.getGame().getOriginalInput(), is(notNullValue()));
        assertThat(gameResponse.getGame().getGameId(), is(notNullValue()));
        assertThat(gameResponse.getGame().isActiveGame(), is(true));
        assertThat(gameResponse.getGame().getGameType(), is(gameType));
        assertThat(gameResponse.getGame().getPlaysOfTheGame().size(), is(1));
        assertThat(gameResponse.getGame().getWinnerPlayer(), is(nullValue()));
        assertThat(gameResponse.getGame().getPlayTurn().getPlayer(), is(Player.PLAYERB));
        assertThat(gameResponse.getGame().getPlayTurn().getAction(), is(nullValue()));
        assertThat(gameResponse.getTurnsPlayer().size(), is(3));
        assertThat(gameResponse.getTurnsPlayer().get(0), is(containsString(this.mockUrl)));
    }


}