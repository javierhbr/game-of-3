package com.takeaway.test.message;

import com.takeaway.test.exceptions.GameNotFoundExceptions;
import com.takeaway.test.exceptions.InvalidTurnException;
import com.takeaway.test.exceptions.NoActiveGameException;
import com.takeaway.test.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class PlayerBTurnListener {
    private Logger logger = LoggerFactory.getLogger(PlayerBTurnListener.class);

    private GameService gameService;

    public PlayerBTurnListener(GameService gameService) {
        this.gameService = gameService;
    }

    @JmsListener(destination = "${game.messages.queue.player-b.name}"
            , containerFactory = "${game.messages.connection-factory.name}")
    public void playerBTurnListener(PlayMessage play) {
        logger.info(" >>  player B Turn play: {}", play);
        try {
            gameService.playIncomingMove(play);
        } catch (GameNotFoundExceptions | NoActiveGameException | InvalidTurnException ex) {
            logger.error("error to execute playerB move", ex.getClass());
        }
    }


}
