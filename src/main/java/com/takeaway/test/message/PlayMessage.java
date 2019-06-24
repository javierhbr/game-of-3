package com.takeaway.test.message;


import com.takeaway.test.domain.GameType;
import com.takeaway.test.domain.PlayTurn;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@Builder
@Value
public class PlayMessage implements Serializable {

    private static final long serialVersionUID = 7526472295622776147L;
    private String gameId;
    private GameType gameType;
    private PlayTurn playTurn;

}
