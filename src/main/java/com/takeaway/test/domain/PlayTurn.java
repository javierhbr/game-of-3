package com.takeaway.test.domain;

import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@Setter
@ToString
public class PlayTurn implements Serializable {

    private Integer inputNumber;
    private Player player;
    private Action action;

}
