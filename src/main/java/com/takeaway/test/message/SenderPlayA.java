package com.takeaway.test.message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class SenderPlayA implements PlaySender {

    JmsTemplate jmsTemplate;

    @Value("${game.messages.queue.player-a.name}")
    private String PLAYER_A_QUEUE;

    public SenderPlayA(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public void sendGameToPlayer(PlayMessage message) {
        jmsTemplate.convertAndSend(PLAYER_A_QUEUE, message);

    }
}
