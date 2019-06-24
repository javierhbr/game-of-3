package com.takeaway.test.service;

import com.takeaway.test.domain.Action;
import com.takeaway.test.domain.Game;
import com.takeaway.test.domain.PlayTurn;
import com.takeaway.test.domain.Player;
import com.takeaway.test.exceptions.GameNotFoundExceptions;
import com.takeaway.test.message.*;
import com.takeaway.test.rest.response.GameResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayService {

    private GameService gameService;

    private SenderPlayB senderPlayB;

    private SenderPlayA senderPlayA;

    public PlayService(GameService gameService, SenderPlayB senderPlayB, SenderPlayA senderPlayA) {
        this.gameService = gameService;
        this.senderPlayB = senderPlayB;
        this.senderPlayA = senderPlayA;
    }

    public GameResponse sendPlay(String gameId, Player playerTurn, Action action) throws GameNotFoundExceptions {

        PlaySender playSender = senderPlayA;
        Game game = gameService.getGameFromGlobalList(gameId);

        PlayTurn turn = PlayTurn.builder()
                .action(Optional.ofNullable(action).orElse(Action.ZERO))
                .inputNumber(game.getPlayTurn().getInputNumber())
                .player(playerTurn)
                .build();
        PlayMessage playMessage = PlayMessage
                                    .builder()
                                    .gameId(game.getGameId())
                                    .gameType(game.getGameType())
                                    .playTurn(turn)
                                    .build();

        Player nextPlayer = Player.PLAYERA;

        if(Player.PLAYERA.equals(playerTurn)){
            playSender = senderPlayB;
            nextPlayer = Player.PLAYERB;
        }

        playSender.sendGameToPlayer(playMessage);
        return GameResponse.builder()
                .game(game)
                .turnsPlayer(gameService.creteActionsList(game.getGameId(), nextPlayer))
                .build();
    }
}
