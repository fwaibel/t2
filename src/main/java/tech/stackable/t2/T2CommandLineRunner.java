package tech.stackable.t2;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Profile("cli")
@Component
public class T2CommandLineRunner implements CommandLineRunner {

    private final T2Command t2Command;

    public T2CommandLineRunner(T2Command t2Command) {
        this.t2Command = t2Command;
    }

    @Override
    public void run(String... args) {
        System.exit(new CommandLine(t2Command).execute(args));
    }
}
