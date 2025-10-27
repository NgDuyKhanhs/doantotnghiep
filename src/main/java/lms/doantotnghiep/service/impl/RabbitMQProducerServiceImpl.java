//package lms.doantotnghiep.service.impl;
//
//import lms.doantotnghiep.configuration.RabbitMQConfig;
//import lms.doantotnghiep.service.RabbitMQProducerService;
//import lombok.AllArgsConstructor;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Component;
//
//@Component
//@AllArgsConstructor
//public class RabbitMQProducerServiceImpl implements RabbitMQProducerService {
//    private final RabbitTemplate rabbitTemplate;
//    @Override
//    public void send(String rabbitMQMessage) {
//        rabbitTemplate.convertAndSend(
//                RabbitMQConfig.EXCHANGE_NAME,
//                RabbitMQConfig.ROUTING_KEY,
//                rabbitMQMessage
//        );
//        System.out.println("Sent: " + rabbitMQMessage);
//    }
//}
