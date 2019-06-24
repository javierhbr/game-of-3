package com.takeaway.test.rest.response;

import com.takeaway.test.domain.Game;
import com.takeaway.test.domain.PlayTurn;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class GameResponse {

    private Game game;
    private String gameStatus;
    private List<PlayTurn> plays;
    private List<String> turnsPlayer;

}
