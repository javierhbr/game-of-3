package com.takeaway.test.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ActionTest {

    @Test
    public void whenGetRandomAction_thenReturnActionValue() {

        Action action = Action.getRandomAction();
        assertThat(action, is(notNullValue()));
        assertThat(action, is(Action.valueOf(action.toString())));
    }
}