package com.project.taigaAPI;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mashape.unirest.http.exceptions.UnirestException;

import Controller.TaigaController;

@SpringBootApplication
public class TaigaApiApplication {

	public static void main(String[] args)throws UnirestException {
		SpringApplication.run(TaigaApiApplication.class, args);
		
		System.out.println("---- Application Start ----");
		TaigaController.getProjectData();
	}

}
