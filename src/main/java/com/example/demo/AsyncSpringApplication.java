package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableAsync
public class AsyncSpringApplication implements CommandLineRunner {
	private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
	public static void main(String[] args) {
		SpringApplication.run(AsyncSpringApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		FileUpdater fileUpdater = new FileUpdater();
		executor.scheduleAtFixedRate(()-> fileUpdater.updateFile(), 1, 10, TimeUnit.SECONDS);
		CompletableFuture.runAsync(() -> {
			try {
				Thread.sleep(5000);
				System.out.println("5 секунд від запуску програми");
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
	}
}

@Component
class FileUpdater {

	private static final Path FILE_PATH = Path.of("counter.txt");
	private int counter = 0;

	@PostConstruct
	public void initFile() {
		try {
			if (!Files.exists(FILE_PATH)) {
				Files.createFile(FILE_PATH);
			}
			Files.writeString(FILE_PATH, "", StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException("Error initializing file", e);
		}
	}

	@Scheduled
	public void updateFile() {
		counter += 5;
		try {
			Files.writeString(FILE_PATH, "Counter: " + counter + System.lineSeparator(), StandardOpenOption.APPEND);
			System.out.println("Updated counter to: " + counter);
		} catch (IOException e) {
			throw new RuntimeException("Error updating file", e);
		}
	}
}

