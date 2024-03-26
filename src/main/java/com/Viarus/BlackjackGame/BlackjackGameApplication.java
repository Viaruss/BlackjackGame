package com.Viarus.BlackjackGame;

import com.Viarus.BlackjackGame.Table.TableDAO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BlackjackGameApplication {

	final TableDAO tableDAO;
	public BlackjackGameApplication(TableDAO tableDAO){this.tableDAO = tableDAO;}
	public static void main(String[] args) {
		SpringApplication.run(BlackjackGameApplication.class, args);
	}
}
