package com.takeaway.test.domain;

import com.takeaway.test.exceptions.InvalidTurnException;
import com.takeaway.test.exceptions.NoActiveGameException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Action.class})
public class GameTest {

    private Optional<Integer> inputNumber;
    private Optional<GameType> gameType;
    private Integer orElseInputNumber;
    private final int divisionGameRule = 3;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp(){
        this.inputNumber = Optional.ofNullable(53);
        this.gameType = Optional.ofNullable(GameType.MANUAL);
        this.orElseInputNumber = 0;
    }

    @Test
    public void whenCreateNewGameWithValues_thenReturnGameObject() {
        Game game = Game.createNewGame(this.inputNumber.orElse(this.orElseInputNumber)
                , this.gameType.orElse(GameType.AUTOMATIC));

        assertThat(game, is(notNullValue()));
        assertThat(game.getGameId(), is(notNullValue()));
        assertThat(game.getGameType(), is(this.gameType.get()));
        assertThat(game.getPlaysOfTheGame().size(), is(1));
        assertThat(game.isActiveGame(), is(true));
        assertThat(game.getPlayTurn().getInputNumber(), is(this.inputNumber.get()));

    }



    @Test
    public void whenCreateNewGameWithNullInputNumber_thenReturnGameObject(){
        Optional<Integer> testInputNumber = Optional.empty();
        Game game = Game.createNewGame(testInputNumber.orElse(this.orElseInputNumber)
                , this.gameType.orElse(GameType.AUTOMATIC));

        assertThat(game, is(notNullValue()));
        assertThat(game.getGameId(), is(notNullValue()));
        assertThat(game.getGameType(), is(this.gameType.get()));
        assertThat(game.getPlaysOfTheGame().size(), is(1));
        assertThat(game.getPlayTurn().getInputNumber(), is(this.orElseInputNumber));
        assertThat(game.getPlaysOfTheGame().size(), is(1));

    }

    @Test
    public void whenCreateNewGameWithNullInputNumberAndGameType_thenReturnGameObject(){

        Optional<Integer> testInputNumber = Optional.empty();
        Optional<GameType> gameType = Optional.empty();
        Game game = Game.createNewGame(testInputNumber.orElse(this.orElseInputNumber)
                , gameType.orElse(GameType.AUTOMATIC));

        assertThat(game, is(notNullValue()));
        assertThat(game.getGameId(), is(notNullValue()));
        assertThat(game.getGameType(), is(GameType.AUTOMATIC));
        assertThat(game.getPlayTurn().getInputNumber(), is(this.orElseInputNumber));
        assertThat(game.getPlaysOfTheGame().size(), is(1));
        assertThat(game.isActiveGame(), is(true));
    }


    @Test
    public void whenPlayAndGameIsInActive_thenThrowsNoActiveGameException () throws NoActiveGameException {
        expectedException.expect(NoActiveGameException.class);
        Game game = Game.createNewGame(this.inputNumber.orElse(this.orElseInputNumber)
                , this.gameType.orElse(GameType.AUTOMATIC));

        game.cancelGame();
        PlayTurn turn = PlayTurn.builder()
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .action(Optional.of(Action.ZERO).get())
                .build();

        game.playMove(turn);
    }


    @Test
    public void whenPlayMoveActionZeroWithManualGame_thenCompletedWithOutWinner() throws NoActiveGameException {
        Game game = Game.createNewGame(this.inputNumber.orElse(this.orElseInputNumber)
                , this.gameType.orElse(GameType.AUTOMATIC));

        PlayTurn turn = PlayTurn.builder()
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .action(Optional.of(Action.ZERO).get())
                .build();

        game.playMove(turn);

        assertThat(game.getPlayTurn(), is(notNullValue()));
        assertThat(game.getPlaysOfTheGame(), is(notNullValue()));
        assertThat(game.getPlaysOfTheGame().size(), is(2));
        assertThat(game.getPlayTurn().getInputNumber(), is(calculateDivisionRule(this.inputNumber)));
        assertThat(game.isActiveGame(), is(true));
    }



    @Test
    public void whenPlayMoveAutomaticActionRandomZero_thenCompletedWithOutWinner() throws NoActiveGameException {

        mockStatic(Action.class);
        when(Action.getRandomAction()).thenReturn(Action.ZERO);

        Game game = Game.createNewGame(this.inputNumber.orElse(this.orElseInputNumber)
                , Optional.of(GameType.AUTOMATIC).orElse(GameType.MANUAL));

        PlayTurn turn = PlayTurn.builder()
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .build();
        game.playMove(turn);
        assertThat(game.getPlayTurn(), is(notNullValue()));
        assertThat(game.getPlaysOfTheGame(), is(notNullValue()));
        assertThat(game.getPlaysOfTheGame().size(), is(2));
        assertThat(game.getPlaysOfTheGame().contains(turn), is(true));
        assertThat(game.getPlayTurn().getInputNumber(), is(calculateDivisionRule(this.inputNumber)));
        assertThat(game.isActiveGame(), is(true));
    }

    @Test
    public void whenPlayMoveAutomaticActionRandomPlus_thenCompletedWithOutWinner() throws NoActiveGameException {

        mockStatic(Action.class);
        Action plusAction = Action.PLUS;
        Integer inputNumberAfterMove = inputNumber.get() + plusAction.getValue();
        when(Action.getRandomAction()).thenReturn(plusAction);

        Game game = Game.createNewGame(this.inputNumber.orElse(this.orElseInputNumber)
                , Optional.of(GameType.AUTOMATIC).orElse(GameType.MANUAL));

        PlayTurn turn = PlayTurn.builder()
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .build();
        game.playMove(turn);

        assertThat(game.getPlayTurn(), is(notNullValue()));
        assertThat(game.getPlaysOfTheGame(), is(notNullValue()));
        assertThat(game.getPlaysOfTheGame().size(), is(2));
        assertThat(game.getPlaysOfTheGame().contains(turn), is(true));
        assertThat(game.getPlayTurn().getInputNumber(), is(inputNumberAfterMove / this.divisionGameRule));
        assertThat(game.isActiveGame(), is(true));
    }

    @Test
    public void whenPlayMoveAutomaticActionRandomZeroMinus_thenCompletedWithOutWinner() throws NoActiveGameException {

        mockStatic(Action.class);
        Action plusAction = Action.MINUS;
        Integer inputNumberAfterMove = this.inputNumber.get() + plusAction.getValue();
        when(Action.getRandomAction()).thenReturn(plusAction);

        Game game = Game.createNewGame(this.inputNumber.orElse(this.orElseInputNumber)
                , Optional.of(GameType.AUTOMATIC).orElse(GameType.MANUAL));

        PlayTurn turn = PlayTurn.builder()
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(Player.PLAYERB)
                .build();
        game.playMove(turn);

        assertThat(game.getPlayTurn(), is(notNullValue()));
        assertThat(game.getPlaysOfTheGame(), is(notNullValue()));
        assertThat(game.getPlaysOfTheGame().size(), is(2));
        assertThat(game.getPlaysOfTheGame().contains(turn), is(true));
        assertThat(game.getPlayTurn().getInputNumber(), is(inputNumberAfterMove / this.divisionGameRule));
        assertThat(game.isActiveGame(), is(true));
    }

    @Test
    public void whenPlayMoveAutomaticActionRandomZero_thenCompletedWithWinner() throws NoActiveGameException {

        Optional<Integer> inputNumberWinner = Optional.of(3);
        Player winnerPlayer = Player.PLAYERB;
        mockStatic(Action.class);

        when(Action.getRandomAction()).thenReturn(Action.ZERO);

        Game game = Game.createNewGame(inputNumberWinner.orElse(this.orElseInputNumber)
                , Optional.of(GameType.AUTOMATIC).orElse(GameType.MANUAL));

        PlayTurn turn = PlayTurn.builder()
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(winnerPlayer)
                .build();
        game.playMove(turn);
        assertThat(game.getPlayTurn(), is(notNullValue()));
        assertThat(game.getPlaysOfTheGame(), is(notNullValue()));
        assertThat(game.getPlaysOfTheGame().size(), is(3));
        assertThat(game.getPlaysOfTheGame().contains(turn), is(true));
        assertThat(game.getPlayTurn().getInputNumber(), is(calculateDivisionRule(inputNumberWinner)));
        assertThat(game.isActiveGame(), is(false));
        assertThat(game.getWinnerPlayer(), is(winnerPlayer));
    }


    @Test
    public void whenPlayMoveSamePlayerConsecutively_thenThrowsInvalidTurnException() throws NoActiveGameException {
        expectedException.expect(NoActiveGameException.class);
        Optional<Integer> inputNumberWinner = Optional.of(3);
        Player winnerPlayer = Player.PLAYERB;

        Game game = Game.createNewGame(inputNumberWinner.orElse(this.orElseInputNumber)
                , Optional.of(GameType.MANUAL).orElse(GameType.MANUAL));

        PlayTurn turn = PlayTurn.builder()
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(winnerPlayer)
                .action(Optional.of(Action.ZERO).get())
                .build();

        game.playMove(turn);
        game.playMove(turn);

    }
    @Test
    public void whenPlayMoveManualActionZero_thenCompletedWithWinner() throws NoActiveGameException{

        Optional<Integer> inputNumberWinner = Optional.of(3);
        Player winnerPlayer = Player.PLAYERB;

        Game game = Game.createNewGame(inputNumberWinner.orElse(this.orElseInputNumber)
                , Optional.of(GameType.MANUAL).orElse(GameType.MANUAL));

        PlayTurn turn = PlayTurn.builder()
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(winnerPlayer)
                .action(Optional.of(Action.ZERO).get())
                .build();
        game.playMove(turn);
        assertThat(game.getPlayTurn(), is(notNullValue()));
        assertThat(game.getPlaysOfTheGame(), is(notNullValue()));
        assertThat(game.getPlaysOfTheGame().size(), is(3));
        assertThat(game.getPlaysOfTheGame().contains(turn), is(true));
        assertThat(game.getPlayTurn().getInputNumber(), is(calculateDivisionRule(inputNumberWinner)));
        assertThat(game.isActiveGame(), is(false));
        assertThat(game.getWinnerPlayer(), is(winnerPlayer));
    }

    @Test
    public void whenCancelGame_thenChangeGameToInActive() {
        Game game = Game.createNewGame(this.inputNumber.orElse(this.orElseInputNumber)
                , this.gameType.orElse(GameType.AUTOMATIC));
        game.cancelGame();
        assertThat(game, is(notNullValue()));
        assertThat(game.getGameId(), is(notNullValue()));
        assertThat(game.getGameType(), is(this.gameType.get()));
        assertThat(game.getPlaysOfTheGame().isEmpty(), is(false));
        assertThat(game.isActiveGame(), is(false));
        assertThat(game.getWinnerPlayer(), is(Player.DEFAULT));
        assertThat(game.getPlayTurn().getInputNumber(), is(-1));

    }

    private Integer calculateDivisionRule(Optional<Integer> inputNumber) {
        return inputNumber.get() / this.divisionGameRule;
    }
}