package com.Viarus.BlackjackGame;

import com.Viarus.BlackjackGame.Game.Table.TableDAO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BlackjackGameApplication {

	final TableDAO tableDAO;
	public BlackjackGameApplication(TableDAO tableDAO){this.tableDAO = tableDAO;}

	static CommandLineRunner runner = args -> {
		//Test Code Here
	};

	public static void main(String[] args) {
		SpringApplication.run(BlackjackGameApplication.class, args);
		try {
			runner.run();
		} catch (Exception ignored){}
	}


}
