//package lms.doantotnghiep.service.impl;
//
//import lms.doantotnghiep.configuration.RabbitMQConfig;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class RabbitMQConsumer {
//    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
//    public void handle(String message) {
//        System.out.println("Handle message: " + message);
//    }
//}
