package com.takeaway.test.domain;

import com.takeaway.test.exceptions.InvalidTurnException;
import com.takeaway.test.exceptions.NoActiveGameException;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Builder(access = AccessLevel.PRIVATE)
@ToString
public class Game implements Serializable {

    @Getter
    private String gameId;

    @Getter
    private GameType gameType;

    @Getter
    private Integer originalInput;

    @Getter
    private Player winnerPlayer;

    @Getter
    private PlayTurn playTurn;

    @Getter
    private boolean activeGame;

    @Getter
    private List<PlayTurn> playsOfTheGame;

    private final int winnerNumber = 1;

    private final int divisionGameRule = 3;

    public static Game createNewGame(Integer inputNumber, GameType gameType){

        Game newGame = Game.builder()
                .gameId(RandomStringUtils.randomAlphabetic(10))
                .originalInput(inputNumber)
                .gameType(gameType)
                .playsOfTheGame( new ArrayList<>())
                .playTurn(PlayTurn.builder()
                        .inputNumber(inputNumber)
                        .player(Player.PLAYERB)
                        .build())
                .activeGame(true)
                .build();

        newGame.getPlaysOfTheGame().add(PlayTurn.builder()
                .inputNumber(inputNumber)
                .player(Player.PLAYERA)
                .build());
        return newGame;
    }

    public void playMove(PlayTurn turn) throws NoActiveGameException {

        if(!this.isActiveGame()){
            throw new NoActiveGameException();
        }

        isDuplicateTurnOfPlayer(turn);

        if (this.getGameType().equals(GameType.AUTOMATIC)){
            turn.setAction(Optional.of(Action.getRandomAction()).orElse(Action.ZERO));
        }

        this.playsOfTheGame.add(turn);
        Integer preparePlay = this.playTurn.getInputNumber() + turn.getAction().getValue();

        Integer resultOfPlay = preparePlay / divisionGameRule ;
        Integer resultOfPlayModule = preparePlay % divisionGameRule;
        if (resultOfPlay == winnerNumber && resultOfPlayModule == 0){
            this.winnerPlayer= turn.getPlayer();
            this.activeGame= false;
            this.playsOfTheGame.add(PlayTurn.builder()
                    .inputNumber(resultOfPlay)
                    .player(turn.getPlayer())
                    .action(turn.getAction())
                    .build());
        }

        this.playTurn.setInputNumber(resultOfPlay);
    }

    public void cancelGame(){
        this.winnerPlayer= Player.DEFAULT;
        this.activeGame= false;
        this.playTurn.setInputNumber(-1);
    }

    private void isDuplicateTurnOfPlayer(PlayTurn turn){
        PlayTurn lastTurn = this.getPlaysOfTheGame().get(this.getPlaysOfTheGame().size()-1);
        if(lastTurn.getPlayer().equals(turn.getPlayer())){
            throw new InvalidTurnException();
        }

    }
}
