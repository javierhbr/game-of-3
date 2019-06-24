package com.takeaway.test.rest.controller;

import com.takeaway.test.TakeawayChallengeApplication;
import com.takeaway.test.domain.GameType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = TakeawayChallengeApplication.class)
@AutoConfigureMockMvc
public class GameControllerTest {

    @Autowired
    private MockMvc mvc;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String serverContextPath;

    private String mockUrl;

    @Before
    public void setUp() {
        this.mockUrl = "http://localhost:8080/gameServiceTest";
    }

    @Test
    public void whenCallApiStartNewGame_thenReturnNewGame() throws Exception {

        StringBuilder url = new StringBuilder();
        url.append(getBaseURLNewGame()).append("?gameType=").append(GameType.MANUAL)
                .append("&inputNumber=").append(56);

        mvc.perform(get(url.toString()).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.game.gameType").value("MANUAL"))
                .andExpect(jsonPath("$.game.originalInput").value("56"))
                .andExpect(jsonPath("$.game.playTurn.inputNumber").value("56"))
                .andExpect(jsonPath("$.game.activeGame").value("true"))
                .andExpect(jsonPath("$.game.activeGame").value("true"))
                .andExpect(jsonPath("$.turnsPlayer", hasSize(3)))
        ;
    }

    @Test
    public void whenCallApiStartNewGameWithoutParameters_thenReturnNewGame() throws Exception {

        mvc.perform(get(getBaseURLNewGame()).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.game.gameType").value("AUTOMATIC"))
                .andExpect(jsonPath("$.game.originalInput").isNumber())
                .andExpect(jsonPath("$.game.playTurn.inputNumber").isNumber())
                .andExpect(jsonPath("$.game.activeGame").value("true"))
                .andExpect(jsonPath("$.game.activeGame").value("true"))
                .andExpect(jsonPath("$.turnsPlayer", hasSize(3)))
        ;
    }

    private String getBaseURLNewGame(){
        return String.format("/game/start");
    }


}