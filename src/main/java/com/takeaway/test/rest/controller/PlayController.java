package com.takeaway.test.rest.controller;

import com.takeaway.test.domain.Action;
import com.takeaway.test.domain.Player;
import com.takeaway.test.exceptions.GameNotFoundExceptions;
import com.takeaway.test.rest.response.GameResponse;
import com.takeaway.test.service.PlayService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/play")
public class PlayController {

    private PlayService playService;

    public PlayController(PlayService playService) {
        this.playService = playService;
    }

    @ApiOperation("this operation sends a message to player A queue")
    @ResponseBody
    @GetMapping("/turn/{gameId}/{playerTurn}")
    public GameResponse sendMessageToPlayerWithTurn(@PathVariable("gameId") String gameId,
                                                    @PathVariable("playerTurn") Player playerTurn,
                                                    @RequestParam("action") Action action) throws GameNotFoundExceptions {

        return playService.sendPlay(gameId, playerTurn, action);

    }
}
