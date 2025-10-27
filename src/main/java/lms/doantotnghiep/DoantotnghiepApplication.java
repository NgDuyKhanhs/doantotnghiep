package lms.doantotnghiep;

//import lms.doantotnghiep.service.RabbitMQProducerService;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRabbit
@SpringBootApplication
@EnableRedisHttpSession
public class DoantotnghiepApplication {

	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(DoantotnghiepApplication.class, args);

//		//test
//		RabbitMQProducerService messageProducer = applicationContext
//				.getBean(RabbitMQProducerService.class);
//		messageProducer.send("Hello Enzo");
	}

}
