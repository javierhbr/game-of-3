package com.takeaway.test.rest.controller;

import com.takeaway.test.domain.GameType;
import com.takeaway.test.exceptions.GameNotFoundExceptions;
import com.takeaway.test.rest.response.GameResponse;
import com.takeaway.test.service.GameService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
public class GameController {


    private GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @ApiOperation(value = "Start new Game",
    notes = "Automatic Game and random input number:\n" +
            "- http://localhost:9090/challenge/game/start\n" +
            "- http://localhost:9090/challenge/game/start?gameType=AUTOMATIC\n" +
            "\n" +
            "Manual Game and specific input number:\n" +
            "- http://localhost:9090/challenge/game/start?gameType=MANUAL&inputNumber=53\n" +
            "\n" +
            "Manual Game and random input number:\n" +
            "- http://localhost:9090/challenge/game/start?gameType=MANUAL")
    @ResponseBody
    @GetMapping("/game/start")
    public GameResponse startNewGameByPlayerA(@RequestParam(required = false) Optional<Integer> inputNumber,
                                              @RequestParam(required = false) Optional<GameType> gameType) {
        return gameService.createNewGame(inputNumber, gameType);
    }

    @ApiOperation(value = "Retrieve game status",
    notes = "Use the gameID from the game/start response to get the game status")
    @ResponseBody
    @GetMapping("/game/status/{gameId}")
    public GameResponse startNewGameByPlayerA(@PathVariable("gameId") Optional<String> gameId) throws GameNotFoundExceptions {
        return gameService.getGameData(gameId);
    }
}
