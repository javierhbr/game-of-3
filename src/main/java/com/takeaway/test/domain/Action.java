package com.takeaway.test.domain;

import java.util.Random;
import java.util.stream.Stream;

public enum Action {
    MINUS (-1), ZERO(0), PLUS(1);

    private int value;

    Action(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


    public static Stream<Action> stream() {
        return Stream.of(Action.values());
    }

    public static Action getRandomAction() {
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }
}
