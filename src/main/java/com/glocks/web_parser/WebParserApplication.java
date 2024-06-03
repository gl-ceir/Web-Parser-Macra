package com.glocks.web_parser;

import com.glocks.web_parser.controller.MainController;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableEncryptableProperties
@EnableScheduling
public class WebParserApplication implements CommandLineRunner {


//	FileOperations fileOperations = new FileOperations();

	@Autowired
	MainController mainController;
	public static void main(String[] args) {

		ApplicationContext context =	SpringApplication.run(WebParserApplication.class, args);
		MainController mainController = (MainController) context.getBean("mainController");

//		mainController.listPendingProcessTask(context);

//		fileOperations.sortFile("/Users/dexter/Downloads/sample2.txt", "/Users/dexter/Downloads/a.txt");
//		fileOperations.createDelDiff("/Users/dexter/web_parser/sample1.txt", "/Users/dexter/web_parser/sample2.txt", "/Users/dexter/web_parser/deltaDeleteFile.csv");
	}

	@Override
	public void run(String... args) throws Exception {
//		ApplicationContext context =	SpringApplication.run(WebParserApplication.class, args);

	}
}
