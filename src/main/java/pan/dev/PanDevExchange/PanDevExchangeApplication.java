package pan.dev.PanDevExchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.generics.TelegramBot;

@SpringBootApplication
public class PanDevExchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PanDevExchangeApplication.class, args);

		System.out.println(TelegramBot.class.getName());

	}

}
