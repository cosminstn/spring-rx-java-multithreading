package tech.sharply.rx.multithreading;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Random;

/**
 * Resources
 * <a href="https://medium.com/mobile-app-development-publication/rxjava-2-making-threading-easy-in-android-in-kotlin-603d8342d6c">Link 1</a>
 * <a href="https://www.baeldung.com/rxjava-schedulers">Link2</a>
 */
@SpringBootApplication
@EnableScheduling
public class App implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("Started app...");
		LOG.info("Main thread: " + getThreadInfo());

		var scheduler = Schedulers.newThread();

		var disposable = Single.fromCallable(this::superSlowProcess)
				.subscribeOn(Schedulers.computation())
				.observeOn(Schedulers.newThread())
				.subscribe(result -> LOG.info("Observed value " + result + " of super slow thread on thread: " + getThreadInfo()));

		Thread.sleep(7000);
		disposable.dispose();
	}

	@Scheduled(fixedRate = 1000)
	public void periodic() {
		// Seems like scheduled tasks are executed on another thread than the main thread.
		LOG.info("Main thread still active: " + getThreadInfo());
	}

	private Integer superSlowProcess() throws InterruptedException {
		LOG.info("Running super slow process on thread: " + getThreadInfo());
		Thread.sleep(5000);
		return new Random().nextInt(100);
	}

	private String getThreadInfo() {
		return String.format("name: %s, id: %d", Thread.currentThread().getName(), Thread.currentThread().getId());
	}
}
