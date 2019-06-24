package com.takeaway.test.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

@Component
public class PlayMessageConverter implements MessageConverter {

    private Logger logger = LoggerFactory.getLogger(PlayMessageConverter.class);

    private ObjectMapper mapper;

    public PlayMessageConverter() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public Message toMessage(Object object, Session session)
            throws JMSException {
        PlayMessage playMessage = (PlayMessage) object;
        String payload = null;
        try {
            payload = mapper.writeValueAsString(playMessage);
            logger.info("outbound json='{}'", payload);
        } catch (JsonProcessingException e) {
            logger.error("error converting form person", e);
        }

        TextMessage message = session.createTextMessage();
        message.setText(payload);

        return message;
    }

    @Override
    public Object fromMessage(Message message) throws JMSException {
        TextMessage textMessage = (TextMessage) message;
        String payload = textMessage.getText();
        logger.info("inbound json='{}'", payload);

        PlayMessage playMessage = null;
        try {
            playMessage = mapper.readValue(payload, PlayMessage.class);
        } catch (Exception e) {
            logger.error("error converting to person", e);
        }

        return playMessage;
    }

}
