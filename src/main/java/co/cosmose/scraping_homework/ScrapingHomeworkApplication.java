package co.cosmose.scraping_homework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ScrapingHomeworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScrapingHomeworkApplication.class, args);
	}

}
