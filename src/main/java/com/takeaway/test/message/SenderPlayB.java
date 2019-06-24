package com.takeaway.test.message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class SenderPlayB implements PlaySender{

    JmsTemplate jmsTemplate;

    @Value("${game.messages.queue.player-b.name}")
    private String PLAYER_B_QUEUE;

    public SenderPlayB(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public void sendGameToPlayer(PlayMessage message) {
        jmsTemplate.convertAndSend(PLAYER_B_QUEUE, message);
    }
}
